package se.sundsvall.seabloader;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import se.sundsvall.dept44.ServiceApplication;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT2M")
public class Application {
	public static void main(final String... args) {
		run(Application.class, args);
	}
}
