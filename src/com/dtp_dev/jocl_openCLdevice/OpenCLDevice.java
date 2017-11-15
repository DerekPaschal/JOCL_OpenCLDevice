package com.dtp_dev.jocl_openCLdevice;

import static org.jocl.CL.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	
	//Hidden
	
	//Readable
	private cl_device_id device_id;
	private cl_platform_id platform_id;
	private String deviceName;
	private String platformName;
	private int computeUnits;
	private long clockSpeed;
	private long deviceType;
	private boolean doubleSupport;
	private long[] maxWorkSizes;
	
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
		set_maxWorkSizes();
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
	public long getDeviceType()
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
	
	/**
	 * 
	 * @return
	 */
	public long[] getMaxWorkSizes() {
		return this.maxWorkSizes;
	}
	
	// Protected Methods
	
	
	
	// Private Methods
	
	private void set_deviceName()
	{
		this.deviceName = getString(this.device_id, CL.CL_DEVICE_NAME);
	}
	
	private void set_platformName()
	{
		this.platformName = getString(this.platform_id, CL.CL_PLATFORM_NAME);
	}
	
	private void set_computeUnits()
	{
		this.computeUnits = getInt(this.device_id, CL.CL_DEVICE_MAX_COMPUTE_UNITS);
	}
	
	private void set_clockSpeed()
	{
		this.clockSpeed = getInt(this.device_id, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
	}
	
	private void set_deviceType()
	{
		this.deviceType = getLong(this.device_id, CL.CL_DEVICE_TYPE);
	}
	
	private void set_doubleSupport()
	{
		this.doubleSupport = (getInt(this.device_id, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE) > 0);
	}
	
	private void set_maxWorkSizes() {
		int dim = getInt(this.device_id, CL.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
		
		this.maxWorkSizes = getSizes(this.device_id, CL.CL_DEVICE_MAX_WORK_ITEM_SIZES, dim);
	}
	
	
	
	
	
	
	//Utility Functions to get different return types from OpenCL API
	/**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static int getInt(cl_device_id device, int paramName)
    {
        return getInts(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static int[] getInts(cl_device_id device, int paramName, int numValues)
    {
        int values[] = new int[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getLong(cl_device_id device, int paramName)
    {
        return getLongs(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static long[] getLongs(cl_device_id device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_device_id device, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_platform_id platform, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }
    
    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getSize(cl_device_id device, int paramName)
    {
        return getSizes(device, paramName, 1)[0];
    }
    
    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    static long[] getSizes(cl_device_id device, int paramName, int numValues)
    {
        // The size of the returned data has to depend on 
        // the size of a size_t, which is handled here
        ByteBuffer buffer = ByteBuffer.allocate(
            numValues * Sizeof.size_t).order(ByteOrder.nativeOrder());
        clGetDeviceInfo(device, paramName, Sizeof.size_t * numValues, 
            Pointer.to(buffer), null);
        long values[] = new long[numValues];
        if (Sizeof.size_t == 4)
        {
            for (int i=0; i<numValues; i++)
            {
                values[i] = buffer.getInt(i * Sizeof.size_t);
            }
        }
        else
        {
            for (int i=0; i<numValues; i++)
            {
                values[i] = buffer.getLong(i * Sizeof.size_t);
            }
        }
        return values;
    }
}
