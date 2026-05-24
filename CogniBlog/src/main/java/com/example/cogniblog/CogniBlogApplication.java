package com.example.cogniblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.example.cogniblog.mapper")
@EnableCaching
public class CogniBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CogniBlogApplication.class, args);
    }

}
