package io.mosip.mds;

import io.mosip.mds.entitiy.Store;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableSwagger2
public class MosipDeviceSpecificationApplication {

	public static void main(String[] args) {
		Store.STORAGE_PATH = args.length == 0 ? null : args[0];
		System.out.println("Store.STORAGE_PATH set to : " + Store.STORAGE_PATH);
		SpringApplication.run(MosipDeviceSpecificationApplication.class, args);
	}

}
