package com.cpiwx.canalstarter.service.impl;

import com.cpiwx.canalstarter.anotations.CanalListener;
import com.cpiwx.canalstarter.model.dto.CanalParseDTO;
import com.cpiwx.canalstarter.service.CanalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chenPan
 * @date 2023-08-01 09:44
 **/
@CanalListener(dbName="canal_manager",tableName = "t_test")
@Slf4j
@Service
public class CanalServiceDefaultImpl implements CanalService {
    /**
     * 插入操作
     *
     * @param dto {@link CanalParseDTO}
     */
    @Override
    public void handleInsert(CanalParseDTO dto) {
        log.info("handleInsert:{}", dto.getSql());
    }

    /**
     * 更新操作
     *
     * @param dto {@link CanalParseDTO}
     */
    @Override
    public void handleUpdate(CanalParseDTO dto) {
        log.info("handleUpdate:{}", dto.getSql());
    }

    /**
     * 删除操作
     *
     * @param dto {@link CanalParseDTO}
     */
    @Override
    public void handleDelete(CanalParseDTO dto) {
        log.info("handleDelete:{}", dto.getSql());
    }

    /**
     * 处理ddl语句 建表、索引。。。
     *
     * @param dto {@link CanalParseDTO}
     */
    @Override
    public void handleDdl(CanalParseDTO dto) {
        log.info("handleDdl:{}", dto);
    }
}
