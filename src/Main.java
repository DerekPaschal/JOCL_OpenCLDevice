import static org.jocl.CL.clCreateCommandQueueWithProperties;
import static org.jocl.CL.clCreateContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;

import com.dtp_dev.jocl_openCLdevice.OpenCLDevice;
import com.dtp_dev.jocl_openCLdevice.OpenCLDevice.DeviceType;

/**
 * Test main class for JOCL Wrapper
 * @author Derek Paschal
 */
public class Main 
{
	final static boolean useViewer = true;
	
	
	static int n= (int)(Math.pow(2, 15)); //Massive Particles
	
	static float arrX[];
	static float arrY[];
	static float arrZ[];
	
	static float arrXv[];
	static float arrYv[];
	static float arrZv[];
	
	static float arrXa[] = new float[n];
	static float arrYa[] = new float[n];
	static float arrZa[] = new float[n];
	
	static float arrM[];
	
	
	static float Seconds = (float)500.0;
	
	static ParticleView viewer;
	
	
	private static void setupArrs()
	{			
		arrX = new float[n];
		arrY = new float[n];
		arrZ = new float[n];
		
		arrXv = new float[n];
		arrYv = new float[n];
		arrZv = new float[n];
		
		arrM = new float[n];
		
		generateDisk();
		//generateSphere();
		
	}
	
	private static void generateSphere()
	{
		float radius = 400;
		float theta;
		float phi;
		float r;
		float u;
		float costheta;
		for (int i = 0; i < n; i++)
		{			
			phi = (float) (Math.random()*3.14*2);
			costheta = (float) ((Math.random()*2.0)-1.0);
			u = (float) Math.random();
			theta = (float) Math.acos(costheta);
			r = (float) (radius * Math.cbrt(u));
			
			arrX[i] = (float) (r*Math.sin(theta)*Math.cos(phi));
			arrY[i] = (float) (r*Math.sin(theta)*Math.sin(phi));
			arrZ[i] = (float) (r*Math.cos(theta));		
			
			arrM[i] = (float) ((0.001) + ((Math.random()-0.5)*(0.0001)));
		}
	}
	
	private static void generateDisk()
	{
		float radius = 100;
		float theta;
		float r;
		float centerX = 0.0f, centerY = 0.0f, centerZ = 0.0f;
		float avgMass = 0.00001f;
		float totalMass = avgMass*n;
		float totalVel = 0.0f;
		for (int i = 0; i < n; i++)
		{						
			//r = (float) (Math.random() * radius);
			r = (float) Math.sqrt(Math.random()*radius*radius);
			theta = (float) (Math.random() * 6.28318);
			arrX[i] = (float) (r * Math.cos(theta))+centerX;
			arrY[i] = (float) (r * Math.sin(theta))+centerY;
			arrZ[i] = centerZ;
			
			arrM[i] = (float) (((avgMass)) + ((Math.random()-0.5)*(0.1*avgMass)));
			
			//totalVel = (float) (r*Math.sqrt(totalMass/(radius*radius)));
			
			//arrXv[i] = (float) (arrY[i]/r * totalVel );
			//arrYv[i] = (float) (-arrX[i]/r * totalVel );
		}
		
	}

	
	private static void RunOpenCL(DeviceType ComputeType) throws Exception
	{
		/**
	     * The source code of the OpenCL program to execute
	     */
		String programSource = new String(Files.readAllBytes(Paths.get("kernels/Test4.cl")), StandardCharsets.UTF_8);
		
		setupArrs();
		
		//OpenCL
		//Create device manager and find optimal device
		OpenCLDevice[] devices = OpenCLDevice.getOpenCLDevices();
		OpenCLDevice device = null;
		for (OpenCLDevice tempDevice : devices) {
						
			if (tempDevice == null) {
				continue;
			}
			
			//System.out.println(tempDevice.getPlatformName() + " " + tempDevice.getDeviceType());
			
			if (device == null) {
				device = tempDevice;
				continue;
			}
			
			if (tempDevice.getDeviceType() == OpenCLDevice.DeviceType.GPU && tempDevice.getPlatformName().toLowerCase().trim().contains("intel")) {
				continue;
			}
			
			if (tempDevice.getDeviceType() == ComputeType) {
				device = tempDevice;
				continue;
			}
		}		
		
		// Create a context
        cl_context context = clCreateContext(null, 1, new cl_device_id[]{ device.getDeviceID() }, null, null, null);

        // Create the command queue
        cl_command_queue command_queue = clCreateCommandQueueWithProperties(context, device.getDeviceID(), null, null);
        
        //Create memory buffers on device
        cl_mem x_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrX).length, null, null);
        cl_mem y_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrY).length, null, null);
        cl_mem z_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrZ).length, null, null);
        cl_mem xv_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrXv).length, null, null);
        cl_mem yv_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrYv).length, null, null);
        cl_mem zv_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrZv).length, null, null);
        cl_mem xa_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrXa).length, null, null);
        cl_mem ya_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrYa).length, null, null);
        cl_mem za_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrZa).length, null, null);
        cl_mem m_mem_obj = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE, Sizeof.cl_float * ((float[])arrM).length, null, null);
        
        //Copy the arrays x,y,z,m to their memory buffers
        CL.clEnqueueWriteBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrX).length, Pointer.to((float[])arrX), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrY).length, Pointer.to((float[])arrY), 0, null, null);
        CL.clEnqueueWriteBuffer(command_queue, z_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrZ).length, Pointer.to((float[])arrZ), 0, null, null);
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
        CL.clSetKernelArg(kernelGrav, 2, Sizeof.cl_mem, Pointer.to(z_mem_obj));
        CL.clSetKernelArg(kernelGrav, 3, Sizeof.cl_mem, Pointer.to(xa_mem_obj));
        CL.clSetKernelArg(kernelGrav, 4, Sizeof.cl_mem, Pointer.to(ya_mem_obj));
        CL.clSetKernelArg(kernelGrav, 5, Sizeof.cl_mem, Pointer.to(za_mem_obj));
        CL.clSetKernelArg(kernelGrav, 6, Sizeof.cl_mem, Pointer.to(m_mem_obj));
        CL.clSetKernelArg(kernelGrav, 7, Sizeof.cl_float4 * 1024, null);
        
        //Create the Step kernel
        cl_kernel kernelStep = CL.clCreateKernel(program, "Step", null);
        
        //Set Step kernel arguments
        CL.clSetKernelArg(kernelStep, 0, Sizeof.cl_mem, Pointer.to(x_mem_obj));
        CL.clSetKernelArg(kernelStep, 1, Sizeof.cl_mem, Pointer.to(y_mem_obj));
        CL.clSetKernelArg(kernelStep, 2, Sizeof.cl_mem, Pointer.to(z_mem_obj));
        CL.clSetKernelArg(kernelStep, 3, Sizeof.cl_mem, Pointer.to(xv_mem_obj));
        CL.clSetKernelArg(kernelStep, 4, Sizeof.cl_mem, Pointer.to(yv_mem_obj));
        CL.clSetKernelArg(kernelStep, 5, Sizeof.cl_mem, Pointer.to(zv_mem_obj));
        CL.clSetKernelArg(kernelStep, 6, Sizeof.cl_mem, Pointer.to(xa_mem_obj));
        CL.clSetKernelArg(kernelStep, 7, Sizeof.cl_mem, Pointer.to(ya_mem_obj));
        CL.clSetKernelArg(kernelStep, 8, Sizeof.cl_mem, Pointer.to(za_mem_obj));
        CL.clSetKernelArg(kernelStep, 9, Sizeof.cl_mem, Pointer.to(m_mem_obj));
        
		if (useViewer)
			viewer = new ParticleView("OpenCL Compute Window");
		
		int TimesToRun = Math.round(Seconds);
		
		//Begin the Compute Benchmark
		System.out.println("\nStart OpenCL Compute with: \t"+ device.getDeviceName());
		long before = System.nanoTime();
		for (int i = 0; i < TimesToRun; i++)
		{			
			CL.clEnqueueNDRangeKernel(command_queue, kernelGrav, 1, null, new long[] {n}, new long[] {1024}, 0, null, null);
			CL.clFinish(command_queue);
			
			CL.clEnqueueNDRangeKernel(command_queue, kernelStep, 1, null, new long[] {n}, null, 0, null, null);
			CL.clFinish(command_queue);
			
			CL.clEnqueueReadBuffer(command_queue, x_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrX).length, Pointer.to(arrX), 0, null, null);
			CL.clEnqueueReadBuffer(command_queue, y_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrY).length, Pointer.to(arrY), 0, null, null);
			CL.clEnqueueReadBuffer(command_queue, z_mem_obj, CL.CL_TRUE, 0, Sizeof.cl_float * ((float[])arrZ).length, Pointer.to(arrZ), 0, null, null);
			
			if (useViewer) {
				viewer.PaintParticleView(n, arrX, arrY);
			}
		}
		long after = System.nanoTime();
		double duration =((after-before)/(1e9f));
		
		//Clean up
		CL.clFlush(command_queue);
		CL.clFinish(command_queue);
		CL.clReleaseKernel(kernelGrav);
		CL.clReleaseKernel(kernelStep);
		CL.clReleaseProgram(program);
		CL.clReleaseMemObject(x_mem_obj);
		CL.clReleaseMemObject(y_mem_obj);
		CL.clReleaseMemObject(z_mem_obj);
		CL.clReleaseMemObject(xv_mem_obj);
		CL.clReleaseMemObject(yv_mem_obj);
		CL.clReleaseMemObject(zv_mem_obj);
		CL.clReleaseMemObject(xa_mem_obj);
		CL.clReleaseMemObject(ya_mem_obj);
		CL.clReleaseMemObject(za_mem_obj);
		CL.clReleaseMemObject(m_mem_obj);
		CL.clReleaseCommandQueue(command_queue);
		CL.clReleaseContext(context);
		if(useViewer)
			viewer.dispose();
		
		//Print Info
		System.out.println("Duration: \t\t\t" + ((int)(Math.round(duration*10.0))/10.0) + " s");
		System.out.println("AVG Computes Per Second:\t" + ((int)(Math.round(TimesToRun/(duration/100.0)))/100.0));
	}
	
	
	private static void RunJava()
	{
		long before, after;
		
		setupArrs();
		
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
				arrA = new float[]{0.0f,0.0f,0.0f}; 
				
				for (int  i= 0; i < n; i++)
				{
					distMag = ((float)Math.sqrt(((arrX[gid] - arrX[i])*(arrX[gid] - arrX[i])) + ((arrY[gid] - arrY[i])*(arrY[gid] - arrY[i]))+ ((arrZ[gid] - arrZ[i])*(arrZ[gid] - arrZ[i]))))+0.0000001f;
					
					
					VectorG = arrM[i] * (1.0f/(distMag*distMag));
					arrA[0] += VectorG * (arrX[i] - arrX[gid]);
					arrA[1] += VectorG * (arrY[i] - arrY[gid]);
					arrA[2] += VectorG * (arrZ[i] - arrZ[gid]);
					
					
				}
				arrXv[gid] += arrA[0];
				arrYv[gid] += arrA[1];
				arrZv[gid] += arrA[2];
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
				arrZ[gid] += arrZv[gid];
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

	
	public static void main(String[] args) throws Exception 
	{
		setupArrs();
		int DiffFactor = (int)Math.round((long)n*(long)n*0.00020493);
		System.out.println("Particles: "+n+" ; Timestep: "+ 1 +" ; Seconds: "+Seconds+" ;");
		System.out.println("Iterations: "+Math.round(Seconds/1)+" ; Computation Difficulty Factor: "+ DiffFactor);
		System.out.println("RunTime Factor: "+ (long)(DiffFactor*0.001*(long)Math.round(Seconds/1)));
	
		TimeUnit.SECONDS.sleep(2);
		
		RunOpenCL(DeviceType.GPU);
		
		//TimeUnit.SECONDS.sleep(2);
		
		//RunOpenCL(DeviceType.CPU);
		
		//TimeUnit.SECONDS.sleep(2);
		
		//RunJava();
		
	}

}
