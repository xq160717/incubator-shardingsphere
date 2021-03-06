# 用户手册
## 环境要求
纯JAVA开发，JDK建议1.8以上版本。

支持迁移场景如下：

| 源端       | 目标端         | 是否支持 |
| ---------- | -------------- | -------- |
| MySQL      | sharding-proxy | 支持     |
| PostgreSQL | sharding-proxy | 支持     |

## API接口
### 创建迁移任务
接口描述：POST /shardingscaling/job/start

请求体：

| Parameter                                         | Describe                                                     |
| ------------------------------------------------- | ------------------------------------------------------------ |
| ruleConfiguration.sourceDatasource                | 源端sharding proxy数据源相关配置                             |
| ruleConfiguration.sourceRule                      | 源端sharding proxy表规则相关配置                             |
| ruleConfiguration.destinationDataSources.name     | 目标端sharding proxy名称                                     |
| ruleConfiguration.destinationDataSources.url      | 目标端sharding proxy jdbc url                                |
| ruleConfiguration.destinationDataSources.username | 目标端sharding proxy用户名                                   |
| ruleConfiguration.destinationDataSources.password | 目标端sharding proxy密码                                     |
| jobConfiguration.concurrency                      | 迁移并发度，举例：如果设置为3，则待迁移的表将会有三个线程同时对该表进行迁移，前提是该表有整数型主键 |

示例：

```
curl -X POST \
  http://localhost:8888/shardingscaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  properties:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\''\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
      "sourceRule": "defaultDatabaseStrategy:\n  inline:\n    algorithmExpression: ds_${user_id % 2}\n    shardingColumn: user_id\ntables:\n  t1:\n    actualDataNodes: ds_0.t1\n    keyGenerator:\n      column: order_id\n      type: SNOWFLAKE\n    logicTable: t1\n    tableStrategy:\n      inline:\n        algorithmExpression: t1\n        shardingColumn: order_id\n  t2:\n    actualDataNodes: ds_0.t2\n    keyGenerator:\n      column: order_item_id\n      type: SNOWFLAKE\n    logicTable: t2\n    tableStrategy:\n      inline:\n        algorithmExpression: t2\n        shardingColumn: order_id\n",
      "destinationDataSources": {
         "name": "dt_0",
         "password": "123456",
         "url": "jdbc:mysql://127.0.0.1:3306/test2?serverTimezone=UTC&useSSL=false",
         "username": "root"
      }
   },
   "jobConfiguration": {
      "concurrency": 3
   }
}'
```

返回信息：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

### 查询迁移任务进度
接口描述：GET /shardingscaling/job/progress/{jobId}

示例：
```
curl -X GET \
  http://localhost:8888/shardingscaling/job/progress/1
```

返回信息：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": {
        "id": 1,
        "jobName": "Local Sharding Scaling Job",
        "status": "RUNNING/STOPPED"
        "syncTaskProgress": [{
            "id": "127.0.0.1-3306-test",
            "status": "PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED",
            "historySyncTaskProgress": [{
                "id": "history-test-t1#0",
                "estimatedRows": 41147,
                "syncedRows": 41147
            }, {
                "id": "history-test-t1#1",
                "estimatedRows": 42917,
                "syncedRows": 42917
            }, {
                "id": "history-test-t1#2",
                "estimatedRows": 43543,
                "syncedRows": 43543
            }, {
                "id": "history-test-t2#0",
                "estimatedRows": 39679,
                "syncedRows": 39679
            }, {
                "id": "history-test-t2#1",
                "estimatedRows": 41483,
                "syncedRows": 41483
            }, {
                "id": "history-test-t2#2",
                "estimatedRows": 42107,
                "syncedRows": 42107
            }],
            "realTimeSyncTaskProgress": {
                "id": "realtime-test",
                "delayMillisecond": 1576563771372,
                "logPosition": {
                    "filename": "ON.000007",
                    "position": 177532875,
                    "serverId": 0
                }
            }
        }]
   }
}
```

### 查询所有迁移任务
接口描述：GET /shardingscaling/job/list

示例：
```
curl -X GET \
  http://localhost:8888/shardingscaling/job/list
```

返回信息：

```
{
  "success": true,
  "errorCode": 0,
  "model": [
    {
      "jobId": 1,
      "jobName": "Local Sharding Scaling Job",
      "status": "RUNNING"
    }
  ]
}
```

### 停止迁移任务
接口描述：POST /shardingscaling/job/stop

请求体：

| Parameter | Describe |
| --------- | -------- |
| jobId     | job id   |

示例：
```
curl -X POST \
  http://localhost:8888/shardingscaling/job/stop \
  -H 'content-type: application/json' \
  -d '{
   "jobId":1
}'
```
返回信息：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

## 应用配置项
应用现有配置项如下，相应的配置可在`conf/server.yaml`中修改：
| 名称           | 说明                                         | 默认值 |
| -------------- | -------------------------------------------- | ------ |
| port           | Http服务监听端口                             | 8888   |
| blockQueueSize | 数据传输通道队列大小                         | 10000  |
| pushTimeout    | 数据推送超时时间，单位ms                     | 1000   |
| workerThread   | 工作线程池大小，允许同时运行的迁移任务线程数 | 30     |
