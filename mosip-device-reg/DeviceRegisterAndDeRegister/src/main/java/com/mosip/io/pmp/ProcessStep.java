package com.mosip.io.pmp;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pmp.ApproveDeviceDetail;
import com.mosip.io.pmp.ApproveSecureBiometricInfo;
import com.mosip.io.pmp.CreateDevice;
import com.mosip.io.pmp.CreateDeviceSpecification;
import com.mosip.io.pmp.DefinePolicyGroup;
import com.mosip.io.pmp.PartnerSelfRegistration;
import com.mosip.io.pmp.RegisterDevice;
import com.mosip.io.pmp.SaveDeviceDetail;
import com.mosip.io.pmp.SecureBiometricInfo;
import com.mosip.io.pmp.ValidateHistory;
import com.mosip.io.util.Util;

@SuppressWarnings("unused")
public class ProcessStep extends Util {
    String deviceCode=null;
	public String process(String type, Map<String, String> prop) {
		Util.setupLogger();
		logInfo("Running Authentication");
		Authentication auth = new Authentication();
		auth.login("partner_appid", "partner_user", "partner_password");
		// 1. defining Policy Group
		/*
		 * boolean isPolicyGroupRequired = isPartnerTypePresent(); String
		 * policyGroupName = null; if (isPolicyGroupRequired) { DefinePolicyGroup
		 * dpGroup = new DefinePolicyGroup(); policyGroupName =
		 * dpGroup.definePolicyGroup(); System.out.println(policyGroupName); }
		 */
		// 2.partner Self Registration
		logInfo("Running Partner Self Registration");
		String partnerId = PartnerSelfRegistration.partnerSelfRegistration(prop);
		// 3.save device Detail
		logInfo("Running Save Device Detail");
		String deviceDetailId = SaveDeviceDetail.saveDeviceDetail(partnerId, prop);
		// 4.Approve Device detail
		logInfo("Running Approve Device Detail");
		ApproveDeviceDetail.approveDeviceDetail(deviceDetailId);
		// 5.save SecureBiometricInfo
		logInfo("Running Save SecureBiometricInfo");
		String saveSecureBiometricInfoId = SecureBiometricInfo.saveSecureBiometricInfo(deviceDetailId, prop);
		// 6.Approve SecureBiometricInfo
		logInfo("Running Approve SecureBiometricInfo");
		ApproveSecureBiometricInfo.approveSecureBiometricInfo(saveSecureBiometricInfoId);
		// 6.1 Admin login
		logInfo("Running Admin Login");
		auth.login("admin_appid", "admin_user", "admin_password");
		// 7.CreateDevice specification in eng
		logInfo("Running CreateDevice Specification");
		String primaryLanguage = commonDataProp.get("primaryLanguage");
		String secondaryLanguage = commonDataProp.get("secondaryLanguage");
		if ((StringUtils.isEmpty(primaryLanguage)))
			throw new RuntimeException("provide value for PrimaryLanguage");
		String deviceSpecificationId = CreateDeviceSpecification.createDeviceSpec(primaryLanguage, prop, null);
		// 7.1 //7.CreateDevice specification in ara
		if (isSecdryLangRequired())
			deviceSpecificationId = CreateDeviceSpecification.createDeviceSpec(secondaryLanguage, prop,
					deviceSpecificationId);
		// 8.save Device detail in eng
		logInfo("Running Save Device Detail");
		if ((StringUtils.isEmpty(primaryLanguage)))
			throw new RuntimeException("provide value for PrimaryLanguage");
		String deviceId = CreateDevice.createDevice(deviceSpecificationId, primaryLanguage, prop);
		// 8.1 save Device detail in ara
		if (isSecdryLangRequired())
			deviceId = CreateDevice.createDevice(deviceId, secondaryLanguage, prop);
		// 8.1 partner login
		logInfo("Running Partner Login");
		auth.login("partner_appid", "partner_user", "partner_password");
		// 9.signedRegisteredDevice
		logInfo("Running SignedRegisteredDevice");
		RegisterDevice registerDevice = new RegisterDevice();
		String deviceCode = registerDevice.registerDevice(deviceId, prop);
		// 10.validateDeviceProvider
		//logInfo("Running ValidateDeviceProvider");
		if (!(StringUtils.isEmpty(deviceCode))) {
		    // String errorCode = ValidateHistory.validateDeviceHistory(prop,deviceCode);
		     //if(errorCode==null)
		     this.deviceCode=deviceCode;
		     //else this.deviceCode=errorCode;
		     
		}
		return this.deviceCode;
	}
	

	public void deRegisterDevice(Map<String, String> deviceTypeAndDeviceCode) {
		if(deviceTypeAndDeviceCode.isEmpty()) {
		auditLog.info("Device already de-registerd");	
		}else {
		logInfo("Running DeviceDeRegistration");
		String deviceCode = "";
		Scanner scanner = new Scanner(System.in);
		System.out.print("Do you want to De-register the Device press Y/N :");
		String input = scanner.nextLine();
		if (input != null && !input.isEmpty()) {
			char value = input.trim().toLowerCase().charAt(0);
			if (value == 'y') {
				if (!deviceTypeAndDeviceCode.isEmpty()) {
					Set<String> deviceTypes = deviceTypeAndDeviceCode.keySet();
					for (String deviceType : deviceTypes) {
						deviceCode = deviceTypeAndDeviceCode.get(deviceType);
						DeviceDeRegister deRegister = new DeviceDeRegister();
						boolean deviceDeRegister = deRegister.deviceDeRegister(deviceCode);
						if(deviceDeRegister)
							auditLog.info("DeviceType : " + deviceType + " has been deRegistered succesfully!");
						else
						auditLog.info("Unable to deRegistered the device");
					}
				}
			}else {
				auditLog.info("Exist");
				scanner.close();
			}
			
		} else {
			scanner.close();
			return;
		}
		}
	}

	private boolean isPartnerTypePresent() {
		DataBaseAccess dataBaseAccess = new DataBaseAccess();
		String sqlQuery = "Select count(*) FROM pms.partner_type where code='Auth_Partner' and is_active=true";
		boolean isPresent = dataBaseAccess.validateDataInDb(sqlQuery, "pms");
		return isPresent;
	}
}
