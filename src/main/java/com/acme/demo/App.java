package com.acme.demo;

import com.acme.demo.models.*;
import com.acme.demo.serdes.SerdeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class App {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "debezium_orders_to_domain_commands_events");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final StreamsBuilder builder = new StreamsBuilder();

        //[de]serializers
        final Serde<DefaultId> idSerde = SerdeFactory.createDbzEventJsonPojoSerdeFor(DefaultId.class, true);
        final Serde<OrderChanges> orderChangeSerde = SerdeFactory.createDbzEventJsonPojoSerdeFor(OrderChanges.class, false);
        final Serde<CreateOrder> createdOrderSerde = SerdeFactory.createDbzEventJsonPojoSerdeFor(CreateOrder.class, false);
        final Serde<CompleteOrder> completeOrderSerde = SerdeFactory.createDbzEventJsonPojoSerdeFor(CompleteOrder.class, false);

        //this is the source we read from
        KStream<DefaultId, OrderChanges> source = builder.stream("db.kafka_demo.orders", Consumed.with(idSerde, orderChangeSerde))
                //filter empty
                .filter((defaultId, orderChanges) -> orderChanges != null)
                //filter out changes triggered by the domain
                .filter((defaultId, orderChanges) -> !orderChanges.isDomainChange());

        //print out if we receive a message
        source.print(Printed.toSysOut());

        //branch our results based on predicates
        KStream<DefaultId, OrderChanges>[] branches = source.branch(
                (defaultId, orderChanges) -> orderChanges.isCreated(),
                (defaultId, orderChanges) -> orderChanges.isCompleted()
        );

        //order created branch
        branches[0].mapValues((defaultId, orderChanges) -> new CreateOrder(
                defaultId.getId()))
                .to("legacy.commands", Produced.with(idSerde, createdOrderSerde));

        //order completed branch
        branches[1].mapValues((defaultId, orderChanges) -> new CompleteOrder(defaultId.getId()))
                .to("legacy.commands", Produced.with(idSerde, completeOrderSerde));

        /*
        // simple example for just matching one event
        source.filter((defaultId, orderChanges) -> orderChanges.IsCompleted())
                .mapValues((defaultId, orderChanges) -> new CompleteOrder(defaultId.getId()))
                .to("legacy.commands", Produced.with(idSerde, completeOrderSerde));
        */

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
