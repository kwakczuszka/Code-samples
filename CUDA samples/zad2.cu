
#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <vector>
#include <numeric>
#include <stdio.h>
#include <random>
#include <iostream>

cudaError_t addWithCuda(int *a, int *b, int *out, unsigned int size);

__global__ void addKernel(int *a, int *b, int *out, int size)
{
    int idx = blockIdx.x * blockDim.x + threadIdx.x;

    // Check if thread is not out of bounds
    if (idx < size) {
        out[idx] = a[idx] + b[idx];
    }
}

int main()
{
    const long arraySize = 10000000;
    int* a = new int[arraySize];
    int* b = new int[arraySize];
    int* out = new int[arraySize];

    std::random_device rd;
    std::mt19937 gen(rd());

    std::uniform_int_distribution<> dist(-100, 100);

    int control_sum1 = 0;
    for (int i = 0; i < arraySize; ++i) {
        a[i] = dist(gen);
        b[i] = dist(gen);
        control_sum1 += a[i];
        control_sum1 += b[i];
    }
    
    

    // Add vectors in parallel.
    cudaError_t cudaStatus = addWithCuda(a, b, out, arraySize);
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "addWithCuda failed!");
        return 1;
    }



    // cudaDeviceReset must be called before exiting in order for profiling and
    // tracing tools such as Nsight and Visual Profiler to show complete traces.
    cudaStatus = cudaDeviceReset();
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaDeviceReset failed!");
        return 1;
    }


    int control_sum2=0;
    for (int i = 0; i < arraySize; i++) control_sum2 += out[i];
    std::cout << "suma kontrolna:\t" << control_sum1 << "\nsuma rzeczywista:\t"<<control_sum2;

    return 0;
}

// Helper function for using CUDA to add vectors in parallel.
cudaError_t addWithCuda(int *a, int *b, int *out, unsigned int size)
{
    int *dev_a = 0;
    int *dev_b = 0;
    int *dev_out = 0;
    cudaError_t cudaStatus;

    // Choose which GPU to run on, change this on a multi-GPU system.
    cudaStatus = cudaSetDevice(0);
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaSetDevice failed!  Do you have a CUDA-capable GPU installed?");
        goto Error;
    }


    cudaStatus = cudaMalloc((void**)&dev_a, size * sizeof(int));
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMalloc failed!");
        goto Error;
    }

    cudaStatus = cudaMalloc((void**)&dev_b, size * sizeof(int));
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMalloc failed!");
        goto Error;
    }

     cudaStatus = cudaMalloc((void**)&dev_out, size * sizeof(int));
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMalloc failed!");
        goto Error;
    }

    // Copy input vectors from host memory to GPU buffers.
    cudaStatus = cudaMemcpy(dev_a, a, size * sizeof(int), cudaMemcpyHostToDevice);
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMemcpy failed!");
        goto Error;
    }

    cudaStatus = cudaMemcpy(dev_b, b, size * sizeof(int), cudaMemcpyHostToDevice);
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMemcpy failed!");
        goto Error;
    }

    int block_size;
    int min_grid_size;
    int grid_size;

    cudaOccupancyMaxPotentialBlockSize(&min_grid_size, &block_size, addKernel, 0, size);

    grid_size = (size + block_size - 1) / block_size;

    // Launch a kernel on the GPU with one thread for each element.
    addKernel<<<grid_size, block_size>>>(dev_a, dev_b, dev_out, size);

    // Check for any errors launching the kernel
    cudaStatus = cudaGetLastError();
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "addKernel launch failed: %s\n", cudaGetErrorString(cudaStatus));
        goto Error;
    }
    
    // cudaDeviceSynchronize waits for the kernel to finish, and returns
    // any errors encountered during the launch.
    cudaStatus = cudaDeviceSynchronize();
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaDeviceSynchronize returned error code %d after launching addKernel!\n", cudaStatus);
        goto Error;
    }

    // Copy output vector from GPU buffer to host memory.
    cudaStatus = cudaMemcpy(out, dev_out, size * sizeof(int), cudaMemcpyDeviceToHost);
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaMemcpy failed!");
        goto Error;
    }


Error:
    cudaFree(dev_a);
    cudaFree(dev_b);
    cudaFree(dev_out);
    return cudaStatus;
}
