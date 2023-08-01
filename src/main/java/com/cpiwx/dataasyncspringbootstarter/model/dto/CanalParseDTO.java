package com.cpiwx.dataasyncspringbootstarter.model.dto;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author chenPan
 * @date 2023-08-01 09:23
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CanalParseDTO {
    /**
     * 原始数据
     */
    private CanalEntry.RowChange rowChange;
    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 操作类型 INSERT DELETE UPDATE ALTER CINDEX
     */
    private CanalEntry.EventType eventType;

    /**
     * 拼接成的sql
     */
    private String sql;
}
