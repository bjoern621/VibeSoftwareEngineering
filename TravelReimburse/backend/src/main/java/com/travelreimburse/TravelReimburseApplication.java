package com.travelreimburse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TravelReimburseApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelReimburseApplication.class, args);
    }

}