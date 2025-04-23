#include "mpi.h"
#include <algorithm>
#include <cstdio>
#include <iostream>
#include <cstring>
#include <ostream>
#include <random>
#include <chrono>
#include <vector>
#include <unistd.h>
#include<fstream>

inline void SetColor(int textColor) {
    std::cout << "\033[" << textColor << "m";
}

inline void ResetColor() { 
    std::cout << "\033[0m"; 
}

#define N 1000

void generateMatrix(int** &matrix) {
    std::random_device rd;  
    std::mt19937 gen(rd()); 
    
    std::uniform_int_distribution<> dist(-100, 100); 
    
    for (int i = 0; i < N; ++i) {
        for(int j = 0; j < N; ++j) {
            matrix[i][j] = dist(gen);
        }
    }
}

void printMatrix(int** &matrix, int rows, int cols, std::ostream &os) { 
    for (int i = 0; i < rows; ++i) {          
        for (int j = 0; j < cols; ++j) {      
            os << matrix[i][j] << " "; 
        }
        os << "\n";                    
    }
}

int arrMul(int x[N], int y[N]) {
    int ret = 0;
    for(int i = 0; i<N; i++){
        ret+=x[i]*y[i];
    }
    return ret;
}

void matToArr(int** matrix, int* &arr, int rows, int cols){
    for(int i = 0; i<rows; i++){
        for(int j = 0; j<cols; j++){
            arr[i*rows+j] = matrix[i][j];
        }
    }

}

void arrToMat(int** &matrix, int* arr, int rows, int cols){
    for(int i = 0; i<rows; i++){
        for(int j = 0; j<cols; j++){
            matrix[i][j] = arr[i*rows+j];
        }
    }

}

void matrixMul(int** matrix1, int** matrix2, int** &resultMatrix, int rows, int cols){
    for(int i=0; i<rows; i++){
        int arr1[N];
        for(int itr = 0; itr < N; itr++){
                arr1[itr] = matrix1[i][itr];
        }

        for(int j = 0; j < cols; j++){
            int arr2[N];
            for(int itr = 0; itr < N; itr++){
                arr2[itr] = matrix2[itr][j];
            }

            resultMatrix[i][j]=arrMul(arr1, arr2);
        }
    }
}

void initMatrix(int** &matrix, int rows, int cols){
    matrix = new int*[rows];
    for(int i=0; i<rows; i++){
        matrix[i] = new int[cols];
    }
}

int main(int argc, char** argv) {


    MPI_Init(&argc, &argv);
    int world_rank, world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    if(world_rank == 0) {
        
        int** matrix0;
        int** matrix1;
        int** matrix2;
        initMatrix(matrix0, N, N);
        initMatrix(matrix1, N, N);
        initMatrix(matrix2, N, N);

        generateMatrix(matrix0);
        generateMatrix(matrix1);

        SetColor(92);
        std::cout<<"Wygenerowano macierze kwadratowe o rozmiarze "<<N<<"\n";
        
        std::vector<int*> recvBuffers;
        recvBuffers.resize(world_size);
        std::generate(recvBuffers.begin(), recvBuffers.end(), 
            [world_size]() { return new int[N * N / world_size]; });
        
        SetColor(95);
        std::cout<<"Początek obliczeń\n";
        ResetColor();

        auto start = std::chrono::high_resolution_clock::now();


        for(int itr = 1; itr<world_size; itr++){

            int** matrixToSend;
            initMatrix(matrixToSend, N/world_size, N);

            int startRow = itr*N/world_size;
            int stopRow = (itr+1)*N/world_size;
            for(int i = startRow; i<stopRow; i++){
                for(int j=0; j<N; j++){
                    matrixToSend[i%(N/world_size)][j] = matrix0[i][j];
                }
            }
            int* message = new int[N*N/world_size];
            // std::cout<<"step1\n";
            matToArr(matrixToSend, message, N/world_size, N);
            // std::cout<<"step2\n";

            MPI_Send(message, N*N/world_size, MPI_INT,
                itr, 0, MPI_COMM_WORLD);
            // std::cout<<"step3\n";

        }

        int* messageBcast = new int[N*N];
        matToArr(matrix1, messageBcast, N, N);
        MPI_Bcast(messageBcast, N*N, 
            MPI_INT, 0, MPI_COMM_WORLD);

        
        int** calcMat; int** resultMat;
        initMatrix(calcMat, N/world_size, N);
        initMatrix(resultMat, N/world_size, N);
        int stopRow = N/world_size;
        for(int i = 0; i<stopRow; i++){
            for(int j=0; j<N; j++){
                calcMat[i][j] = matrix0[i][j];
            }
        }
        matrixMul(calcMat, matrix1, resultMat, N/world_size, N);
        int* message = new int[N*N/world_size];
        matToArr(resultMat, message, N/world_size, N);
        recvBuffers[0] = message;
        int cnt = N*N/world_size;
        SetColor(97);
        std::cout<<"Proces ";
        SetColor(96);
        std::cout<<world_rank;
        SetColor(97);
        std::cout<<" zakończył obliczenia.\n";
        ResetColor();
        for(int i = 1; i<world_size; i++){
            MPI_Recv(recvBuffers[i], cnt, MPI_INT, 
        i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        }
        auto stop = std::chrono::high_resolution_clock::now();
        std::chrono::duration<double> timeWall = stop - start;  
        SetColor(95);
        std::cout<<"Koniec obliczeń, uzyskano czas:\n";
        SetColor(96);
        std::cout<<timeWall.count()<<"s\n";
        SetColor(95);
        std::cout<<"dla ";
        SetColor(96);
        std::cout<<world_size;
        SetColor(95);
        std::cout<<" rdzeni.\n";

        for(int i = 0; i<world_size; i++){
            int** tempMat;
            initMatrix(tempMat, N/world_size, N);
            arrToMat(tempMat, recvBuffers[i], N/world_size, N);
            for(int j = 0; j<N/world_size; j++){
                for(int k = 0; k<N; k++){
                    matrix2[i*N/world_size+j][k] = tempMat[j][k];
                }
            }
        }

        std::ofstream result("result.txt");
        std::ofstream mat1("matrix1.txt");
        std::ofstream mat2("matrix2.txt");
        printMatrix(matrix2, N, N, result);
        printMatrix(matrix0, N, N, mat1);
        printMatrix(matrix1, N, N, mat2);
        result.close();
        mat1.close();
        mat2.close();

    } else {
        int* message = new int[N*N/world_size];
        int* arrayBcast = new int[N*N];
        int** calcMat; int** resultMat; int** matrixBcast;
        initMatrix(calcMat, N/world_size, N);
        initMatrix(resultMat, N/world_size, N);
        initMatrix(matrixBcast, N, N);
        
        MPI_Recv(message, N*N/world_size, MPI_INT,
        0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        
        MPI_Bcast(arrayBcast, N*N, 
            MPI_INT, 0, MPI_COMM_WORLD);

        arrToMat(matrixBcast, arrayBcast, N, N);
        arrToMat(calcMat, message, N/world_size, N);

        matrixMul(calcMat, matrixBcast, 
            resultMat, N/world_size, N);
        matToArr(resultMat, message, N/world_size, N);
        SetColor(97);
        std::cout<<"Proces ";
        SetColor(96);
        std::cout<<world_rank;
        SetColor(97);
        std::cout<<" zakończył obliczenia.\n";
        ResetColor();
        MPI_Send(message, N*N/world_size, MPI_INT,
        0, 0, MPI_COMM_WORLD);
   }

    MPI_Finalize();

    
}