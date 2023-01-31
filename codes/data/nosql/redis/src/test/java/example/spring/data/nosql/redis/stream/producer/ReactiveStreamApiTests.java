/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.spring.data.nosql.redis.stream.producer;

import example.spring.data.nosql.redis.stream.consumer.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.redis.connection.stream.StreamOffset.fromStart;

/**
 * @author Christoph Strobl
 */
@SpringBootTest
public class ReactiveStreamApiTests {

    @Autowired
    ReactiveStringRedisTemplate template;
    @Autowired
    StreamReceiver<String, MapRecord<String, String, String>> streamReceiver;

    ReactiveStreamOperations<String, String, String> streamOps;

    @BeforeEach
    public void setUp() {

        // clear all
        template.getConnectionFactory().getReactiveConnection()
                .serverCommands().flushAll()
                .then().as(StepVerifier::create)
                .verifyComplete();

        streamOps = template.opsForStream();
    }

    @Test
    public void basics() {

        // XADD with fixed id
        streamOps.add(SensorData.RECORD_1234_0)
                 .as(StepVerifier::create)
                 .expectNext(SensorData.RECORD_1234_0.getId()).verifyComplete();

        streamOps.add(SensorData.RECORD_1234_1)
                 .as(StepVerifier::create)
                 .expectNext(SensorData.RECORD_1234_1.getId()).verifyComplete();

        // XLEN
        streamOps.size(SensorData.KEY)
                 .as(StepVerifier::create)
                 .expectNext(2L).verifyComplete();

        // XADD errors when timestamp is less then last inserted
        streamOps.add(SensorData.create("1234", "19.8", "invalid").withId(RecordId.of("0-0")))
                 .as(StepVerifier::create)
                 .verifyError(RedisSystemException.class);

        // XADD with autogenerated id
        streamOps.add(SensorData.create("1234", "19.8", null))
                 .as(StepVerifier::create)
                 .consumeNextWith(autogeneratedId -> autogeneratedId.getValue().endsWith("-0")).verifyComplete();

        streamOps.size(SensorData.KEY)
                 .as(StepVerifier::create)
                 .expectNext(3L).verifyComplete();

        // XREAD from start
        streamOps.read(fromStart(SensorData.KEY))
                 .map(MapRecord::getId)
                 .as(StepVerifier::create)
                 .expectNext(SensorData.RECORD_1234_0.getId(), SensorData.RECORD_1234_1.getId())
                 .expectNextCount(1).verifyComplete();

        // XREAD resume after
        streamOps.read(StreamOffset.create(SensorData.KEY, ReadOffset.from(SensorData.RECORD_1234_1.getId())))
                 .as(StepVerifier::create)
                 .expectNextCount(1).verifyComplete();
    }

    @Test
    public void continuousRead() {

        Flux<MapRecord<String, String, String>> messages = streamReceiver.receive(fromStart(SensorData.KEY));

        messages.as(StepVerifier::create)
                .then(() ->
                    streamOps.add(SensorData.RECORD_1234_0)
                             .then(streamOps.add(SensorData.RECORD_1234_1))
                             .subscribe())
                .consumeNextWith(it -> {
                    assertThat(it.getId()).isEqualTo(SensorData.RECORD_1234_0.getId());
                })
                .consumeNextWith(it -> {
                    assertThat(it.getId()).isEqualTo(SensorData.RECORD_1234_1.getId());
                })
                .then(() -> streamOps.add(SensorData.RECORD_1235_0)
                                     .subscribe())
                .consumeNextWith(it -> {
                    assertThat(it.getId()).isEqualTo(SensorData.RECORD_1235_0.getId());
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

}
