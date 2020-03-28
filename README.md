# POC Debezium from legacy to microservice

This is a proof of concept on how Debezium could help to migrate a legacy database centered monolith codebase to an event driver microservice architecture.
See the blog post I created for additional information on my [blog](https://michaelboke.nl/blog/from_legacy_monolith_to_microservice_with_debezium/).

The application is an example of how to map changes in the legacy database that come from legacy code to the new domain microservice.

I provided a docker composer file with the needed containers. After all the containers are started you need to start debezium.

Issue the following command to start debezium in Kafka connect.
```bash
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '
{ 
    "name": "demo-connector", 
    "config": { 
        "connector.class": "io.debezium.connector.mysql.MySqlConnector", 
        "tasks.max": "1", 
        "database.hostname": "db", 
        "database.port": "3306", 
        "database.user": "root", 
        "database.password": "test", 
        "database.server.id": "184054", 
        "database.server.name": "db",
        "database.allowPublicKeyRetrieval": "true", 
        "database.whitelist": "kafka_demo", 
        "database.history.kafka.bootstrap.servers": "kafka:29092", 
        "database.history.kafka.topic": "dbhistory.kafka_demo" 
    } 
}'
```

And  create the following table in the mysql database.
```sql
create table kafka_demo.orders
(
    id int auto_increment
        primary key,
    status enum('open', 'paid', 'closed') default 'open' null,
    _version int default 0 null
);
```

