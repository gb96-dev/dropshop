package com.example.dropshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableJpaAuditing jpaconfig 에포함
@EnableScheduling
@ConfigurationPropertiesScan
public class DropshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(DropshopApplication.class, args);
    }

}
