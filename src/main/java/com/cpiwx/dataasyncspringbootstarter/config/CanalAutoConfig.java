package com.cpiwx.dataasyncspringbootstarter.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * @author chenPan
 * @date 2023-07-31 10:35
 **/
@Configuration
@EnableConfigurationProperties(CanalProperties.class)
@Slf4j
public class CanalAutoConfig {

    @Bean
    public CanalConnector canalConnector(CanalProperties canalProperties) {
        // 创建链接
        InetSocketAddress socketAddress = new InetSocketAddress(canalProperties.getHost(), canalProperties.getPort());
        return CanalConnectors.newSingleConnector(socketAddress, canalProperties.getDestination(), canalProperties.getUsername(), canalProperties.getPassword());
    }

    @Bean(destroyMethod = "close")
    public CanalClient canalClient(CanalConnector canalConnector, CanalProperties canalProperties) {
        return new CanalClient(canalConnector, canalProperties);
    }

}
