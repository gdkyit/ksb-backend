package springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages={"com.gdky.restful","gov.hygs"})
@SpringBootApplication
public class KsbBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KsbBackendApplication.class, args);
	}
}
