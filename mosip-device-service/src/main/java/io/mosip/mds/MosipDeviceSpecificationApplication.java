package io.mosip.mds;

import io.mosip.mds.entitiy.Store;
import io.mosip.mds.service.TestCaseResultService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@Configuration
@ComponentScan(basePackages="io.mosip.mds")
public class MosipDeviceSpecificationApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		Store.STORAGE_PATH = args.length == 0 ? null : args[0];
		System.out.println("Store.STORAGE_PATH set to : " + Store.STORAGE_PATH);
		SpringApplication.run(MosipDeviceSpecificationApplication.class, args);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/notFound").setViewName("forward:/index.html");
	}


	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
		return container -> {
			container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND,
					"/notFound"));
		};
	}
	

}
