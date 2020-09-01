package com.mosip.io.db;

import java.util.List;
import java.util.Map;

import com.mosip.io.util.Util;

public class QueryBuilder extends Util{
	public static String isModelExistInDBSqlQuery(String make,String deviceTypeCode, String lang_code){
		StringBuilder query= new StringBuilder();
	return 	query.append("Select id from master.device_spec where brand='")
		.append(make)
		.append("'")
		.append(" and dtyp_code='")
		.append(deviceTypeCode)
		.append("'")
		.append(" and lang_code='")
		.append(lang_code)
		.append("'").toString();
		}
	
	public static String sqlQuery(String serial_num,String dspec_id, String lang_code){
		StringBuilder query= new StringBuilder();
		return query.append("select * from master.device_master where serial_num='")
		.append(serial_num)
		.append("'")
		.append(" and dspec_id='")
		.append(dspec_id)
		.append("'")
		.append(" and lang_code='")
		.append(lang_code)
		.append("'").toString();
		}
	
	public static String centerMappingSqlQuery(String regcntr_id,String device_id){
		StringBuilder query= new StringBuilder();
		return query.append("Select * from master.reg_center_device where regcntr_id='")
		.append(regcntr_id)
		.append("'")
		.append(" and device_id='")
		.append(device_id)
		.append("'").toString();
		}
	
	public static  String registerDeviceSqlQuery(String code,String serial_number,String provider_id){
		StringBuilder query= new StringBuilder();
		return query.append("select * from master.registered_device_master where code='")
		.append(code)
		.append("'")
		.append(" and serial_number='")
		.append(serial_number)
		.append("'")
		.append(" and provider_id='")
		.append(provider_id)
		.append("'").toString();
		}
	//DeviceValidate History
	public static String DeviceValidateSqlQuery(String code,String device_id){
		StringBuilder query= new StringBuilder();
		return query.append("select * from master.registered_device_master where code='")
		.append(code)
		.append("'")
		.append(" and device_id='")
		.append(device_id)
		.append("'").toString();
		}
	//device_providerQuery
	public static String device_providerSqlQuery(String id,String vendor_name){
		StringBuilder query= new StringBuilder();
		return query.append("Select * from master.device_provider where id='")
		.append(id)
		.append("'")
		.append(" and vendor_name='")
		.append(vendor_name)
		.append("'").toString();
		}
	//device_providerHistoryQuery
	public static String device_providerHistorySqlQuery(String id,String vendor_name){
		StringBuilder query= new StringBuilder();
		return query.append("Select * from master.device_provider_h where id='")
		.append(id)
		.append("'")
		.append(" and vendor_name='")
		.append(vendor_name)
		.append("'").toString();
		}
	
	public static String setIsActiveTrueForPriLangUpdateForCreateDevice(String id){
		StringBuilder query= new StringBuilder();
		return  query.append("update master.device_master set is_active='true' where id='")
		.append(id)
		.append("'").toString();
		}	
	
	public static String setIsActiveTrueForPriLangUpdateForDeviceSpec(String id){
		StringBuilder query= new StringBuilder();
		return query.append("update master.device_spec set is_active='true' where id='")
		.append(id)
		.append("'").toString();
		}
	
	public static String updateCodewithDeviceIdSqlQuery(String code,String dtype_code,String serial_number,String provider_id ){
		StringBuilder query= new StringBuilder();
		return query.append("update master.registered_device_master set code='")
		.append(code)
		.append("'")
		.append(" and dtype_code='")
		.append(dtype_code)
		.append("'")
		.append(" and serial_number='")
		.append(serial_number)
		.append("'")
		.append(" and provider_id='")
		.append(provider_id)
		.append("'").toString();
		}
	
	public static void updateDeviceProviderId(Map<String, String> prop, List<String> providerList) {
		setupLogger();
		DataBaseAccess db = new DataBaseAccess();
		if (db.executeQuery("update master.device_provider set id=" + "'" + prop.get("deviceProviderId") + "'"
				+ " where id=" + "'" + providerList.get(0) + "'and ", "masterdata")
				&& db.executeQuery("update master.device_provider_h set id=" + "'" + prop.get("deviceProviderId") + "'"
						+ " where id=" + "'" + providerList.get(0) + "'", "masterdata")) {
			logInfo("DeviceProvider and DeviceProviderHistory updated with :" + prop.get("deviceProviderId"));
		}
	}
}
