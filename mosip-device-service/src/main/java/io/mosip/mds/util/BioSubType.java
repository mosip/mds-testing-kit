package io.mosip.mds.util;

import java.util.ArrayList;
import java.util.List;

public enum BioSubType {
	
	LEFT_IRIS("LeftEye", "LEFT_IRIS,TWO_IRIS", "", "Left"),
	RIGHT_IRIS("RightEye", "RIGHT_IRIS,TWO_IRIS", "", "Right"),
	
	RIGHT_INDEX("RightIndex", "RIGHT_SLAP", "", "Right IndexFinger"),
	RIGHT_MIDDLE("RightMiddle", "RIGHT_SLAP", "", "Right MiddleFinger"),
	RIGHT_RING("RightRing", "RIGHT_SLAP", "", "Right RingFinger"),
	RIGHT_LITTLE("RightLittle", "RIGHT_SLAP", "", "Right LittleFinger"),
	
	LEFT_INDEX("LeftIndex", "LEFT_SLAP", "", "Left IndexFinger"),
	LEFT_MIDDLE("LeftMiddle", "LEFT_SLAP", "", "Left MiddleFinger"),
	LEFT_RING("LeftRing", "LEFT_SLAP", "", "Left RingFinger"),
	LEFT_LITTLE("LeftLittle", "LEFT_SLAP", "", "Left LittleFinger"),
	
	LEFT_THUMB("LeftThumb", "TWO_THUMBS", "", "Left Thumb"),
	RIGHT_THUMB("RightThumb", "TWO_THUMBS", "", "Right Thumb"),
	
	FACE("Face", "FULL_FACE", "", null);
	
	BioSubType(String commonName, String deviceSubType, String name_092, String name_095) {
		this.commonName = commonName;
		this.name_092 = name_092;
		this.name_095 = name_095;
		this.deviceSubType = deviceSubType;
	}
	
	private String commonName;
	private String name_092;
	private String name_095;
	private String deviceSubType;
	
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public String getName_092() {
		return name_092;
	}
	public void setName_092(String name_092) {
		this.name_092 = name_092;
	}
	public String getName_095() {
		return name_095;
	}
	public void setName_095(String name_095) {
		this.name_095 = name_095;
	}
	
	public static List<String> convertTo095(List<String> commonNames) {
		List<String> names_095 = new ArrayList<>();		
		for(BioSubType bioSubType : BioSubType.values()) {
			if(commonNames.stream().anyMatch(cn -> cn.equals(bioSubType.commonName)))
				names_095.add(bioSubType.name_095);
		}		
		return names_095;
	}
	
	public static List<String> get095BioSubTypes(String deviceSubType) {
		List<String> names_095 = new ArrayList<>();		
		for(BioSubType bioSubType : BioSubType.values()) {
			if(bioSubType.deviceSubType.contains(deviceSubType))
				names_095.add(bioSubType.name_095);
		}		
		return names_095;
	}
}