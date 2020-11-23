package com.mosip.io.util;

public interface EndPoint {
	public static final String USER_AUTHENTICATE = "/v1/authmanager/authenticate/useridPwd";
	public static final String CREATE_DEVICE_SPECIFICATIONS = "/v1/masterdata/devicespecifications";
	public static final String DEFINE_POLICY_GROUP = "/partnermanagement/v1/policies/policies/policyGroup";
	public static final String PARTNER_SELF_REGISTRATION = "/partnermanagement/v1/partners/partners";
	public static final String SAVE_DEVICE_DETAIL = "/partnermanagement/v1/partners/devicedetail";
	public static final String APPROVE_DEVICE_DETAIL = "/partnermanagement/v1/partners/devicedetail";
	public static final String SAVE_SECURE_BIOMETRIC_INFO = "/partnermanagement/v1/partners/securebiometricinterface";
	public static final String APPROVE_SECURE_BIOMETRIC_INFO = "/partnermanagement/v1/partners/securebiometricinterface";
	public static final String CREATE_DEVICE = "/v1/masterdata/devices";
	public static final String VALIDATE_DEVICE_HISTORY = "/partnermanagement/v1/partners/deviceprovidermanagement/validate";
	public static final String REGISTERED_DEVICES = "/partnermanagement/v1/partners/registereddevices";
	public  static final String DEVICE_DE_REGISTERED="/partnermanagement/v1/partners/registereddevices/deregister";
}
