package com.cpiwx.canalstarter.service;

import com.cpiwx.canalstarter.model.dto.CanalParseDTO;

/**
 * @author chenPan
 * @date 2023-08-01 09:19
 **/
public interface CanalService {
    /**
     * 插入操作
     *
     * @param dto {@link CanalParseDTO}
     */
    void handleInsert(CanalParseDTO dto);

    /**
     * 更新操作
     *
     * @param dto {@link CanalParseDTO}
     */
    void handleUpdate(CanalParseDTO dto);

    /**
     * 删除操作
     *
     * @param dto {@link CanalParseDTO}
     */
    void handleDelete(CanalParseDTO dto);

    /**
     * 处理ddl语句 建表、索引。。。
     *
     * @param dto {@link CanalParseDTO}
     */
    void handleDdl(CanalParseDTO dto);
}
