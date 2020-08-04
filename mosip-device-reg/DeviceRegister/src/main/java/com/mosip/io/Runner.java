package com.mosip.io;

import java.util.ArrayList;
import java.util.List;

import com.mosip.io.util.Util;

public class Runner extends Util {

	public static void main(String[] args) {
		Util.setupLogger();
		Runner runner = new Runner();
		runner.process();
	}

	public void process() {
		// System.out.println("****Authentication********");
		auditLog.info("****Authentication********");
		Authentication auth = new Authentication();
		auth.login();

		// 1. checking DeviceProvider and DeviceProviderHistory table for providerID is
		// present or not
		auditLog.info("****Checking providerID in  DeviceProvider and DeviceProviderHistory table********");
		List<String> providerList = new ArrayList<>();
		Dprovider dprovider = new Dprovider();
		if (!dprovider.dbCheck(type, getDeviceProviderId())) {
			providerList = dprovider.registerVedorWithMosip();
			auditLog.info("****DeviceProvider details is  Not Prsent in DB*******");
			auditLog.info("providerId :" + providerList.get(0) + " VendorName :" + providerList.get(1));
		} else {
			providerList.add(getDeviceProviderId());
			providerList.add(commonDataProp.get("vendorName"));
			auditLog.info("****DeviceProvider details is alReady Exist in DB*******");
			auditLog.info("providerId :" + providerList.get(0) + " VendorName :" + providerList.get(1));
		}

		// 2. checking mosip_device_service and mosip_device_service_History table for
		// providerID is present or not
		auditLog.info("****Checking providerID in  MosipDeviceService and mosip_device_service_History table********");
		MosipDeviceService mds = new MosipDeviceService();
		List<String> mosipDeviceServiceProviderList = new ArrayList<>();
		if (!mds.dbCheck(type, getDeviceProviderId())) {
			mosipDeviceServiceProviderList = mds.registerMDS(providerList.get(0));
			String mosipDeviceServiceId = mosipDeviceServiceProviderList.get(0);
			String make = mosipDeviceServiceProviderList.get(1);
			String model = mosipDeviceServiceProviderList.get(2);
			mosipDeviceServiceProviderList.add(mosipDeviceServiceId);
			mosipDeviceServiceProviderList.add(make);
			mosipDeviceServiceProviderList.add(model);
			auditLog.info("**** MDS info Not Prsent in DB*****");
			auditLog.info("mosipDeviceServiceId :" + mosipDeviceServiceProviderList.get(0) + " make :"
					+ mosipDeviceServiceProviderList.get(1) + "  model: " + mosipDeviceServiceProviderList.get(2));
		} else {
			mosipDeviceServiceProviderList.add(getDeviceProviderId());
			mosipDeviceServiceProviderList.add(prop.get("make"));
			mosipDeviceServiceProviderList.add(prop.get("model"));
			auditLog.info("**** MDS info Prsent in DB*****");
			auditLog.info("MosipDeviceServiceId :" + mosipDeviceServiceProviderList.get(0) + " make :"
					+ mosipDeviceServiceProviderList.get(1) + "  model: " + mosipDeviceServiceProviderList.get(2));
		}

		// 3. Create Device Specification in eng,ara language
		auditLog.info("****Create Device Specification in eng and ara language********");
		CreateDeviceSpecification deviceSpec = new CreateDeviceSpecification();
		String deviceSpecIdIn_Eng = deviceSpec.createDeviceSpec("eng", mosipDeviceServiceProviderList, null);
		String deviceSpecIdIn_Ara = deviceSpec.createDeviceSpec("ara", mosipDeviceServiceProviderList,
				deviceSpecIdIn_Eng);

		// 4.a create device in primary language by passing the createdDeviceSpecId from
		// step 3
		auditLog.info("****create device in primary language********");
		CreateDevice createDevice = new CreateDevice();
		String deviceId = createDevice.createDevice(deviceSpecIdIn_Eng, "", "eng");

		// 4.b Update the above deviceId (from 4.a response) with code from info
		auditLog.info("****Update  the above deviceId with code from info file********");
		String deviceIdUpdatedValue = createDevice.updateDeviceIdWithCode(deviceId, deviceSpecIdIn_Ara);
		if (deviceIdUpdatedValue != null)
			auditLog.info("DeviceId updated with " + deviceIdUpdatedValue);
		else
			auditLog.info("Unable to Update deviceId");

		// 4.c create device in secondary language with updated deviceId(get deviceId
		// from 4.b )
		auditLog.info("****create device in secondary language********");
		deviceId = createDevice.createDevice(deviceSpecIdIn_Ara, deviceIdUpdatedValue, "ara");

		// 5.a check that device is isAcitive= true in both the language based on
		// deviceId
		auditLog.info("****Device and center mapping********");
		DeviceRegistrationCenterMapping mapping = new DeviceRegistrationCenterMapping();
		String devicId = mapping.deviceRegCenterMapping(deviceId);
		auditLog.info("device Mapped succesfully with devicId : " + devicId);

		// 6. Register device take request value from info
		auditLog.info("****Device Registration********");
		RegisterDevice registerDevice = new RegisterDevice();
		registerDevice.centerRegistrationWithDevice();

		// 7. DeviceValidate History
		auditLog.info("****DeviceValidate History********");
		ValidateHistory validateHistory = new ValidateHistory();
		validateHistory.validateDeviceHistory();

	}
}
