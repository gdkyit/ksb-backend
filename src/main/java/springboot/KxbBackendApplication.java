package springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;


@ServletComponentScan(basePackages={"com.gdky.restful","gov.hygs"})
@ComponentScan(basePackages={"com.gdky.restful","gov.hygs"})
@SpringBootApplication
public class KxbBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KxbBackendApplication.class, args);
	}
}
