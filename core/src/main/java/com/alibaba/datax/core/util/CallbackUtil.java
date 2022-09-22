package com.alibaba.datax.core.util;

import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 信息回调
 * @author liudi
 */
public class CallbackUtil {
    private static String topic = "datax-notify";
    private static final String TOTAL_READ_BYTES = "totalReadBytes";

    public static void start() {
        Map<String,Object> map = new HashMap<>(16);
        map.put("process",getProcessId());
        KafkaUtil.send(topic,map);
    }

    private static Integer getProcessId() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMxBean.getName().split("@")[0]);
    }

    public static void error(Communication reportCommunication) {
        String snapshot = CommunicationTool.Stringify.getSnapshot(reportCommunication);
        Map<String,Object> map = new HashMap<>(16);
        map.put("process",getProcessId());
        map.put("snapshot",snapshot);
        KafkaUtil.send(topic,map);
    }

    public static void success(Communication reportCommunication) {
        Map<String,Object> map = new HashMap<>(16);
        map.put("process",getProcessId());
        // 总读取记录数
        long totalReadRecords = CommunicationTool.getTotalReadRecords(reportCommunication);
        // 失败记录数
        long totalErrorRecords = CommunicationTool.getTotalErrorRecords(reportCommunication);
        // 成功数据大小
        long writeSucceedBytes = CommunicationTool.getWriteSucceedBytes(reportCommunication);
        // 总数据量大小
        Long totalReadBytes = reportCommunication.getLongCounter(TOTAL_READ_BYTES);
        map.put("totalReadRecords",totalReadRecords);
        map.put("totalErrorRecords",totalErrorRecords);
        map.put("writeSucceedBytes",writeSucceedBytes);
        map.put("totalReadBytes",totalReadBytes);
        KafkaUtil.send(topic,map);
    }

}
