package com.cpiwx.dataasyncspringbootstarter.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chenPan
 * @date 2023-08-01 13:54
 **/
@Component
public class AutoRun implements CommandLineRunner {
    @Resource
    private CanalClient canalClient;

    @Override
    public void run(String... args) throws Exception {
        canalClient.begin();
    }
}
