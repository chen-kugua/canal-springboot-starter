package com.cpiwx.dataasyncspringbootstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author chenPan
 * @date 2023-07-31 10:39
 **/
@ConfigurationProperties(prefix = "canal")
@Data
public class CanalProperties {
    /**
     * canal ip:端口
     */
    private List<String> cluster;


    /**
     * canal连接用户名
     */
    private String username;

    /**
     * canal连接密码
     */
    private String password;

    /**
     *指定订阅的数据库实例或目标。Canal 支持订阅多个数据库实例，每个实例称为一个 Destination。
     */
    private String destination;

    /**
     * 订阅库
     */
    private String subscribe;

    /**
     * 重连间隔时间
     */
    private Integer testIdleTime;

    /**
     * 一次获取消息条数
     */
    private Integer batchSize;

}
