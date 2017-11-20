package com.dtp_dev.jocl_utilities;

import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetDeviceInfo;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clGetPlatformInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
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
 * CLUtil is full of user friendly utility functions for developers using jocl.
 *
 * @author Derek Paschal
 *
 */
public class CLUtil {
	
	
	
	//PLATFORM UTILITIES
	
	/**
	 * 
	 * @return
	 */
	static public cl_platform_id[] getOpenCLPlatforms() {
		
		// Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        
        
        // Populate array of platforms
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
		
		return platforms;
	}
	
	/**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    public static String getPlatformString(cl_platform_id platform, int paramName)
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
	
	
    
    //DEVICE UTILITIES
    
	/**
	 * 
	 * @param platform
	 * @return
	 */
	static public cl_device_id[] getOpenCLDevices(cl_platform_id platform) {
		
		if (platform == null)
    		return null;
    	
    	// Obtain the number of devices for the platform
		int numDevicesArray[] = new int[1];
		try{clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);} catch (Exception ex){}
		int numDevices = numDevicesArray[0];
		
		// Obtain the all device IDs for this platform
		cl_device_id platformDevices[] = new cl_device_id[numDevices];
		try{clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, numDevices, platformDevices, null);} catch (Exception ex){}
		
		
		return platformDevices;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	static public cl_device_id[] getAllOpenCLDevices() throws Exception
	{
		cl_device_id[] tempCLDevices = new cl_device_id[100];
		
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
					
				tempCLDevices[devicesFound] = platformDevices[j];
				devicesFound++;
			}
		}
        
        cl_device_id[] CLDevices = new cl_device_id[devicesFound];
        for (int i = 0; i < devicesFound; i++) {
        	CLDevices[i] = tempCLDevices[i];
        }
        
		return CLDevices;
	}
	
	/**
	 * 
	 * @param devices
	 * @return
	 */
	public static cl_device_id[] getUniqueDevices(cl_device_id[] devices) {
		
		cl_device_id[] unique_devices;
		
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
		unique_devices = new cl_device_id[unique_count];
		int found = 0;
		for(int i = 0; found < unique_count; i++) {
			if (unique_device_locations[i]) {
				unique_devices[found] = devices[i];
				found++;
			}
		}

		return unique_devices;
	}
	
	public static long getMaxDeviceLocalSizeFromLocalMem(cl_device_id device, long fixedLocalMemAlloc, double localMemAllocGrowthRate) {        	
        return (long) ((CLUtil.getDeviceLong(device, CL.CL_DEVICE_LOCAL_MEM_SIZE) - fixedLocalMemAlloc) / localMemAllocGrowthRate);
	}
	
	/**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static int getDeviceInt(cl_device_id device, int paramName)
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
    public static int[] getDeviceInts(cl_device_id device, int paramName, int numValues)
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
    public static long getDeviceLong(cl_device_id device, int paramName)
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
    public static long[] getDeviceLongs(cl_device_id device, int paramName, int numValues)
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
    public static String getDeviceString(cl_device_id device, int paramName)
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
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static long getDeviceSize(cl_device_id device, int paramName)
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
    public static long[] getDeviceSizes(cl_device_id device, int paramName, int numValues)
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
	
    
    
    //KERNEL UTILITIES
       
    public static long getKernelSize(cl_device_id device, cl_kernel kernel, int paramName)
    {
        return getKernelSizes(device, kernel, paramName, 1)[0];
    }
    
   
    public static long[] getKernelSizes(cl_device_id device, cl_kernel kernel, int paramName, int numValues)
    {
    	ByteBuffer buffer = ByteBuffer.allocate(1 * Sizeof.size_t).order(ByteOrder.nativeOrder());
        CL.clGetKernelWorkGroupInfo(kernel, device, paramName, Sizeof.size_t, Pointer.to(buffer), null);
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
