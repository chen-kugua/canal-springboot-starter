package com.cpiwx.dataasyncspringbootstarter.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.cpiwx.dataasyncspringbootstarter.model.dto.CanalParseDTO;
import com.cpiwx.dataasyncspringbootstarter.service.CanalService;
import com.cpiwx.dataasyncspringbootstarter.utils.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @author chenPan
 * @date 2023-08-01 10:14
 **/
@Slf4j
public class CanalClient {
    private final CanalConnector connector;
    private final CanalProperties canalProperties;

    private final static int BATCH_SIZE = 1000;

    public CanalClient(CanalConnector connector, CanalProperties canalProperties) {
        this.connector = connector;
        this.canalProperties = canalProperties;
    }

    private void reconnect() {
        log.debug("断开连接...");
        connector.disconnect();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("sleep异常", e);
        }
        log.debug("开始重连...");
        connector.connect();
    }

    @Async
    public void checkValidate() {
        while (true) {
            log.debug("开始校验连接是否有效...");
            try {
                boolean isValidate = connector.checkValid();
                if (!isValidate) {
                    this.reconnect();
                }
            } catch (CanalClientException e) {
                log.error("CanalClientException", e);
                this.reconnect();
            }
            try {
                Thread.sleep(canalProperties.getTestIdleTime());
            } catch (InterruptedException e) {
                log.error("sleep异常", e);
            }
        }

    }

    @Async
    public void handleMessage() {
        try {
            connector.connect();
            // 订阅数据库表，来覆盖服务端初始化时的设置
            connector.subscribe(canalProperties.getSubscribe());
            // 回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                // 获取批量ID
                long batchId = message.getId();
                // 获取批量的数量
                int size = message.getEntries().size();
                // 如果没有数据
                if (batchId == -1 || size == 0) {
                    try {
                        // 线程休眠2秒
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("睡眠中异常", e);
                    }
                } else {
                    // 如果有数据，处理数据
                    printEntry(message.getEntries());
                }
                // 进行 batch id 的确认
                connector.ack(batchId);
            }
        } catch (Exception e) {
            log.error("canal异常", e);
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                    || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                // 开启/关闭事务的实体类型，跳过
                continue;
            }
            // RowChange对象，包含了一行数据变化的所有特征
            CanalEntry.RowChange rowChange;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("解析canal数据出错", e);
            }
            // 获取操作类型：insert/update/delete类型
            CanalEntry.EventType eventType = rowChange.getEventType();
            CanalEntry.Header header = entry.getHeader();
            String dbName = header.getSchemaName();
            String tableName = header.getTableName();
            // 打印Header信息
            log.info("binlog[{}:{}] , name[{},{}] , eventType : {}",
                    header.getLogfileName(), header.getLogfileOffset(), dbName, tableName, eventType);
            // 处理器
            String handlerKey = dbName + StrUtil.COLON + tableName;
            List<CanalService> handlers = CanalContext.handlers.get(handlerKey);
            if (CollUtil.isEmpty(handlers)) {
                log.debug("没有可用的数据处理器，跳过：{}", handlerKey);
                return;
            }
            CanalParseDTO dto = new CanalParseDTO()
                    .setDbName(dbName)
                    .setTableName(tableName)
                    .setEventType(eventType);
            // 判断是否是DDL语句
            if (rowChange.getIsDdl()) {
                log.info("isDdl: true,sql:" + rowChange.getSql());
                dto.setSql(rowChange.getSql());
                handlers.forEach(handler -> handler.handleDdl(dto));
                return;
            }
            // 获取RowChange对象里的每一行数据，打印出来
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                // 如果是删除语句
                if (eventType == CanalEntry.EventType.DELETE) {
                    // 删除
                    String sql = SqlHelper.getDelete(dbName, tableName, rowData.getBeforeColumnsList());
                    dto.setSql(sql);
                    handlers.forEach(handler -> handler.handleDelete(dto));
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    // 插入
                    String sql = SqlHelper.getInsert(dbName, tableName, rowData.getAfterColumnsList());
                    dto.setSql(sql);
                    handlers.forEach(handler -> handler.handleInsert(dto));
                } else {
                    // 修改
                    // 变更前的数据
                    // printColumn(rowData.getBeforeColumnsList());
                    // 变更后的数据
                    // printColumn(rowData.getAfterColumnsList());
                    String sql = SqlHelper.getUpdate(dbName, tableName, rowData.getAfterColumnsList());
                    dto.setSql(sql);
                    handlers.forEach(handler -> handler.handleUpdate(dto));
                }
            }
        }
    }

    public void close() {
        log.debug("close...");
        connector.disconnect();
    }
}
