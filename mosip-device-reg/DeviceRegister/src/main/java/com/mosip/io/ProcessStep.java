package com.mosip.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.util.Util;

public class ProcessStep extends Util {

	public void process(String type, Map<String, String> prop) {
		Util.setupLogger();
		auditLog.info("******************************Authentication**************************");
		Authentication auth = new Authentication();
		auth.login();

		// 1.a checking DeviceProvider and DeviceProviderHistory table for providerID is
		// present or not
		auditLog.info("*********************************DeviceProvider********************************");
		auditLog.info("****Checking providerID in  DeviceProvider and DeviceProviderHistory table*****");
		List<String> providerList = new ArrayList<>();
		DeviceProvider dprovider = new DeviceProvider();
		if (!dprovider.dbCheck(type, prop.get("deviceProviderId"))) {
			auditLog.info("****DeviceProvider details was  not Prsent in DB so, Creating DeviceProvider*******");
			providerList = dprovider.registerVedorWithMosip();
			if (providerList.isEmpty())
				throw new RuntimeException("Device Provider already exist");
			auditLog.info("DeviceProvider created with providerId :" + providerList.get(0) + " VendorName :"
					+ providerList.get(1));

			// 1.b update DeviceProvider and DeviceProviderHistory table for providerID
			updateDeviceProviderId(prop, providerList);

		} else {
			providerList.add(prop.get("deviceProviderId"));
			providerList.add(commonDataProp.get("vendorName"));
			auditLog.info(">>>>>>>>>>>>>> DeviceProvider details is alReady Exist in DB");
			auditLog.info("\tproviderId :" + providerList.get(0) + " VendorName :" + providerList.get(1));
		}

		// 2. checking mosip_device_service and mosip_device_service_History table for
		// providerID is present or not
		auditLog.info("******************************Mosip Device Service*****************************************");
		auditLog.info("****Checking providerID and model in  MosipDeviceService and mosip_device_service_History table******");
		MosipDeviceService mds = new MosipDeviceService();
		List<String> mosipDeviceServiceProviderList = new ArrayList<>();
		if (!mds.dbCheck(type, prop.get("deviceProviderId"),prop.get("model"))) {
			// mosipDeviceServiceProviderList = mds.registerMDS(providerList.get(0),prop);
			mosipDeviceServiceProviderList = mds.registerMDS(prop.get("deviceProviderId"), prop);
			String mosipDeviceServiceId = mosipDeviceServiceProviderList.get(0);
			String make = mosipDeviceServiceProviderList.get(1);
			String model = mosipDeviceServiceProviderList.get(2);
			mosipDeviceServiceProviderList.add(mosipDeviceServiceId);
			mosipDeviceServiceProviderList.add(make);
			mosipDeviceServiceProviderList.add(model);
			auditLog.info("**** MDS info was not prsent in DB so,created MDS info*****");
			auditLog.info("mosipDeviceServiceId :" + mosipDeviceServiceProviderList.get(0) + " make :"
					+ mosipDeviceServiceProviderList.get(1) + "  model: " + mosipDeviceServiceProviderList.get(2));
		} else {
			mosipDeviceServiceProviderList.add(prop.get("deviceProviderId"));
			mosipDeviceServiceProviderList.add(prop.get("make"));
			mosipDeviceServiceProviderList.add(prop.get("model"));
			auditLog.info(">>>>>>>>>>>>>>MDS info already Prsent in DB");
			auditLog.info("\tMosipDeviceServiceId :" + mosipDeviceServiceProviderList.get(0) + " make :"
					+ mosipDeviceServiceProviderList.get(1) + "  model: " + mosipDeviceServiceProviderList.get(2));
		}

		// 3. Create Device Specification in eng,ara language
		auditLog.info("**************Device Specification***********************************");
		auditLog.info("***Checking for Device Specification in eng and ara language****");
		CreateDeviceSpecification deviceSpec = new CreateDeviceSpecification();
		// select id from master.device_spec where
		// model='mosipDeviceServiceProviderList.get(2)'
		DataBaseAccess db = new DataBaseAccess();
		String deviceSpecId = "";
		String updatedDeviceId = prop.get("deviceCode");
		boolean isSerialNo_DeviceSpecIdPresent = false;
		try {
			String isModelExistInDB = "Select id from master.device_spec where brand=" + "'" + prop.get("make")
					+ "' and dtyp_code=" + "'" + prop.get("deviceTypeCode") + "'";
			List<String> dSpecId = db.getDbData(isModelExistInDB, "masterdata");
			if (dSpecId.size() > 0) {
				deviceSpecId = dSpecId.get(0);
				auditLog.info("DeviceSpecId :" + deviceSpecId);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if (deviceSpecId != null && !deviceSpecId.isEmpty()) {
			auditLog.info("****Device Specification is present in DB with eng and ara language********");
			auditLog.info("--------------------SKIP DEVICE SPECIFICATION STEP -------------------------");

			String sqlQuery = "SELECT * FROM master.device_master where serial_num=" + "'" + prop.get("serialNo")
					+ "'and dspec_id=" + "'" + deviceSpecId + "'";
			List<String> dSpecId = db.getDbData(sqlQuery, "masterdata");
			if (dSpecId.size() > 0) {
				isSerialNo_DeviceSpecIdPresent = true;
				System.out.println("Details exist in DB :" + isSerialNo_DeviceSpecIdPresent);
				auditLog.info(
						"serial_num :" + prop.get("serialNo") + " and dtyp_code :" + deviceSpecId + " is present");
				auditLog.info("Device deatils already exist in DB so, Skipping Create Device step");
			}
			if (!isSerialNo_DeviceSpecIdPresent) {
				// 4.a create device in primary language by passing the createdDeviceSpecId from
				// step 3
				auditLog.info("********************create device***********************************");
				auditLog.info("****create device in primary language********");
				CreateDevice createDevice = new CreateDevice();
				String deviceId = createDevice.createDevice(deviceSpecId, "", "eng", prop);

				// 4.b Update the above deviceId (from 4.a response) with code from info
				auditLog.info("****Update  the above deviceId with code from info file********");
				String deviceIdUpdatedValue = createDevice.updateDeviceIdWithCode(deviceId, deviceSpecId, prop);
				if (deviceIdUpdatedValue != null)
					auditLog.info("DeviceId updated with " + deviceIdUpdatedValue);
				else
					auditLog.info("Unable to Update deviceId");

				// 4.c create device in secondary language with updated deviceId(get deviceId
				// from 4.b )
				auditLog.info("****create device in secondary language********");
				updatedDeviceId = createDevice.createDevice(deviceSpecId, deviceIdUpdatedValue, "ara", prop);
			}

		} else {
			String deviceSpecIdIn_Eng = deviceSpec.createDeviceSpec("eng", mosipDeviceServiceProviderList, null, prop);
			String deviceSpecIdIn_Ara = deviceSpec.createDeviceSpec("ara", mosipDeviceServiceProviderList,
					deviceSpecIdIn_Eng, prop);

			String sqlQuery = "SELECT*	FROM master.device_master where serial_num=" + "'" + prop.get("serialNo")
					+ "'and dspec_id=" + "'" + deviceSpecIdIn_Ara + "'";
			List<String> dSpecId = db.getDbData(sqlQuery, "masterdata");
			if (dSpecId.size() > 0) {
				System.out.println("Details exist in DB :" + isSerialNo_DeviceSpecIdPresent);
				auditLog.info(
						"serial_num :" + prop.get("serialNo") + " and dspec_id :" + deviceSpecIdIn_Ara + " is present");
				auditLog.info(" Device deatils already exist in DB so, Skipping Create Device step");
			} else {
				// 4.a create device in primary language by passing the createdDeviceSpecId from
				auditLog.info("********************create device***********************************");
				auditLog.info("****create device in primary language********");
				CreateDevice createDevice = new CreateDevice();
				String deviceId = createDevice.createDevice(deviceSpecIdIn_Eng, "", "eng", prop);

				// 4.b Update the above deviceId (from 4.a response) with code from info
				auditLog.info("****Update  the above deviceId with code from info file********");
				String deviceIdUpdatedValue = createDevice.updateDeviceIdWithCode(deviceId, deviceSpecIdIn_Ara, prop);
				if (deviceIdUpdatedValue != null)
					auditLog.info("DeviceId updated with " + deviceIdUpdatedValue);
				else
					auditLog.info("Unable to Update deviceId");

				// 4.c create device in secondary language with updated deviceId(get deviceId
				// from 4.b )
				auditLog.info("*********create device in secondary language********");
				updatedDeviceId = createDevice.createDevice(deviceSpecIdIn_Ara, deviceIdUpdatedValue, "ara", prop);
			}

		}

		// 5.a check that device is isAcitive= true in both the language based on
		// deviceId
		if (!(type.equalsIgnoreCase("Auth"))) {
			auditLog.info("****Device and center mapping********");

			String centerMappingSqlQuery = "Select * from master.reg_center_device where regcntr_id=" + "'"
					+ prop.get("regCenterId") + "'" + " and device_id=" + "'" + prop.get("deviceId") + "'";
			List<String> dSpecId = db.getDbData(centerMappingSqlQuery, "masterdata");
			if (dSpecId.size() > 0) {
				auditLog.info("regcntr_id :" + prop.get("regCenterId") + " and device_id : " + updatedDeviceId
						+ " is present");
				auditLog.info("Mapping  already exist in DB so, Skipping Device & Center mapping step");
			} else {
				DeviceRegistrationCenterMapping mapping = new DeviceRegistrationCenterMapping();
				String devicId = mapping.deviceRegCenterMapping(updatedDeviceId, prop);
				auditLog.info("device Mapped succesfully with devicId : " + devicId);
			}
		}

		// 6. Register device take request value from info
		auditLog.info("****Device Registration********");
		RegisterDevice registerDevice = new RegisterDevice();
		if (db.getDbData("select * from master.registered_device_master where code=" + "'" + prop.get("deviceCode")
				+ "' and  serial_number=" + "'" + prop.get("serialNo") + "'", "masterdata").size() > 0) {
			auditLog.info("DeviceCode and SerialNo already exist in DB so, Skipping Device Registration step");
		} else {
			auditLog.info("DeviceCode and SerialNo does exist in DB so, executing Device Registration step");
			registerDevice.registerDevice(prop);
		}

		// 7. DeviceValidate History

		auditLog.info("****DeviceValidate History********");
		auditLog.info("Verifying in DB whether DeviceCode and DeviceId values are Same or different");
		if ((db.getDbData("select * from master.registered_device_master where code=" + "'" + prop.get("deviceCode")
				+ "' and  device_id=" + "'" + prop.get("deviceId") + "'", "masterdata").size() > 0)) {
			auditLog.info("DeviceCode and DeviceId  values are same so, now validating device History");
			ValidateHistory validateHistory = new ValidateHistory();
			validateHistory.validateDeviceHistory(prop);
		} else {
			auditLog.info("DeviceCode and DeviceId  Values are different so, Device History cannot be validated");
		}

	}

	private void updateDeviceProviderId(Map<String, String> prop, List<String> providerList) {
		DataBaseAccess db = new DataBaseAccess();
		if (db.executeQuery("update master.device_provider set id=" + "'" + prop.get("deviceProviderId") + "'"
				+ " where id=" + "'" + providerList.get(0) + "'", "masterdata")
				&& db.executeQuery("update master.device_provider_h set id=" + "'" + prop.get("deviceProviderId") + "'"
						+ " where id=" + "'" + providerList.get(0) + "'", "masterdata")) {
			auditLog.info("DeviceProvider and DeviceProviderHistory updated with :" + prop.get("deviceProviderId"));
		}
	}
}
