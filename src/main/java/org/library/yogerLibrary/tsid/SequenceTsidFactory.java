package org.library.yogerLibrary.tsid;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class SequenceTsidFactory {

    private static final int SEQUENCE_BIT = 12;
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BIT) - 1; // 시퀀스의 최대값 (12비트)
    private static final int NODE_BIT = 10;
    private static final int SEQUENCE_AND_NODE_BIT = SEQUENCE_BIT + NODE_BIT;

    private static final long STANDARD_EPOCH_MILLI = Instant.parse("2020-01-01T00:00:00.000Z").toEpochMilli();


    private final int nodeId; // 고유 노드 ID
    //private final AtomicLong sequence = new AtomicLong(0); // 시퀀스 값
    //private final AtomicLong lastTimestamp = new AtomicLong(-1L); // 마지막 타임스탬프

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private final ReentrantLock lock = new ReentrantLock();


    public SequenceTsidFactory(int nodeId) {
        validateNodeId(nodeId);
        this.nodeId = nodeId;
    }

    private void validateNodeId(int nodeId) {
        if (nodeId < 0 || nodeId >= (1 << NODE_BIT)) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + ((1 << NODE_BIT) - 1));
        }
    }

    public long getId() {
        lock.lock();
        try {
            long currentTimestamp = getEpochMilli();

            // 1. 시간이 역전된 경우 (Clock Drift 방어)
            if (currentTimestamp < lastTimestamp) {
                throw new IllegalStateException("Clock moved backwards.");
            }

            // 2. 같은 밀리초인 경우
            if (currentTimestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 시퀀스 초과 시 다음 밀리초까지 대기
                if (sequence == 0) {
                    currentTimestamp = waitNextMillis(lastTimestamp);
                }
            }
            else { // 3. 시간이 흐른 경우
                sequence = 0L;
            }

            lastTimestamp = currentTimestamp;

            return generateId(currentTimestamp, sequence);

        } finally {
            lock.unlock();
        }
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getEpochMilli();
        while (timestamp <= lastTimestamp) {
            timestamp = getEpochMilli();
        }
        return timestamp;
    }

    private long getEpochMilli() {
        return System.currentTimeMillis() - STANDARD_EPOCH_MILLI;
    }

    // Snowflake 스타일 ID 생성 (시간, 노드, 시퀀스 결합)
    private long generateId(long timestamp, long sequence) {
        return (timestamp << SEQUENCE_AND_NODE_BIT) // 상위 41비트에 타임스탬프
                | ((long) nodeId << SEQUENCE_BIT) // 중간 10비트에 노드 ID
                | sequence; // 하위 12비트에 시퀀스
    }
}

