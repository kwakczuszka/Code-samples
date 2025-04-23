/*
The goal of this task is to implement a CUDA program that computes a known integral (f(x) = 4/(1+x^2)) using the trapezoidal method.
*/
#include <cuda.h>
#include <iostream>

__global__ void trapezoidalIntegration(double* results, double step, int n) {
    int tid = blockIdx.x * blockDim.x + threadIdx.x;

    if (tid < n) {
        double x1 = tid * step;
        double x2 = (tid + 1) * step;
        results[tid] = (4.0 / (1.0 + x1 * x1) + 4.0 / (1.0 + x2 * x2)) * step / 2.0;    // f(x) = 4/(1+x^2)
    }
}

int main() {
    const int N = 10000000;
    const int blockSize = 256;
    const int gridSize = (N + blockSize - 1) / blockSize;

    double step = 1.0 / N;
    double* results;
    cudaMallocManaged(&results, N * sizeof(double));

    trapezoidalIntegration << <gridSize, blockSize >> > (results, step, N);
    cudaDeviceSynchronize();

    double sum = 0.0;
    for (int i = 0; i < N; ++i) {
        sum += results[i];
    }

    std::cout << "Wartość całki: " << sum << std::endl;

    cudaFree(results);
    return 0;
}
