package com.mosip.io.pmp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mosip.io.util.DeviceRegisterException;
import com.mosip.io.util.Util;

public class Runner extends Util {
	public static Map<String, String> prop = null;

	public static void main(String[] args) {
		Runner runner = new Runner();
		runner.run();
	}

	public void run() {
		String deviceCode=null;
		ProcessStep runner = new ProcessStep();
		String type = System.getProperty("type");
		Map<String,String> deviceTypeAndDeviceCode= new HashMap<>();
		if (type == null || type.isEmpty()) {
			throw new DeviceRegisterException("Type cannot be null please provide Type value from VM argument !!!");
		} else if (type.equalsIgnoreCase("Face") || type.equalsIgnoreCase("Iris") || type.equalsIgnoreCase("Finger")
				|| type.equalsIgnoreCase("Auth")) {
			prop = loadDataFromCsv(type);
			if (prop == null || prop.isEmpty())
				throw new DeviceRegisterException("No data found in deviceData file for : " + type);
			logInfo("**********REGISTERING DEVICE :" + type + "************");
			deviceCode=runner.process(type, prop);
			if(!(deviceCode.equals("ADM-DPM-001"))){
				deviceTypeAndDeviceCode.put(type, deviceCode);
				runner.deRegisterDevice(deviceTypeAndDeviceCode);
			}else
				logInfo("Deivce already De-Registered");
		} else if (type.equalsIgnoreCase("All")) {
			List<String> typeList = Arrays.asList("Face", "Iris", "Finger", "Auth");
			for (int i = 0; i < typeList.size(); i++) {
				prop = loadDataFromCsv(typeList.get(i));
				if (prop == null || prop.isEmpty())
					throw new DeviceRegisterException("No data found in deviceData file for : " + typeList.get(i));
				logInfo("**********REGISTERING DEVICE :" + typeList.get(i) + "************");
				deviceCode=runner.process(typeList.get(i), prop);
				if(!(deviceCode.equals("ADM-DPM-001")))
				deviceTypeAndDeviceCode.put(typeList.get(i), deviceCode);
			}
			runner.deRegisterDevice(deviceTypeAndDeviceCode);
		} else {
			String[] dtypes = type.split(",");
			for (int i = 0; i < dtypes.length; i++) {
				prop = loadDataFromCsv(dtypes[i]);
				if (prop == null || prop.isEmpty())
					throw new DeviceRegisterException("No data found in deviceData file for : " + dtypes[i]);
				logInfo("**********REGISTERING DEVICE :" + dtypes[i] + "************");
				deviceCode=runner.process(dtypes[i], prop);
				if(!(deviceCode.equals("ADM-DPM-001")))
				deviceTypeAndDeviceCode.put(dtypes[i], deviceCode);
			}
			runner.deRegisterDevice(deviceTypeAndDeviceCode);
		}
	}

}
