package com.icss.datax.plugin.writer.kafkawriter;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.Types;
import java.util.*;

/**
 * @author liudi
 */
public class KafkaWriter extends Writer {

    public static class Job extends Writer.Job {
        private Configuration originalConfig = null;
        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();
        }

        @Override
        public void destroy() {

        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> configList = new ArrayList<Configuration>();
            for(int i = 0; i < mandatoryNumber; i++) {
                configList.add(this.originalConfig.clone());
            }
            return configList;
        }
    }

    public static class Task extends Writer.Task {
        private Configuration writerSliceConfig;
        private JSONObject props;
        private Producer<String, String> producer;
        private List<String> columns;
        private String topic;
        @Override
        public void init() {
            // 获取配置
            this.writerSliceConfig = this.getPluginJobConf();
            String servers = writerSliceConfig.getString("servers");
            this.topic = writerSliceConfig.getString("topic");
            this.props = JSON.parseObject(writerSliceConfig.getString("prop"));
            this.columns = writerSliceConfig.getList("column", String.class);
            // 建立连接
            Properties properties = new Properties();
            properties.putAll(this.props);
            properties.put("bootstrap.servers",servers);
            producer = new KafkaProducer<>(properties);
        }

        @Override
        public void destroy() {
            // 关闭连接
            if(producer != null){
                producer.close();
            }
        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            // 发送数据
            Record record;
            while((record = lineReceiver.getFromReader()) != null) {
                try{
                    send(record);
                }catch (Exception e){
                    TaskPluginCollector taskPluginCollector = super.getTaskPluginCollector();
                    taskPluginCollector.collectDirtyRecord(record,e);
                }
            }
        }

        private void send(Record record) {
            int columnNumber = record.getColumnNumber();
            Map<String,Object> data = new HashMap<>(16);
            for (int i = 0; i < columnNumber; i++) {
                String columnName = columns.get(i);
                Column column = record.getColumn(i);
                if(Column.Type.DATE.equals(column.getType()) && column.getRawData()!=null){
                    // eg:createtime$yyyy-MM-dd hh:mm:ss
                    // $分割，前者为字段名，后者为格式化
                    String[] split = columnName.split("\\$");
                    if(split.length == 2){
                        String formatValue = DateFormatUtils.format(column.asDate(), split[1]);
                        data.put(split[0],formatValue);
                    }
                }else{
                    data.put(columnName,column.getRawData());
                }
            }
            producer.send(new ProducerRecord<>(topic,JSON.toJSONString(data)));
        }
    }
}
