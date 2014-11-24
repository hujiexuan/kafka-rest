/**
 * Copyright 2014 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.kafkarest.integration;

import io.confluent.kafkarest.entities.Topic;
import kafka.utils.TestUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConversions;

import java.util.*;

public class ConsumerTest extends AbstractConsumerTest {
    private static final String topicName = "topic1";
    private static final Topic topic = new Topic(topicName, 1);
    private static final String groupName = "testconsumergroup";

    private final List<ProducerRecord> recordsOnlyValues = Arrays.asList(
            new ProducerRecord(topicName, "value".getBytes()),
            new ProducerRecord(topicName, "value2".getBytes()),
            new ProducerRecord(topicName, "value3".getBytes()),
            new ProducerRecord(topicName, "value4".getBytes())
    );

    private final List<ProducerRecord> recordsWithKeys = Arrays.asList(
            new ProducerRecord(topicName, "key".getBytes(), "value".getBytes()),
            new ProducerRecord(topicName, "key".getBytes(), "value2".getBytes()),
            new ProducerRecord(topicName, "key".getBytes(), "value3".getBytes()),
            new ProducerRecord(topicName, "key".getBytes(), "value4".getBytes())
    );


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final int numPartitions = 3;
        final int replicationFactor = 1;
        TestUtils.createTopic(zkClient, topicName, numPartitions, replicationFactor, JavaConversions.asScalaIterable(this.servers).toSeq(), new Properties());
    }

    @Test
    public void testConsumeOnlyValues() {
        String instanceUri = startConsumeMessages(groupName, topicName);
        produceMessages(recordsOnlyValues);
        consumeMessages(instanceUri, topicName, recordsOnlyValues);
    }

    @Test
    public void testConsumeWithKeys() {
        String instanceUri = startConsumeMessages(groupName, topicName);
        produceMessages(recordsWithKeys);
        consumeMessages(instanceUri, topicName, recordsWithKeys);
    }

    @Test
    public void testConsumeInvalidTopic() {
        startConsumeMessages(groupName, "nonexistenttopic", true);
    }

    @Test
    public void testConsumeTimeout() {
        String instanceUri = startConsumeMessages(groupName, topicName);
        produceMessages(recordsWithKeys);
        consumeMessages(instanceUri, topicName, recordsWithKeys);
        consumeForTimeout(instanceUri, topicName);
    }

    @Test
    public void testDeleteConsumer() {
        String instanceUri = startConsumeMessages(groupName, topicName);
        produceMessages(recordsWithKeys);
        consumeMessages(instanceUri, topicName, recordsWithKeys);
        deleteConsumer(instanceUri);
    }
}
