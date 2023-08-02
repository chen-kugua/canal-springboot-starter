package com.cpiwx.canalstarter.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author chenPan
 * @date 2023-07-31 18:12
 **/
@Slf4j
public class SqlHelper {

    public static String getInsert(String dbName, String tableName, List<CanalEntry.Column> list) {
        String template = "INSERT INTO {}{} VALUES {}";
        StringJoiner names = new StringJoiner(",", "(", ")");
        StringJoiner values = new StringJoiner(",", "(", ")");
        for (CanalEntry.Column column : list) {
            names.add(column.getName());
            values.add(column.getValue());
        }
        String sql = StrUtil.format(template, tableName, names.toString(), values.toString());
        log.debug("insert sql:{}", sql);
        return sql;
    }

    public static String getUpdate(String dbName, String tableName, List<CanalEntry.Column> list) {
        String template = "UPDATE {} SET {} WHERE {}";
        String where = "";
        StringJoiner values = new StringJoiner(",");
        for (CanalEntry.Column column : list) {
            if (column.getIsKey()) {
                where = column.getName() + "=" + column.getValue();
            } else {
                values.add(column.getName() + "=" + column.getValue());
            }
        }
        String sql = StrUtil.format(template, tableName, values.toString(), where);
        log.debug("UPDATE SQL:{}", sql);
        return sql;
    }

    public static String getDelete(String dbName, String tableName, List<CanalEntry.Column> list) {
        String template = "DELETE FROM {} WHERE {}";
        String where = "";
        for (CanalEntry.Column column : list) {
            if (column.getIsKey()) {
                where = column.getName() + "=" + column.getValue();
                break;
            }
        }
        String sql = StrUtil.format(template, tableName, where);
        log.debug("DELETE SQL:{}",sql);
        return sql;
    }
}
