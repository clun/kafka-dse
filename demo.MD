
 DEMO
 
 curl -s \
     -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
      "name": "dse_tx_flat",
      "config": {
         "connector.class": "com.datastax.kafkaconnector.DseSinkConnector",
                "tasks.max": "1",
                "topics": "stocks_ticks",
                "contactPoints": "dse",
                "loadBalancing.localDc": "DC1",
                "port": 9042,
                "maxConcurrentRequests": 500,
                "maxNumberOfRecordsInBatch": 32,
                "queryExecutionTimeout": 30,
                "connectionPoolLocalSize": 4,
                "jmx": false,
                "compression": "None",
                "auth.provider": "None",s
                "auth.username": "",
                "auth.password": "",
                "auth.gssapi.keyTab": "",
                "auth.gssapi.principal": "",
                "auth.gssapi.service": "dse",
                "ssl.provider": "None",
                "ssl.hostnameValidation": true,
                "ssl.keystore.password": "",
                "ssl.keystore.path": "",
                "ssl.openssl.keyCertChain": "",
                "ssl.openssl.privateKey": "",
                "ssl.truststore.password": "",
                "ssl.truststore.path": "",
                "ssl.cipherSuites": "",
                "topic.stocks_ticks.demo_kafka.stocks_ticks.mapping": "symbol=value.symbol, value=value.value, value_date=value.valueDate",
                "topic.stocks_ticks.demo_kafka.stocks_ticks.consistencyLevel": "LOCAL_ONE",
                "topic.stocks_ticks.demo_kafka.stocks_ticks.ttl": -1,
                "topic.stocks_ticks.demo_kafka.stocks_ticks.nullToUnset": "true",
                "topic.stocks_ticks.demo_kafka.stocks_ticks.deletesEnabled": "true",
                "topic.stocks_ticks.codec.locale": "en_US",
                "topic.stocks_ticks.codec.timeZone": "UTC",
                "topic.stocks_ticks.codec.timestamp": "CQL_TIMESTAMP",
                "topic.stocks_ticks.codec.date": "ISO_LOCAL_DATE",
                "topic.stocks_ticks.codec.time": "ISO_LOCAL_TIME",
                "topic.stocks_ticks.codec.unit": "MILLISECONDS"
            }
        }
        
-  