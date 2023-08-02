package com.cpiwx.canalstarter.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> clusters = canalProperties.getCluster();
        List<InetSocketAddress> list = clusters.stream().map(endpoint -> {
            String[] split = endpoint.split(StrUtil.COLON);
            if (split.length != 2) {
                throw new RuntimeException("【格式错误】" + endpoint);
            }
            return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
        }).collect(Collectors.toList());
        return CanalConnectors.newClusterConnector(list, canalProperties.getDestination(), canalProperties.getUsername(), canalProperties.getPassword());
    }

    @Bean(destroyMethod = "close")
    public CanalClient canalClient(CanalConnector canalConnector, CanalProperties canalProperties) {
        return new CanalClient(canalConnector, canalProperties);
    }

}
