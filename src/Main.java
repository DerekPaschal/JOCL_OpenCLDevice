import static org.jocl.CL.clCreateCommandQueueWithProperties;
import static org.jocl.CL.clCreateContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

import com.dtp_dev.jocl_utilities.CLUtil;

/**
 * 
 * @author Derek
 *
 */
public class Main {

	final static boolean useViewer = true;	
	final static boolean onDeviceIntegration = false;
	
	static Scanner in;
	
	static int n= (int)(Math.pow(2, 13)); //Massive Particles
	
	static float arrX[];
	static float arrY[];
	
	static float arrXv[];
	static float arrYv[];
	
	static float arrXa[][];
	static float arrYa[][];
	
	static float arrM[];
	
	
	static float Seconds = (float)500.0;
	
	static ParticleView viewer;
	
	
	private static void generateDisk(int[] nP)
	{
		int d = nP.length;
		arrX = new float[n];
		arrY = new float[n];
		arrM = new float[n];
		
		arrXv = new float[n];
		arrYv = new float[n];
				
		arrXa = new float[d][];
		arrYa = new float[d][];
		for (int i = 0; i < d; i++) {
			arrXa[i] = new float[(nP[i])];
			arrYa[i] = new float[(nP[i])];
		}
		
		
		float radius = 200;
		float theta;
		float r;
		float centerX = 0.0f, centerY = 0.0f;
		float avgMass = 0.0001f;
		float variance = 0.01f;
		
		for (int i = 0; i < n; i++)
		{	
			r = (float) Math.sqrt(Math.random()*radius*radius);
			theta = (float) (Math.random() * 6.28318);
			arrX[i] = (float) (r * Math.cos(theta))+centerX;
			arrY[i] = (float) (r * Math.sin(theta))+centerY;
			
			arrM[i] = (float) (((avgMass)) + ((Math.random()-0.5)*(variance*avgMass)));
		}
		
	}
	
	
	private static void SingleCL() throws Exception
	{
		/**
	     * The source code of the OpenCL program to execute
	     */
		String programSource = new String(Files.readAllBytes(Paths.get("kernels/Test4.cl")), StandardCharsets.UTF_8);
		
		generateDisk(new int[] {n});
		
		//OpenCL
		//Java exceptions should be thrown when OpenCL API has errors
		CL.setExceptionsEnabled(CL.CL_TRUE);
		
		//Create device manager and find optimal device
		cl_device_id[] devices = CLUtil.getAllOpenCLDevices();
		cl_device_id device = null;
		for(int i = 0; i < devices.length; i++) {
			System.out.println("[" + i + "] : " + CLUtil.getDeviceString(devices[i], CL.CL_DEVICE_NAME));
		}
		device = devices[in.nextInt()];
		
		// Create a context
        cl_context context = clCreateContext(null, 1, new cl_device_id[]{ device }, null, null, null);

        // Create the command queue
        cl_command_queue command_queue = clCreateCommandQueueWithProperties(context, device, null, null);
        
        //Create memory buffers on devices
        cl_mem x_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrX).length, null, null);
        cl_mem y_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrY).length, null, null);
        cl_mem xa_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrXa[0]).length, null, null);
        cl_mem ya_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrYa[0]).length, null, null);
        cl_mem m_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrM).length, null, null);
        
        
        //Copy the arrays x,y,m to their memory buffers
        CL.clEnqueueWriteBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrX).length, Pointer.to((float[])arrX), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrY).length, Pointer.to((float[])arrY), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, m_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrM).length, Pointer.to((float[])arrM), 0, null, null);
		
        //Create the program from kernel source
        cl_program program = CL.clCreateProgramWithSource(context, 1, new String[] { programSource }, null, null);
        
        //Build program
        CL.clBuildProgram(program, 0, null, null, null, null);
        
        //Create Grav kernel
        cl_kernel kernelGrav = CL.clCreateKernel(program, "Grav", null);
        
        //Set Grav kernel arguments
        CL.clSetKernelArg(kernelGrav, 0, Sizeof.cl_mem, Pointer.to(x_mem_obj));
        CL.clSetKernelArg(kernelGrav, 1, Sizeof.cl_mem, Pointer.to(y_mem_obj));
        CL.clSetKernelArg(kernelGrav, 2, Sizeof.cl_mem, Pointer.to(m_mem_obj));
        CL.clSetKernelArg(kernelGrav, 3, Sizeof.cl_mem, Pointer.to(xa_mem_obj));
        CL.clSetKernelArg(kernelGrav, 4, Sizeof.cl_mem, Pointer.to(ya_mem_obj));
        
		if (useViewer)
			viewer = new ParticleView("OpenCL Compute Window");
		
		//Begin the Compute Benchmark
		System.out.println("\nStart OpenCL Compute with: \t"+ CLUtil.getDeviceString(device, CL.CL_DEVICE_NAME));
		int TimesToRun = Math.round(Seconds);
		long before = System.nanoTime();
		for (int i = 0; i < TimesToRun; i++)
		{			
			CL.clEnqueueNDRangeKernel(command_queue, kernelGrav, 1, null, new long[] {n}, null, 0, null, null);
			if (useViewer) {
				viewer.PaintParticleView(n, arrX, arrY);
			}
			CL.clFinish(command_queue);
			
			
			//Complete integration on host
			CL.clEnqueueReadBuffer(command_queue, xa_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrXa[0]), 0, null, null);
			CL.clEnqueueReadBuffer(command_queue, ya_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrYa[0]), 0, null, null);
			for (int j = 0; j < n; j++) {
				arrXv[j] += arrXa[0][j];
				arrYv[j] += arrYa[0][j];
				
				arrX[j] += arrXv[j];
				arrY[j] += arrYv[j];
			}
			CL.clEnqueueWriteBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrX), 0, null, null);
			CL.clEnqueueWriteBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrY), 0, null, null);
			
			
		}
		long after = System.nanoTime();
		double duration =((after-before)/(1e9f));
		
		//Clean up
		CL.clFlush(command_queue);
		CL.clFinish(command_queue);
		CL.clReleaseKernel(kernelGrav);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(x_mem_obj);
		CL.clReleaseMemObject(y_mem_obj);
		CL.clReleaseMemObject(xa_mem_obj);
		CL.clReleaseMemObject(ya_mem_obj);
		CL.clReleaseMemObject(m_mem_obj);
		CL.clReleaseCommandQueue(command_queue);
		CL.clReleaseContext(context);
		if(useViewer)
			viewer.dispose();
		
		//Print Info
		System.out.println("Duration: \t\t\t" + ((int)(Math.round(duration*10.0))/10.0) + " s");
		System.out.println("AVG Computes Per Second:\t" + ((int)(Math.round(TimesToRun/(duration/100.0)))/100.0));
	}
	
	
	private static void SingleSharedCL() throws Exception
	{
		/**
	     * The source code of the OpenCL program to execute
	     */
		String programSource = new String(Files.readAllBytes(Paths.get("kernels/Test4.cl")), StandardCharsets.UTF_8);
		
		generateDisk(new int[] {n});
		
		//OpenCL
		//Java exceptions should be thrown when OpenCL API has errors
		CL.setExceptionsEnabled(CL.CL_TRUE);
		
		//Create device manager and find optimal device
		cl_device_id[] devices = CLUtil.getAllOpenCLDevices();
		cl_device_id device = null;
		for(int i = 0; i < devices.length; i++) {
			System.out.println("[" + i + "] : " + CLUtil.getDeviceString(devices[i], CL.CL_DEVICE_NAME));
		}
		device = devices[in.nextInt()];
		
		//Check device maximum local memory
		long local_size = CLUtil.getDeviceSizes(device, CL.CL_DEVICE_MAX_WORK_ITEM_SIZES, CLUtil.getDeviceInt(device, CL.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS))[0];
		local_size = Math.min(local_size, CLUtil.getDeviceLong(device, CL.CL_DEVICE_MAX_WORK_GROUP_SIZE));
				
		//If local memory size is less than memory taken by local buffers (Entered manually unfortunately)
		local_size = Math.min(local_size, CLUtil.getMaxDeviceLocalSizeFromLocalMem(device, 0, Sizeof.cl_float4));
		
		// Create a context
        cl_context context = clCreateContext(null, 1, new cl_device_id[]{ device }, null, null, null);

        // Create the command queue
        cl_command_queue command_queue = clCreateCommandQueueWithProperties(context, device, null, null);
        
        //Create memory buffers on devices
        cl_mem x_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrX).length, null, null);
        cl_mem y_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrY).length, null, null);
        cl_mem xa_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrXa[0]).length, null, null);
        cl_mem ya_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrYa[0]).length, null, null);
        cl_mem m_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrM).length, null, null);
        
        
        //Copy the arrays x,y,m to their memory buffers
        CL.clEnqueueWriteBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrX).length, Pointer.to((float[])arrX), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrY).length, Pointer.to((float[])arrY), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, m_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrM).length, Pointer.to((float[])arrM), 0, null, null);
		
        //Create the program from kernel source
        cl_program program = CL.clCreateProgramWithSource(context, 1, new String[] { programSource }, null, null);
        
        //Build program
        CL.clBuildProgram(program, 0, null, null, null, null);
        
        //Create Grav kernel
        cl_kernel kernelGrav = CL.clCreateKernel(program, "GravShared", null);
        
        //Set Grav kernel arguments
        CL.clSetKernelArg(kernelGrav, 0, Sizeof.cl_mem, Pointer.to(x_mem_obj));
        CL.clSetKernelArg(kernelGrav, 1, Sizeof.cl_mem, Pointer.to(y_mem_obj));
        CL.clSetKernelArg(kernelGrav, 2, Sizeof.cl_mem, Pointer.to(m_mem_obj));
        CL.clSetKernelArg(kernelGrav, 3, Sizeof.cl_mem, Pointer.to(xa_mem_obj));
        CL.clSetKernelArg(kernelGrav, 4, Sizeof.cl_mem, Pointer.to(ya_mem_obj));
        CL.clSetKernelArg(kernelGrav, 5, Sizeof.cl_float4 * local_size, null);
        
        //If kernel work size is lower than current work size, adapt
        long kernel_work_size = CLUtil.getKernelSize(device, kernelGrav, CL.CL_KERNEL_WORK_GROUP_SIZE);
        if (kernel_work_size < local_size) {
        	local_size = kernel_work_size;
        	
        	//reassign any local kernel arguments depending on local_size
        	CL.clSetKernelArg(kernelGrav, 5, Sizeof.cl_float4 * local_size, null);
        }
        
		if (useViewer)
			viewer = new ParticleView("OpenCL Compute Window");
		
		//Begin the Compute Benchmark
		System.out.println("\nStart OpenCL Compute with: \t"+ CLUtil.getDeviceString(device, CL.CL_DEVICE_NAME));
		System.out.println("With Local Size: " + local_size);
		int TimesToRun = Math.round(Seconds);
		long before = System.nanoTime();
		for (int i = 0; i < TimesToRun; i++)
		{			
			CL.clEnqueueNDRangeKernel(command_queue, kernelGrav, 1, null, new long[] {n}, new long[] {local_size}, 0, null, null);
			if (useViewer) {
				viewer.PaintParticleView(n, arrX, arrY);
			}
			CL.clFinish(command_queue);
			
			
			//Complete integration on host
			CL.clEnqueueReadBuffer(command_queue, xa_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrXa[0]), 0, null, null);
			CL.clEnqueueReadBuffer(command_queue, ya_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrYa[0]), 0, null, null);
			for (int j = 0; j < n; j++) {
				arrXv[j] += arrXa[0][j];
				arrYv[j] += arrYa[0][j];
				
				arrX[j] += arrXv[j];
				arrY[j] += arrYv[j];
			}
			CL.clEnqueueWriteBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrX), 0, null, null);
			CL.clEnqueueWriteBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrY), 0, null, null);
			
			
		}
		long after = System.nanoTime();
		double duration =((after-before)/(1e9f));
		
		//Clean up
		CL.clFlush(command_queue);
		CL.clFinish(command_queue);
		CL.clReleaseKernel(kernelGrav);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(x_mem_obj);
		CL.clReleaseMemObject(y_mem_obj);
		CL.clReleaseMemObject(xa_mem_obj);
		CL.clReleaseMemObject(ya_mem_obj);
		CL.clReleaseMemObject(m_mem_obj);
		CL.clReleaseCommandQueue(command_queue);
		CL.clReleaseContext(context);
		if(useViewer)
			viewer.dispose();
		
		//Print Info
		System.out.println("Duration: \t\t\t" + ((int)(Math.round(duration*10.0))/10.0) + " s");
		System.out.println("AVG Computes Per Second:\t" + ((int)(Math.round(TimesToRun/(duration/100.0)))/100.0));
	}
	
	public static void MultiDeviceCL() throws IOException {
		/**
	     * The source code of the OpenCL program to execute
	     */
		String programSource = new String(Files.readAllBytes(Paths.get("kernels/Test4.cl")), StandardCharsets.UTF_8);

		//OpenCL
		//Java exceptions should be thrown when OpenCL API has errors
		CL.setExceptionsEnabled(CL.CL_TRUE);
		
		//Find requested Platform
		cl_platform_id[] platforms = CLUtil.getOpenCLPlatforms();
		cl_platform_id platform = null;
		for(int i = 0; i < platforms.length; i++) {
			System.out.println("[" + i + "] : " + CLUtil.getPlatformString(platforms[i], CL.CL_PLATFORM_NAME));
		}
		platform = platforms[in.nextInt()];
		//Get devices from selected platform
		cl_device_id devices[] = CLUtil.getOpenCLDevices(platform);
		int d = devices.length;
		
		int[] nP = new int[d];
		int remain = n;
		for (int i = 0; i < d-1; i++) {
			nP[i] = n/d;
			remain -= nP[i];
		}
		nP[d-1] = remain;
		
		int[] nPScan = new int[d];
		for (int i = 1; i < d; i++) {
			nPScan[i] += nPScan[i-1] + nP[i-1];
		}
		
		generateDisk(nP);
		
		//Create contexts
		cl_context[] contexts = new cl_context[d];
		for (int i = 0; i < d; i++) {
			contexts[i] = CL.clCreateContext(null, 1, new cl_device_id[] {devices[i]}, null, null, null);
		}
		
		//Create command queues
		cl_command_queue[] command_queues = new cl_command_queue[d];
		for (int i = 0; i < d; i++) {
			command_queues[i] = CL.clCreateCommandQueueWithProperties(contexts[i], devices[i], null, null);
		}
		
		//Create device buffers
		//All buffers are (unfortunately) created on all devices in the context.  This means each device will have to have enough memory to execute the entire program		
		cl_mem[] x_mem_objs = new cl_mem[d];
		cl_mem[] y_mem_objs = new cl_mem[d];
		cl_mem[] m_mem_objs = new cl_mem[d];
		cl_mem[] xa_mem_objs = new cl_mem[d];
		cl_mem[] ya_mem_objs = new cl_mem[d];
		for (int i = 0; i < d; i++) {
			x_mem_objs[i] = CL.clCreateBuffer(contexts[i], CL.CL_MEM_READ_WRITE, Sizeof.cl_float * n, null, null);
			y_mem_objs[i] = CL.clCreateBuffer(contexts[i], CL.CL_MEM_READ_WRITE, Sizeof.cl_float * n, null, null);
			m_mem_objs[i] = CL.clCreateBuffer(contexts[i], CL.CL_MEM_READ_WRITE, Sizeof.cl_float * n, null, null);
			xa_mem_objs[i] = CL.clCreateBuffer(contexts[i], CL.CL_MEM_READ_WRITE, Sizeof.cl_float * nP[i], null, null);
			ya_mem_objs[i] = CL.clCreateBuffer(contexts[i], CL.CL_MEM_READ_WRITE, Sizeof.cl_float * nP[i], null, null);
		}
		
		//Write data to buffers
		for (int i = 0; i < d; i++) {
			CL.clEnqueueWriteBuffer(command_queues[i], x_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to((float[])arrX), 0, null, null);
			CL.clEnqueueWriteBuffer(command_queues[i], y_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to((float[])arrY), 0, null, null);
			CL.clEnqueueWriteBuffer(command_queues[i], m_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to((float[])arrM), 0, null, null);
		}
		
		//Create programs
		cl_program[] programs = new cl_program[d];
		for (int i = 0; i < d; i++) {
			programs[i] = CL.clCreateProgramWithSource(contexts[i], 1, new String[] { programSource }, null, null);
			CL.clBuildProgram(programs[i], 0, null, null, null, null);
		}
		
        //Build a kernel for each device from the program
        cl_kernel kernels[] = new cl_kernel[d];
        for (int i = 0; i < d; i++) {
        	kernels[i] = CL.clCreateKernel(programs[i], "GravMulti", null);
        }
		
        //Set initial kernel arguments
        for (int i = 0; i < d; i++) {
        	CL.clSetKernelArg(kernels[i], 0, Sizeof.cl_mem, Pointer.to(x_mem_objs[i]));
        	CL.clSetKernelArg(kernels[i], 1, Sizeof.cl_mem, Pointer.to(y_mem_objs[i]));
        	CL.clSetKernelArg(kernels[i], 2, Sizeof.cl_mem, Pointer.to(m_mem_objs[i]));
        }
        
        if (useViewer)
			viewer = new ParticleView("OpenCL Compute Window");
		
        //Print which devices will be used
        System.out.print("\nStart OpenCL Compute with: \t\n");
		for (int i = 0; i < devices.length; i++) {
			System.out.println("\t\t" + CLUtil.getDeviceString(devices[i], CL.CL_DEVICE_NAME));
		}
        
		//Begin the Compute Benchmark
		int TimesToRun = Math.round(Seconds);
		long before = System.nanoTime();
		for (int r = 0; r < TimesToRun; r++)
		{	
			//Run all device code
			for (int i = 0; i < d; i++) {
				
				int offset = nPScan[i];
				CL.clSetKernelArg(kernels[i], 3, Sizeof.cl_mem, Pointer.to(xa_mem_objs[i]));
				CL.clSetKernelArg(kernels[i], 4, Sizeof.cl_mem, Pointer.to(ya_mem_objs[i]));
				CL.clSetKernelArg(kernels[i], 5, Sizeof.cl_int, Pointer.to(new int[] { n }));
				CL.clSetKernelArg(kernels[i], 6, Sizeof.cl_int, Pointer.to(new int[] { offset })); //offset value
				
				CL.clEnqueueNDRangeKernel(command_queues[i], kernels[i], 1, null, new long[] { nP[i] }, null, 0, null, null);
			}
			for (int i = 0; i < d; i++) {
				CL.clFinish(command_queues[i]);
			}
			
			for (int i = 0; i < d; i++) {
				CL.clEnqueueReadBuffer(command_queues[i], xa_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * nP[i], Pointer.to(arrXa[i]), 0, null, null);
				CL.clEnqueueReadBuffer(command_queues[i], ya_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * nP[i], Pointer.to(arrYa[i]), 0, null, null);
			}
			
			if (useViewer) {
				viewer.PaintParticleView(n, arrX, arrY);
			}
			
			//Integrate
			for (int i = 0; i < d; i++) {
				for (int j = 0; j < nP[i]; j++) {
					arrXv[nPScan[i] + j] += arrXa[i][j];
					arrYv[nPScan[i] + j] += arrYa[i][j];
					
					arrX[nPScan[i] + j] += arrXv[nPScan[i] + j];
					arrY[nPScan[i] + j] += arrYv[nPScan[i] + j];
				}
			}
			
			//Write results back to device buffers
			for (int i = 0; i < d; i++) {
				CL.clEnqueueWriteBuffer(command_queues[i], x_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrX), 0, null, null);
				CL.clEnqueueWriteBuffer(command_queues[i], y_mem_objs[i], CL.CL_TRUE, 0, Sizeof.cl_float * n, Pointer.to(arrY), 0, null, null);
			}		
			
		}
		long after = System.nanoTime();
		double duration =((after-before)/(1e9f));
		
		//Clean up
		for (int i = 0; i < d; i++) { CL.clFlush(command_queues[i]); }
		for (int i = 0; i < d; i++) { CL.clFinish(command_queues[i]); }
		for (int i = 0; i < d; i++) { CL.clReleaseKernel(kernels[i]); }
		for (int i = 0; i < d; i++) { CL.clReleaseProgram(programs[i]); }
		for (int i = 0; i < d; i++) {
			CL.clReleaseMemObject(x_mem_objs[i]);
			CL.clReleaseMemObject(y_mem_objs[i]);
			CL.clReleaseMemObject(m_mem_objs[i]);
			CL.clReleaseMemObject(xa_mem_objs[i]);
			CL.clReleaseMemObject(ya_mem_objs[i]); 
		}
		for (int i = 0; i < d; i++) { CL.clReleaseCommandQueue(command_queues[i]); }
		for (int i = 0; i < d; i++) { CL.clReleaseContext(contexts[i]); }
		if(useViewer)
			viewer.dispose();
		
		//Print Info
		System.out.println("Duration: \t\t\t" + ((int)(Math.round(duration*10.0))/10.0) + " s");
		System.out.println("AVG Computes Per Second:\t" + ((int)(Math.round(TimesToRun/(duration/100.0)))/100.0));
	}
	
	private static void RunJava()
	{
		long before, after;
		
		generateDisk(new int[] {n});
		
		if (useViewer)
			viewer = new ParticleView("Java Compute Window");
		
		
		//Start Java Benchmark
		System.out.println("\nStart Java Single Thread Compute");
		before = System.nanoTime();
	
		int TimesToRun = Math.round(Seconds);
		
		for (int timesRun = 0; timesRun < TimesToRun; timesRun++)
		{
			float arrA[] = new float[]{0.0f,0.0f,0.0f};
			for (int gid = 0; gid < n; gid++)
			{
				float distMag;
				float VectorG;
				arrA = new float[]{0.0f,0.0f}; 
				
				for (int  i= 0; i < n; i++)
				{
					distMag = ((float)Math.sqrt(((arrX[gid] - arrX[i])*(arrX[gid] - arrX[i])) + ((arrY[gid] - arrY[i])*(arrY[gid] - arrY[i]))))+0.00001f;
					
					
					VectorG = arrM[i] * (1.0f/(distMag*distMag));
					arrA[0] += VectorG * (arrX[i] - arrX[gid]);
					arrA[1] += VectorG * (arrY[i] - arrY[gid]);
					
					
				}
				arrXv[gid] += arrA[0];
				arrYv[gid] += arrA[1];
			}
			
			//Put something here showing progress...
			if ((timesRun+1)%(TimesToRun*0.01) == 0)
			{
				System.out.print((int)((float)(timesRun+1)*100/TimesToRun) + "%  ");
			}
			
			for (int gid = 0; gid < n; gid++)
			{
				arrX[gid] += arrXv[gid];
				arrY[gid] += arrYv[gid];
			}
			
			if (useViewer)
				viewer.PaintParticleView(n, arrX, arrY);
		}
		after = System.nanoTime();
		float duration = (after-before)/(1e9f);
		
		if(useViewer)
			viewer.dispose();
		
		//Print Info
		System.out.println("\nDuration: \t\t\t" + ((int)(Math.round(duration*10.0))/10.0) + " s");
		System.out.println("AVG Computes Per Second:\t" + ((int)(Math.round(TimesToRun/(duration/100.0)))/100.0));
	}
	
	public static void main(String[] args) throws Exception {
		in = new Scanner(System.in);
		
		while (true) {
			System.out.println("\n[-1]: Quit");
			System.out.println("[0]: Single Device");
			System.out.println("[1]: Single Device, Local Memory");
			System.out.println("[2]: Multi Device");
			System.out.println("[3]: Java");
			
			int choice = in.nextInt();
			
			//TimeUnit.SECONDS.sleep(1);
			
			if (choice == -1) {
				System.out.println("Goodbye!");
				break;
			}
			else if (choice == 0) {
				SingleCL();
			}
			else if (choice == 1) {
				SingleSharedCL();
			}
			else if (choice == 2) {
				MultiDeviceCL();
			}
			else if (choice == 3) {
				MultiDeviceCL();
			}
		}
		in.close();
		
	}

}
