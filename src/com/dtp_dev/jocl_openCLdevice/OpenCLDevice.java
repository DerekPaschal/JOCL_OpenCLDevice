package com.dtp_dev.jocl_openCLdevice;

import static org.jocl.CL.*;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/**
 *  _____   ______  _____       ____    ____    __  __    
 * /\  __ \/\__  _\/\  _  \    /\  _ \ /\  __\ /\ \/\ \   
 * \ \ \/\ \/_/\ \/\ \ \_\ \   \ \ \/\ \ \ \___\ \ \ \ \  
 *  \ \ \ \ \ \ \ \ \ \  __/    \ \ \ \ \ \  __\\ \ \ \ \ 
 *   \ \ \_\ \ \ \ \ \ \ \/      \ \ \_\ \ \ \__ \ \ \_/ \
 *    \ \____/  \ \_\ \ \_\       \ \____/\ \____\\ \____/
 *     \/___/    \/_/  \/_/        \/___/  \/____/ \/___/ 
 * 
 */

/**
 * OpenCLDevice is a user friendly way for developers using jocl to find OpenCL compatible devices on a system.
 *
 * @author Derek Paschal
 *
 */
public class OpenCLDevice 
{	
	public enum DeviceType{CPU, GPU, ACCELERATOR};
	
	//Hidden
	
	//Readable
	private cl_device_id device_id;
	private cl_platform_id platform_id;
	private String deviceName;
	private String platformName;
	private int computeUnits;
	private long clockSpeed;
	private DeviceType deviceType;
	private boolean doubleSupport;
	
	//Readable + Writable
	
	//Direct
	
	protected OpenCLDevice(cl_device_id device, cl_platform_id platform)
	{
		device_id = device;
		platform_id = platform;
		
		set_deviceName();
		set_platformName();
		set_computeUnits();
		set_clockSpeed();
		set_deviceType();
		set_doubleSupport();
	}
	
	// Public Methods
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	static public OpenCLDevice[] getOpenCLDevices() throws Exception
	{
		CL.setExceptionsEnabled(CL_TRUE);
		
		OpenCLDevice[] OpenCLDevices = new OpenCLDevice[100];
		
		// Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        
        
        // Populate array of platforms
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Iterate over every platform
        int devicesFound = 0;
        for (int i = 0; i < numPlatforms; i++)
		{     
        	if (platforms[i] == null)
        		continue;
        	
        	// Obtain the number of devices for the platform
			int numDevicesArray[] = new int[1];
			try{clGetDeviceIDs(platforms[i], CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);} catch (Exception ex){}
			int numDevices = numDevicesArray[0];
			
			// Obtain the all device IDs for this platform
			cl_device_id platformDevices[] = new cl_device_id[numDevices];
			try{clGetDeviceIDs(platforms[i], CL.CL_DEVICE_TYPE_ALL, numDevices, platformDevices, null);} catch (Exception ex){}
			
			
			// Iterate over every device in platform
			for (int j = 0; j < numDevices; j++)
			{
				if (platformDevices[j] == null)
					continue;
					
				OpenCLDevices[devicesFound] = (new OpenCLDevice(platformDevices[j], platforms[i]));
				devicesFound++;
			}
		}
        
        if (devicesFound < 1)
        {
        	throw new Exception("System Contains No Valid OpenCL Devices.");
        }
        
		return OpenCLDevices;
	}
	
	/**
	 * 
	 * @return
	 */
	public cl_device_id getDeviceID() {
		return this.device_id;
	}
	
	/**
	 * 
	 * @return
	 */
	public cl_platform_id getPlatformID() {
		return this.platform_id;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDeviceName()
	{
		return this.deviceName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPlatformName()
	{
		return this.platformName;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getComputeUnits()
	{
		return this.computeUnits;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getClockSpeed()
	{
		return this.clockSpeed;
	}
	
	/**
	 * 
	 * @return
	 */
	public DeviceType getDeviceType()
	{
		return this.deviceType;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean getDoubleSupport()
	{
		return this.doubleSupport;
	}
	
	// Protected Methods
	
	
	
	// Private Methods
	
	private void set_deviceName()
	{
		long size[] = new long[1];
        clGetDeviceInfo(this.device_id, CL_DEVICE_NAME, 0, null, size);
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(this.device_id, CL_DEVICE_NAME, buffer.length, Pointer.to(buffer), null);
        this.deviceName = new String(buffer, 0, buffer.length-1);
	}
	
	private void set_platformName()
	{
		// Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(this.platform_id, CL_PLATFORM_NAME, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(this.platform_id, CL_PLATFORM_NAME, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        this.platformName = new String(buffer, 0, buffer.length-1);
	}
	
	private void set_computeUnits()
	{
		int values[] = new int[1];
        clGetDeviceInfo(this.device_id, CL_DEVICE_MAX_COMPUTE_UNITS, Sizeof.cl_int * 1, Pointer.to(values), null);
        this.computeUnits = values[0];
	}
	
	private void set_clockSpeed()
	{
		long values[] = new long[1];
        clGetDeviceInfo(this.device_id, CL_DEVICE_MAX_CLOCK_FREQUENCY, Sizeof.cl_long * 1, Pointer.to(values), null);
        this.clockSpeed = values[0];
	}
	
	private void set_deviceType()
	{
		long values[] = new long[1];
        clGetDeviceInfo(this.device_id, CL.CL_DEVICE_TYPE, Sizeof.cl_long * 1, Pointer.to(values), null);
        long Type = values[0];
        
        if( (Type & CL_DEVICE_TYPE_CPU) != 0)
            this.deviceType = DeviceType.CPU;
        else if( (Type & CL_DEVICE_TYPE_GPU) != 0)
        	this.deviceType = DeviceType.GPU;
        else if( (Type & CL_DEVICE_TYPE_ACCELERATOR) != 0)
        	this.deviceType = DeviceType.ACCELERATOR;
        else 
        	this.deviceType = DeviceType.CPU;
	}
	
	private void set_doubleSupport()
	{
		int values[] = new int[1];
        clGetDeviceInfo(this.device_id, CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE, Sizeof.cl_int * 1, Pointer.to(values), null);    	
    	this.doubleSupport = (values[0] > 0);
	}
}
