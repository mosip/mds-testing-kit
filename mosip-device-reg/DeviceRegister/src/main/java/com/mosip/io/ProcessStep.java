package com.mosip.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.db.QueryBuilder;
import com.mosip.io.util.Util;

public class ProcessStep extends Util {

	public void process(String type, Map<String, String> prop) {
		Util.setupLogger();
		logInfo("Running Authentication");
		Authentication auth = new Authentication();
		auth.login();

		// 1.a checking DeviceProvider and DeviceProviderHistory table for providerId is
		// present or not
		logInfo("Running DeviceProvider");
		logInfo("checking providerId in deviceProvider and deviceProviderHistory table");
		List<String> providerList = new ArrayList<>();
		DeviceProvider dprovider = new DeviceProvider();
		if (!dprovider.dbCheck(type, prop.get("deviceProviderId"))) {
			logInfo("deviceProvider details not prsent in DB so, creating deviceProvider");
			providerList = dprovider.registerVedorWithMosip();
			if (!providerList.isEmpty())
			logInfo("deviceProvider created with providerId : " + providerList.get(0) + " vendorName : "
					+ providerList.get(1));
			else return ;
			// 1.b update DeviceProvider and DeviceProviderHistory table for providerId
			QueryBuilder.updateDeviceProviderId(prop, providerList);
		} else {
			providerList.add(prop.get("deviceProviderId"));
			providerList.add(commonDataProp.get("vendorName"));
			logInfo("deviceProvider detail exist in DB");
			logInfo("providerId : " + providerList.get(0) + " vendorName : " + providerList.get(1));
		}
		// 2. checking mosip_device_service and mosip_device_service_History table for
		// providerId is present or not
		logInfo("Running Mosip Device Service");
		logInfo("checking providerId and model in  MDS and MDS_History table");
		MosipDeviceService mds = new MosipDeviceService();
		List<String> mdsList = new ArrayList<>();
		if (!mds.dbCheck(type, prop.get("deviceProviderId"), prop.get("model"))) {
			mdsList = mds.registerMDS(prop.get("deviceProviderId"), prop);
			if(!mdsList.isEmpty()) {
			logInfo("MDS info not prsent in DB so,created MDS info");
			logInfo("mosipDeviceServiceId : " + mdsList.get(0) + " make : "
					+ mdsList.get(1) + "  model: " + mdsList.get(2));
			}else return ;
		} else {
			mdsList.add(prop.get("deviceProviderId"));
			mdsList.add(prop.get("make"));
			mdsList.add(prop.get("model"));
			logInfo("MDS info exist in DB");
			logInfo("mosipDeviceServiceId : " + mdsList.get(0) + " make : "
					+ mdsList.get(1) + "  model: " + mdsList.get(2));
		}

		// 3. Create Device Specification in eng,ara language
		logInfo("Running Device Specification");
		String primaryLanguage = commonDataProp.get("primaryLanguage");
		String secondaryLanguage = commonDataProp.get("secondaryLanguage");
		if ((StringUtils.isEmpty(primaryLanguage)))
			throw new RuntimeException("provide value for PrimaryLanguage");
		CreateDeviceSpecification deviceSpec = new CreateDeviceSpecification();
		DataBaseAccess db = new DataBaseAccess();
		String deviceSpecId = "";
		String deviceId = "";
		String deviceIdUpdatedValue = prop.get("deviceCode");
		boolean isSerialNo_DeviceSpecIdPresent = false;
		try {
			List<String> dSpecId =null;
			String isModelExistInDBInEng = QueryBuilder.isModelExistInDBSqlQuery(prop.get("make"),
					prop.get("deviceTypeCode"), primaryLanguage);
			if (isSecdryLangRequired()) {
				String isModelExistInDBInAra = QueryBuilder.isModelExistInDBSqlQuery(prop.get("make"),
						prop.get("deviceTypeCode"), secondaryLanguage);
				List<String> dSpec_Id=db.getDbData(isModelExistInDBInEng, "masterdata");
				if (dSpec_Id.size()>0 && db.getData(isModelExistInDBInAra, "masterdata").size()>0)
					dSpecId = dSpec_Id;
			} else
				dSpecId = db.getDbData(isModelExistInDBInEng, "masterdata");
			if (dSpecId!=null && dSpecId.size() > 0) {
				deviceSpecId = dSpecId.get(0);
				logInfo("deviceSpecId :" + deviceSpecId);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if (deviceSpecId != null && !deviceSpecId.isEmpty()) {
			List<String> dSpecId =null;
			logInfo("Device Specification present in DB so,skipping create device Spec");
			logInfo("Running Create Device");
			logInfo("checking serial_num and dspec_id in device_master table");
			String sqlQueryInEng = QueryBuilder.sqlQuery(prop.get("serialNo"), deviceSpecId, primaryLanguage);
			if (isSecdryLangRequired()) {
				String sqlQueryInAra = QueryBuilder.sqlQuery(prop.get("serialNo"), deviceSpecId, secondaryLanguage);
				List<String> dSpec_Id =db.getDbData(sqlQueryInEng, "masterdata");
				if (dSpec_Id.size()>0 && db.getData(sqlQueryInAra, "masterdata").size()>0)
					dSpecId = dSpec_Id;

			} else
				dSpecId = db.getDbData(sqlQueryInEng, "masterdata");
			if (dSpecId!=null && dSpecId.size() > 0) {
				isSerialNo_DeviceSpecIdPresent = true;
				logInfo("serial_num : " + prop.get("serialNo") + " and Dtyp_code : " + deviceSpecId + " is present");
				logInfo("Device deatils exist in DB so, skipping create device step");
			}
			if (!isSerialNo_DeviceSpecIdPresent) {
				// 4.a create device in primary language by passing the createdDeviceSpecId
				logInfo("Running Create Device");
				logInfo("create device in primary language");
				CreateDevice createDevice = new CreateDevice();
				deviceId = createDevice.createDevice(deviceSpecId, "", primaryLanguage, prop);
				if (!(StringUtils.isEmpty(deviceId))) {
					// 4.b Update the above deviceId (from 4.a response) with code from info
					logInfo("update above deviceId with code from info file");
					deviceIdUpdatedValue = createDevice.updateDeviceIdWithCode(type,deviceId, deviceSpecId, prop);
					if (deviceIdUpdatedValue != null)
						logInfo("deviceId updated with " + deviceIdUpdatedValue);
					else
						logInfo("unable to Update deviceId");

					// 4.c create device in secondary language with updated deviceId(get deviceId from 4.b )
					if (isSecdryLangRequired()) {
						logInfo("create Device in secondary language");
						deviceIdUpdatedValue = createDevice.createDevice(deviceSpecId, deviceIdUpdatedValue,
								secondaryLanguage, prop);
					} else {
						String setIsActiveTrueForPriLang = QueryBuilder
								.setIsActiveTrueForPriLangUpdateForCreateDevice(deviceIdUpdatedValue);
						if (db.executeQuery(setIsActiveTrueForPriLang, "masterdata"))
							logInfo("device status isActive set to true ");
					}
				}
			}

		} else {
			logInfo("device specification not present in DB so, creating device spec");
			deviceSpecId = deviceSpec.createDeviceSpec(primaryLanguage, mdsList, null, prop);
			if (!(StringUtils.isEmpty(deviceSpecId))) {
				if (isSecdryLangRequired()) {
					deviceSpecId = deviceSpec.createDeviceSpec(secondaryLanguage, mdsList,
							deviceSpecId, prop);
				} else {
					String setIsActiveTrueForPriLang = QueryBuilder
							.setIsActiveTrueForPriLangUpdateForDeviceSpec(deviceSpecId);
					if (db.executeQuery(setIsActiveTrueForPriLang, "masterdata"))
						logInfo("device status isActive set to true ");
				}
				List<String> dSpecId =null;
				String sqlQueryInEng = QueryBuilder.sqlQuery(prop.get("serialNo"), deviceSpecId, primaryLanguage);
				if (isSecdryLangRequired()) {
					String sqlQueryInAra = QueryBuilder.sqlQuery(prop.get("serialNo"), deviceSpecId, primaryLanguage);
					List<String> dSpec_Id =db.getDbData(sqlQueryInEng, "masterdata");
					if (dSpec_Id.size()>0 && db.getData(sqlQueryInAra, "masterdata").size()>0)
						dSpecId = dSpec_Id;
				}else
				dSpecId = db.getDbData(sqlQueryInEng, "masterdata");
				if (dSpecId!=null && dSpecId.size() > 0) {
					logInfo("serial_num : " + prop.get("serialNo") + " and dspec_id : " + deviceSpecId + " is present");
					logInfo("Device deatil exist in DB so, skipping create device step");
				} else {
					// 4.a create device in primary language by passing the createdDeviceSpecId
					logInfo("Running Create Device");
					logInfo("create device in primary language");
					CreateDevice createDevice = new CreateDevice();
					deviceId = createDevice.createDevice(deviceSpecId, "", primaryLanguage, prop);
					if (!(StringUtils.isEmpty(deviceId))) {
						// 4.b Update the above deviceId (from 4.a response) with code from info
						logInfo("update above deviceId with code from info file");
						deviceIdUpdatedValue = createDevice.updateDeviceIdWithCode(type,deviceId, deviceSpecId, prop);
						if (deviceIdUpdatedValue != null)
							logInfo("deviceId updated with " + deviceIdUpdatedValue);
						else
							logInfo("unable to Update deviceId");
						// 4.c create device in secondary language with updated deviceId(get deviceId from 4.b )
						if (isSecdryLangRequired()) {
							logInfo("create device in secondary language");
							deviceIdUpdatedValue = createDevice.createDevice(deviceSpecId, deviceIdUpdatedValue,
									secondaryLanguage, prop);
						} else {
							String setIsActiveTrueForPriLang = QueryBuilder
									.setIsActiveTrueForPriLangUpdateForCreateDevice(deviceIdUpdatedValue);
							if (db.executeQuery(setIsActiveTrueForPriLang, "masterdata"))
								logInfo("device status isActive set to true ");
						}
					}
				}

			}
		}

		// 5.a check that device is isAcitive= true in both the language based on deviceId
		if (!(StringUtils.isEmpty(deviceSpecId))) {
			if (!(type.equalsIgnoreCase("Auth"))) {
				logInfo("Running Device and Center mapping");
				String centerMappingSqlQuery = QueryBuilder.centerMappingSqlQuery(prop.get("regCenterId"),
						prop.get("deviceId"));
				List<String> dSpecId = db.getDbData(centerMappingSqlQuery, "masterdata");
				if (dSpecId.size() > 0) {
					logInfo("regcntr_id : " + prop.get("regCenterId") + " and device_id : " + deviceIdUpdatedValue
							+ " is present");
					logInfo("mapping exist in DB so, skipping device & center mapping step");
				} else {
					DeviceRegistrationCenterMapping mapping = new DeviceRegistrationCenterMapping();
					String devicId = mapping.deviceRegCenterMapping(deviceIdUpdatedValue, prop, primaryLanguage);
					logInfo("device mapped succesfully with devicId : " + devicId);
				}
			}

			// 6. Register device take request value from info
			logInfo("Running Device Registration");
			boolean isDeviceRegisteredSuccess=true;
			RegisterDevice registerDevice = new RegisterDevice();
			if (db.getDbData(QueryBuilder.registerDeviceSqlQuery(prop.get("deviceCode"), prop.get("serialNo"),
					prop.get("deviceProviderId")), "masterdata").size() > 0) {
				logInfo("deviceCode and serialNo already exist in DB so, skipping device registration step");
			} else {
				auditLog.info("deviceCode and serialNo does not exist in DB so, executing device registration step");
				boolean isdeviceRigistered = registerDevice.registerDevice(prop);
				isDeviceRegisteredSuccess=isdeviceRigistered;
				if (isdeviceRigistered && type.equalsIgnoreCase("Auth")) {
					String updateCodewithDeviceIdInMaster = QueryBuilder.updateCodewithDeviceIdSqlQuery("registered_device_master",prop.get("deviceId"),
							prop.get("type"), prop.get("serialNo"), prop.get("deviceProviderId"));
					String updateCodewithDeviceIdInHistory = QueryBuilder.updateCodewithDeviceIdSqlQuery("registered_device_master_h",prop.get("deviceId"),
							prop.get("type"), prop.get("serialNo"), prop.get("deviceProviderId"));
					if (db.executeQuery(updateCodewithDeviceIdInMaster, "masterdata") && db.executeQuery(updateCodewithDeviceIdInHistory, "masterdata")) {
						logInfo("code value updated with deviceId");
					}
				}
			}

			// 7. DeviceValidate History
			if(isDeviceRegisteredSuccess) {
			logInfo("Running DeviceValidate History");
			logInfo("verifying in DB whether deviceCode and deviceId values are same or different");
			if ((db.getDbData(QueryBuilder.DeviceValidateSqlQuery(prop.get("deviceCode"), prop.get("deviceId")),
					"masterdata").size() > 0)) {
				logInfo("deviceCode and deviceId  values are same so, now validating device History");
				ValidateHistory validateHistory = new ValidateHistory();
				validateHistory.validateDeviceHistory(prop);
			} else {
				logInfo("deviceCode and deviceId  values are different so, device history cannot be validated");
			}
		 }
		}
	}
}
