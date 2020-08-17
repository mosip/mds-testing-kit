package com.mosip.io;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.mosip.io.util.Util;

public class Runner extends Util {
	public static Map<String, String> prop = null;

	public static void main(String[] args) {
		Runner runner = new Runner();
		runner.run();
	}

	public void run() {
		ProcessStep runner = new ProcessStep();
		String type = System.getProperty("type");
		if (type == null || type.isEmpty()) {
			throw new RuntimeException("Type cannot be null please provide TYPE value from VM argument !!!");
		} else if (type.equalsIgnoreCase("Face") || type.equalsIgnoreCase("Iris") || type.equalsIgnoreCase("Finger")
				|| type.equalsIgnoreCase("Auth")) {
			prop = loadDataFromCsv(type);
			auditLog.info("***************************REGISTERING DEVICE :" + type + " *************************");
			runner.process(type, prop);
		} else if (type.equalsIgnoreCase("All")) {
			List<String> typeList = Arrays.asList("Face", "Iris", "Finger", "Auth");
			for (int i = 0; i < typeList.size(); i++) {
				prop = loadDataFromCsv(typeList.get(i));
				auditLog.info("*************************REGISTERING DEVICE :" + typeList.get(i) + " ***************");
				runner.process(typeList.get(i), prop);
			}
		}
	}

}
