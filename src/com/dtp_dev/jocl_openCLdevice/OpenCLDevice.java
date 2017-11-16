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
	private cl_platform_id platform_id;
	private cl_device_id device_id;
	
	private String platformName;
	private String deviceName;
	private String deviceVendor;
	private String driverVersion;
	private long deviceType;
	private int maxComputeUnits;
	private long maxWorkItemDimensions;
	private long[] maxWorkItemSizes;
	private long maxWorkGroupSize;
	private long maxClockFrequency;
	private int addressBits;
	private long maxMemAllocSize;
	private long globalMemSize;
	private int errorCorrectionSupport;
	private int localMemType;
	private long localMemSize;
	private long maxConstantBufferSize;
	private long queueProperties;
	private int imageSupport;
	private int maxReadImageArgs;
	private int maxWriteImageArgs;
	private long singleFpConfig;
	private long image2dMaxWidth;
	private long image2dMaxHeight;
	private long image3dMaxWidth;
	private long image3dMaxHeight;
	private long image3dMaxDepth;
	private int preferredVectorWidthChar;
	private int preferredVectorWidthShort;
	private int preferredVectorWidthInt;
	private int preferredVectorWidthLong;
	private int preferredVectorWidthFloat;
	private int preferredVectorWidthDouble;
	
	
	
	//Readable + Writable
	
	//Direct
	
	@SuppressWarnings("deprecation")
	protected OpenCLDevice(cl_device_id device, cl_platform_id platform)
	{
		device_id = device;
		platform_id = platform;
		
		this.platformName = getString(platform, CL.CL_PLATFORM_NAME);
        this.deviceName = getString(device, CL.CL_DEVICE_NAME);
        this.deviceVendor = getString(device, CL.CL_DEVICE_VENDOR);
        this.driverVersion = getString(device, CL.CL_DRIVER_VERSION);
        this.deviceType = getLong(device, CL.CL_DEVICE_TYPE);
        this.maxComputeUnits = getInt(device, CL.CL_DEVICE_MAX_COMPUTE_UNITS);      
        this.maxWorkItemDimensions = getLong(device, CL.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);     
        this.maxWorkItemSizes = getSizes(device, CL.CL_DEVICE_MAX_WORK_ITEM_SIZES, (int) this.maxWorkItemDimensions);
        this.maxWorkGroupSize = getSize(device, CL.CL_DEVICE_MAX_WORK_GROUP_SIZE);
        this.maxClockFrequency = getLong(device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
        this.addressBits = getInt(device, CL.CL_DEVICE_ADDRESS_BITS);
        this.maxMemAllocSize = getLong(device, CL.CL_DEVICE_MAX_MEM_ALLOC_SIZE);
        this.globalMemSize = getLong(device, CL.CL_DEVICE_GLOBAL_MEM_SIZE);
        this.errorCorrectionSupport = getInt(device, CL.CL_DEVICE_ERROR_CORRECTION_SUPPORT);
        this.localMemType = getInt(device, CL.CL_DEVICE_LOCAL_MEM_TYPE);
        this.localMemSize = getLong(device, CL.CL_DEVICE_LOCAL_MEM_SIZE);
        this.maxConstantBufferSize = getLong(device, CL.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
        this.queueProperties = getLong(device, CL.CL_DEVICE_QUEUE_PROPERTIES);
        this.imageSupport = getInt(device, CL.CL_DEVICE_IMAGE_SUPPORT);
        this.maxReadImageArgs = getInt(device, CL.CL_DEVICE_MAX_READ_IMAGE_ARGS);
        this.maxWriteImageArgs = getInt(device, CL.CL_DEVICE_MAX_WRITE_IMAGE_ARGS);
        this.singleFpConfig = getLong(device, CL.CL_DEVICE_SINGLE_FP_CONFIG);
        this.image2dMaxWidth = getSize(device, CL.CL_DEVICE_IMAGE2D_MAX_WIDTH);
        this.image2dMaxHeight = getSize(device, CL.CL_DEVICE_IMAGE2D_MAX_HEIGHT);
        this.image3dMaxWidth = getSize(device, CL.CL_DEVICE_IMAGE3D_MAX_WIDTH);
        this.image3dMaxHeight = getSize(device, CL.CL_DEVICE_IMAGE3D_MAX_HEIGHT);
        this.image3dMaxDepth = getSize(device, CL.CL_DEVICE_IMAGE3D_MAX_DEPTH);
        this.preferredVectorWidthChar = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR);
        this.preferredVectorWidthShort = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT);
        this.preferredVectorWidthInt = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT);
        this.preferredVectorWidthLong = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG);
        this.preferredVectorWidthFloat = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT);
        this.preferredVectorWidthDouble = getInt(device, CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE);
       
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
	
	
	
	public cl_platform_id platform_id() {
		return this.platform_id;
	}

	public cl_device_id device_id() {
		return this.device_id;
	}

	public String platformName()
	{
		return this.platformName;
	}

	public String deviceName()
	{
		return this.deviceName;
	}
	
	public String deviceVendor() {
		return this.deviceVendor;
	}
	
	public String driverVersion() {
		return this.driverVersion;
	}
	
	public long deviceType() {
		return this.deviceType;
	}
	
	public int maxComputeUnits() {
		return this.maxComputeUnits;
	}
	
	public long maxWorkItemDimensions() {
		return this.maxWorkItemDimensions();
	}
	
	public long[] maxWorkItemSizes() {
		return this.maxWorkItemSizes;
	}
	
	public long maxWorkGroupSize() {
		return this.maxWorkGroupSize;
	}
	
	public long maxClockFrequency() {
		return this.maxClockFrequency;
	}
	
	public int addressBits() {
		return this.addressBits;
	}
	
	public long maxMemAllocSize() {
		return this.maxMemAllocSize;
	}
	
	public long globalMemSize() {
		return this.globalMemSize;
	}
	
	public int errorCorrectionSupport() {
		return this.errorCorrectionSupport;
	}
	
	public int localMemType() {
		return this.localMemType;
	}
	
	public long localMemSize() {
		return this.localMemSize;
	}
	
	public long maxConstantBufferSize() {
		return this.maxConstantBufferSize;
	}
	
	public long queueProperties() {
		return this.queueProperties;
	}
	
	public int imageSupport() {
		return this.imageSupport;
	}
	
	public int maxReadImageArgs() {
		return this.maxReadImageArgs;
	}
	
	public int maxWriteImageArgs( ) {
		return this.maxWriteImageArgs;
	}
	
	public long singleFpConfig() {
		return this.singleFpConfig;
	}
	
	public long image2dMaxWidth() {
		return this.image2dMaxWidth;
	}
	
	public long image2dMaxHeight() {
		return this.image2dMaxHeight;
	}
	
	public long image3dMaxWidth() {
		return this.image3dMaxWidth;
	}
	
	public long image3dMaxHeight() {
		return this.image3dMaxHeight;
	}
	
	public long image3dMaxDepth() {
		return this.image3dMaxDepth;
	}
	
	public int preferredVectorWidthChar() {
		return this.preferredVectorWidthChar;
	}
	
	public int preferredVectorWidthShort() {
		return this.preferredVectorWidthShort;
	}
	
	public int preferredVectorWidthInt() {
		return this.preferredVectorWidthInt;
	}
	
	public int preferredVectorWidthLong() {
		return this.preferredVectorWidthLong;
	}
	
	public int preferredVectorWidthFloat() {
		return this.preferredVectorWidthFloat;
	}
	
	public int preferredVectorWidthDouble() {
		return this.preferredVectorWidthDouble;
	}
	
	// Protected Methods
	
	
	
	// Private Methods	
	
	
	
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
