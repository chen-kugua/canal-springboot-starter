package com.cpiwx.dataasyncspringbootstarter;

import com.cpiwx.dataasyncspringbootstarter.anotations.EnableCanal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author 38101
 */
@SpringBootApplication
@EnableCanal
@EnableAsync
@Slf4j
public class DataAsyncSpringBootStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAsyncSpringBootStarterApplication.class, args);
        log.info("===============启动完成 =================");
    }

}
