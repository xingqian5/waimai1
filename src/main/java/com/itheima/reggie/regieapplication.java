package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@EnableCaching
@EnableTransactionManagement
@Slf4j
@SpringBootApplication
@ServletComponentScan
public class regieapplication {
    public static void main(String[] args) {
        SpringApplication.run(regieapplication.class,args);
        log.info("启动");
    }
}
