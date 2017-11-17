package com.dtp_dev.jocl_utilities;

import static org.jocl.CL.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
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
public class CLDevice 
{	
	
	//Hidden
	
	//Readable
	private cl_platform_id platform_id;
	private cl_device_id device_id;
	
	//Readable + Writable
	
	//Direct
	
	/**
	 * 
	 * @param _device
	 * @param _platform
	 */
	protected CLDevice(cl_device_id _device, cl_platform_id _platform)
	{
		device_id = _device;
		platform_id = _platform;       
	}
	
	// Public Methods
	
	/**
	 * 
	 * @return
	 */
	public cl_platform_id platform_id() {
		return this.platform_id;
	}

	/**
	 * 
	 * @return
	 */
	public cl_device_id device_id() {
		return this.device_id;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	static public CLDevice[] getOpenCLDevices() throws Exception
	{
		CL.setExceptionsEnabled(CL_TRUE);
		
		CLDevice[] tempCLDevices = new CLDevice[100];
		
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
					
				tempCLDevices[devicesFound] = (new CLDevice(platformDevices[j], platforms[i]));
				devicesFound++;
			}
		}
        
        if (devicesFound < 1)
        {
        	throw new Exception("System Contains No Valid OpenCL Devices.");
        }
        
        CLDevice[] CLDevices = new CLDevice[devicesFound];
        for (int i = 0; i < devicesFound; i++) {
        	CLDevices[i] = tempCLDevices[i];
        }
        
		return CLDevice.getUniqueDevices(CLDevices);
	}
	
	/**
	 * 
	 * @param devices
	 * @return
	 */
	public static CLDevice[] getUniqueDevices(CLDevice[] devices) {
		
		CLDevice[] unique_devices;
		
		//Find how many unique devices in devices and where they are
		int unique_count = 0;
		boolean[] unique_device_locations = new boolean[devices.length];
		Arrays.fill(unique_device_locations, true);
		for (int i = 0; i < devices.length; i++) {
			for(int j = 0; j < i; j++) {
				if (devices[i] == null || devices[i].equals(devices[j])) {
					unique_device_locations[i] = false;
					break;
				}
			}
			if (unique_device_locations[i]) {
				unique_count++;
			}
		}
		
		//fill unique_devices with unique devices
		unique_devices = new CLDevice[unique_count];
		int found = 0;
		for(int i = 0; found < unique_count; i++) {
			if (unique_device_locations[i]) {
				unique_devices[found] = devices[i];
				found++;
			}
		}

		return unique_devices;
	}
	
	
	
	//Utility Functions to get different return types from OpenCL API
	/**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static int getDeviceInt(CLDevice device, int paramName)
    {
        return getDeviceInts(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    public static int[] getDeviceInts(CLDevice device, int paramName, int numValues)
    {
        int values[] = new int[numValues];
        clGetDeviceInfo(device.device_id, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static long getDeviceLong(CLDevice device, int paramName)
    {
        return getDeviceLongs(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    public static long[] getDeviceLongs(CLDevice device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device.device_id, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static String getDeviceString(CLDevice device, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device.device_id, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device.device_id, paramName, buffer.length, Pointer.to(buffer), null);

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
    public static String getPlatformString(CLDevice device, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(device.platform_id, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(device.platform_id, paramName, buffer.length, Pointer.to(buffer), null);

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
    public static long getDeviceSize(CLDevice device, int paramName)
    {
        return getDeviceSizes(device, paramName, 1)[0];
    }
    
    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    public static long[] getDeviceSizes(CLDevice device, int paramName, int numValues)
    {
        // The size of the returned data has to depend on 
        // the size of a size_t, which is handled here
        ByteBuffer buffer = ByteBuffer.allocate(
            numValues * Sizeof.size_t).order(ByteOrder.nativeOrder());
        clGetDeviceInfo(device.device_id, paramName, Sizeof.size_t * numValues, 
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
	
	
	// Protected Methods
	
	
	
	// Private Methods	
	
	
}
