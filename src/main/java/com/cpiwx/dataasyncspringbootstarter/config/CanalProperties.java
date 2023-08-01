package com.cpiwx.dataasyncspringbootstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chenPan
 * @date 2023-07-31 10:39
 **/
@ConfigurationProperties(prefix = "canal")
@Data
public class CanalProperties {

    private String host;

    private Integer port;

    private String username;

    private String password;

    private String destination;

    private String subscribe;

    private Integer testIdleTime;

}
