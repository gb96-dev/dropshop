package com.example.dropshop;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableJpaAuditing jpaconfig 에포함
@EnableScheduling
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT1M")
@ConfigurationPropertiesScan
public class DropshopApplication {

  public static void main(String[] args) {
    SpringApplication.run(DropshopApplication.class, args);
  }

}
