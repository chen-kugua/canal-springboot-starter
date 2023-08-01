package com.cpiwx.dataasyncspringbootstarter.config;

import cn.hutool.core.util.StrUtil;
import com.cpiwx.dataasyncspringbootstarter.anotations.CanalListener;
import com.cpiwx.dataasyncspringbootstarter.service.CanalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenPan
 * @date 2023-08-01 09:18
 **/
@Configuration
@Slf4j
public class CanalContext implements BeanPostProcessor {
    public static Map<String, List<CanalService>> handlers = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        handleCanalHandler(bean);
        return bean;
    }

    private void handleCanalHandler(Object bean) {
        Class<?> aClass = bean.getClass();
        boolean annotationPresent = aClass.isAnnotationPresent(CanalListener.class);
        if (annotationPresent && bean instanceof CanalService) {
            log.debug("发现Canal数据处理器{}", aClass.getName());
            CanalListener annotation = aClass.getAnnotation(CanalListener.class);
            String key = annotation.dbName() + StrUtil.COLON + annotation.tableName();
            List<CanalService> list = handlers.getOrDefault(key, new ArrayList<>());
            list.add((CanalService) bean);
            handlers.put(key, list);
        }
    }
}
