package se.sundsvall.seabloader;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;

import static org.springframework.boot.SpringApplication.run;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
public class Application {
	public static void main(final String... args) {
		run(Application.class, args);
	}
}
