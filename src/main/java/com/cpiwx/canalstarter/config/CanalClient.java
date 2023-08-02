package com.cpiwx.canalstarter.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.cpiwx.canalstarter.model.dto.CanalParseDTO;
import com.cpiwx.canalstarter.service.CanalService;
import com.cpiwx.canalstarter.utils.SqlHelper;
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

    private static boolean IS_HEALTHY = false;

    public CanalClient(CanalConnector connector, CanalProperties canalProperties) {
        this.connector = connector;
        this.canalProperties = canalProperties;
    }

    private void reconnect() {
        try {
            log.debug("断开连接...");
            this.close();
            log.debug("开始重连...");
            connector.connect();
            // 订阅数据库表，来覆盖服务端初始化时的设置
            connector.subscribe(canalProperties.getSubscribe());
            // 回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            IS_HEALTHY = true;
            log.debug("连接成功...");
        } catch (Exception e) {
            log.error("重连失败...", e);
            ThreadUtil.sleep(canalProperties.getTestIdleTime());
        }

    }

    @Async
    public void begin() {
            while (true) {
                if (!IS_HEALTHY) {
                    this.reconnect();
                    continue;
                }
                try {
                    tryMessage();
                } catch (Exception e) {
                    log.error("从canal消费消息出错", e);
                    IS_HEALTHY = false;
                }
            }
    }

    private void tryMessage() {
        // 获取指定数量的数据
        Message message = connector.getWithoutAck(BATCH_SIZE);
        // 获取批量ID
        long batchId = message.getId();
        // 获取批量的数量
        int size = message.getEntries().size();
        // 如果没有数据
        if (batchId == -1 || size == 0) {
            ThreadUtil.sleep(1000);
        } else {
            // 如果有数据，处理数据
            handleMessage(message.getEntries());
        }
        // 进行 batch id 的确认
        connector.ack(batchId);
    }

    private static void handleMessage(List<CanalEntry.Entry> entries) {
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
               log.error("解析canal数据出错", e);
               continue;
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
        if (connector != null) {
            connector.disconnect();
        }
    }
}
