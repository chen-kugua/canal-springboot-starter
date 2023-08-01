package com.cpiwx.dataasyncspringbootstarter.anotations;

import com.cpiwx.dataasyncspringbootstarter.config.CanalAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenPan
 * @date 2023-07-31 11:16
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CanalAutoConfig.class)
public @interface EnableCanal {
}
