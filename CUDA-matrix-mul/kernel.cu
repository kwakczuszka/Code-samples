/*
This program utilizes GPU to multiply two randomly generated square matrices (1024x1024) using standard O(N^2) algorithm.
*/
#include <algorithm>
#include <cassert>
#include <cstdlib>
#include <functional>
#include <iostream>
#include <vector>
#include <fstream>
#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <stdio.h>

__global__ void matrixMul(const int* a, const int* b, int* c, int N) {
    int row = blockIdx.y * blockDim.y + threadIdx.y;
    int col = blockIdx.x * blockDim.x + threadIdx.x;

    c[row * N + col] = 0;
    for (int k = 0; k < N; k++) {
        c[row * N + col] += a[row * N + k] * b[k * N + col];
    }
}

void printMatrix(std::vector<int> matrix, int rows, int cols, std::ostream& os) {
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            os << matrix[i * rows + j] << " ";
        }
        os << "\n";
    }
}

void verify_result(std::vector<int>& a, std::vector<int>& b, std::vector<int>& c, int N) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            int tmp = 0;
            for (int k = 0; k < N; k++) {
                tmp += a[i * N + k] * b[k * N + j];
            }
            assert(tmp == c[i * N + j]);    //check control sum
        }
    }
}

int main() {
    int N = 1 << 10;    // Matrix size of 1024 x 1024;

    size_t bytes = N * N * sizeof(int);

    std::vector<int> host_a(N * N);
    std::vector<int> host_b(N * N);
    std::vector<int> host_c(N * N);

    std::generate(host_a.begin(), host_a.end(), []() { return rand() % 100; });
    std::generate(host_b.begin(), host_b.end(), []() { return rand() % 100; });

    int* device_a, * device_b, * device_c;
    cudaMalloc(&device_a, bytes);
    cudaMalloc(&device_b, bytes);
    cudaMalloc(&device_c, bytes);

    cudaMemcpy(device_a, host_a.data(), bytes, cudaMemcpyHostToDevice);
    cudaMemcpy(device_b, host_b.data(), bytes, cudaMemcpyHostToDevice);

    int THREADS = 32;

    int BLOCKS = N / THREADS;

    dim3 threads(THREADS, THREADS);
    dim3 blocks(BLOCKS, BLOCKS);
    std::cout << "GPU calculation started\n";

    matrixMul <<<blocks, threads >>> (device_a, device_b, device_c, N);

    std::cout << "GPU calculation finished\n";
    cudaMemcpy(host_c.data(), device_c, bytes, cudaMemcpyDeviceToHost);
    std::cout << "CPU calculation started\n";

    verify_result(host_a, host_b, host_c, N);
    std::cout << "CPU calculation finished\n";

    std::ofstream fileout1("mat1.txt");
    std::ofstream fileout2("mat2.txt");
    std::ofstream fileout3("res.txt");

    printMatrix(host_a, N, N, fileout1);
    printMatrix(host_b, N, N, fileout2);
    printMatrix(host_c, N, N, fileout3);

    fileout1.close();
    fileout2.close();
    fileout3.close();

    std::cout << "COMPLETED SUCCESSFULLY\n";

    cudaFree(device_a);
    cudaFree(device_b);
    cudaFree(device_c);

    return 0;
}