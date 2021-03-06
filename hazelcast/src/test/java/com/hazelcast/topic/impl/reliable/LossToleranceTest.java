package com.hazelcast.topic.impl.reliable;

import com.hazelcast.config.Config;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class LossToleranceTest extends HazelcastTestSupport {

    private ReliableTopicProxy<String> topic;
    private Ringbuffer<ReliableTopicMessage> ringbuffer;

    @Before
    public void setup() {
        Config config = new Config();
        config.addRingBufferConfig(new RingbufferConfig("foo")
                .setCapacity(100)
                .setTimeToLiveSeconds(0));
        HazelcastInstance hz = createHazelcastInstance(config);

        topic = (ReliableTopicProxy<String>) hz.<String>getReliableTopic("foo");
        ringbuffer = topic.ringbuffer;
    }

    @Test
    public void whenNotLossTolerant_thenTerminate() {
        ReliableMessageListenerMock listener = new ReliableMessageListenerMock();
        listener.initialSequence = 0;
        listener.isLossTolerant = false;

        topic.publish("foo");

        // we add so many items that the items the listener wants to listen to, doesn't exist anymore
        for (; ; ) {
            topic.publish("item");
            if (ringbuffer.headSequence() > listener.initialSequence) {
                break;
            }
        }
        topic.addMessageListener(listener);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertTrue(topic.runnersMap.isEmpty());
            }
        });
    }

    @Test
    public void whenLossTolerant_thenContinue() {
        final ReliableMessageListenerMock listener = new ReliableMessageListenerMock();
        listener.initialSequence = 0;
        listener.isLossTolerant = true;

        // we add so many items that the items the listener wants to listen to, doesn't exist anymore
        for (; ; ) {
            topic.publish("item");
            if (ringbuffer.headSequence() > listener.initialSequence) {
                break;
            }
        }

        topic.addMessageListener(listener);
        topic.publish("newItem");

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertContains(listener.objects, "newItem");
                assertFalse(topic.runnersMap.isEmpty());
            }
        });
    }
}
