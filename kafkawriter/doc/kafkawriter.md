# DataX KafkaWriter


---


## 1 快速介绍

KafkaWriter 插件实现了写入数据到 kafka队列的功能。在底层实现上， KafkaWriter 通过 kafka-clients 建立连接，并执行相应的 send 方法发送到指定的topic中，数据使用fastjson转换为string进行传输。


## 2 实现原理

KafkaWriter 通过 DataX 框架获取 Reader 生成的协议数据，根据你配置的 `servers` 与 `topic` 发送数据到指定的服务的topic中

<br />

    注意：如果需要配置相关的连接参数，可以将配置写入 `prop` 属性中


## 3 功能说明

### 3.1 配置样例

* 这里使用一份从内存产生到 Mysql 导入的数据。

```json
{
    "job": {
        "setting": {
            "speed": {
                "channel": 1
            }
        },
        "content": [
            {
                 "reader": {
                    "name": "streamreader",
                    "parameter": {
                        "column" : [
                            {
                                "value": "DataX",
                                "type": "string"
                            },
                            {
                                "value": 19880808,
                                "type": "long"
                            },
                            {
                                "value": "1988-08-08 08:08:08",
                                "type": "date"
                            },
                            {
                                "value": true,
                                "type": "bool"
                            },
                            {
                                "value": "test",
                                "type": "bytes"
                            }
                        ],
                        "sliceRecordCount": 10
                    }
                },
                "writer": {
                    "name": "kafkawriter",
                    "parameter": {
                        "servers": "192.168.80.223:30092,192.168.80.223:30093,192.168.80.223:30094",
                        "prop": {
                          "key.serializer": "org.apache.kafka.common.serialization.StringSerializer",
                          "value.serializer": "org.apache.kafka.common.serialization.StringSerializer"
                        },
                        "topic": "liudi_test",
                        "column": [
                            "name",
                            "id",
                            "createtime$yyyy-MM-dd hh:mm:ss",
                            "flag",
                            "remark"
                        ]
                    }
                }
            }
        ]
    }
}

```


### 3.2 参数说明

* **servers**

    * 描述：kafka连接信息。

 	* 必选：是 <br />

	* 默认值：无 <br />

* **prop**

    * 描述：kafka连接参数 <br />

    * 必选：否 <br />

    * 默认值：

               "key.serializer": "org.apache.kafka.common.serialization.StringSerializer",
               "value.serializer": "org.apache.kafka.common.serialization.StringDeserializer"

* **topic**

    * 描述：发送到的主题 <br />

    * 必选：是 <br />

    * 默认值：无 <br />
    
* **column**

    * 描述：需要写入的字段,字段之间用英文逗号分隔。例如: "column": ["id","name","age"]。
  其中日期类型字段可以通过传入format进行格式化，语法为：`columnName$format`，使用`$`进行分割，前者为字段名，后者为format格式化模板。
  注意：如果日期类型不设置格式化，则会以long形式存储。
    
    * 必选：是 <br />

    * 默认值：否 <br />

### 3.3 类型转换

所有数据都以Map<String,Object>形式，依次将数据按顺序加入到map中，key为column，value为传递Record

## 4 性能报告

## 5 约束限制

## FAQ

