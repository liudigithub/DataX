package com.alibaba.datax.core.util;

import com.alibaba.datax.core.util.container.CoreConstant;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author liudi
 */
public class KafkaUtil {
    private static Properties properties;
    static{
        init();
    }

    private static void init() {
        properties = new Properties();
        try {
            InputStream in = new FileInputStream(CoreConstant.DATAX_CONF_KAFKA_PATH);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(String topic,Object message) {
        try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>(topic, JSON.toJSONString(message)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
