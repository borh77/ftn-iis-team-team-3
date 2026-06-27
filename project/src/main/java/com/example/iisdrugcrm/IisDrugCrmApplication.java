package com.example.iisdrugcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IisDrugCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(IisDrugCrmApplication.class, args);
    }
}
