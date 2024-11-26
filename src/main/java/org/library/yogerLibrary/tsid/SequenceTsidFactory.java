package org.library.yogerLibrary.tsid;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceTsidFactory {

    private static final int SEQUENCE_BIT = 12;
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BIT) - 1; // 시퀀스의 최대값 (12비트)
    private static final int NODE_BIT = 10;
    private static final int SEQUENCE_AND_NODE_BIT = SEQUENCE_BIT + NODE_BIT;

    private static final long STANDARD_EPOCH_MILLI = Instant.parse("2020-01-01T00:00:00.000Z").toEpochMilli();


    private final int nodeId; // 고유 노드 ID
    private final AtomicLong sequence = new AtomicLong(0); // 시퀀스 값
    private final AtomicLong lastTimestamp = new AtomicLong(-1L); // 마지막 타임스탬프


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
        while (true) {
            long currentTimestamp = getEpochMilli();
            long lastTs = lastTimestamp.get();

            if (currentTimestamp > lastTs) {
                // 타임스탬프가 변경되었으면 시퀀스를 0으로 초기화
                Optional<Long> newId = initializeSequenceAndGetId(lastTs, currentTimestamp);
                if (newId.isPresent()) return newId.get();
            }
            else if (currentTimestamp == lastTs) {
                // 같은 밀리초면 시퀀스를 증가
                long currentSequence = sequence.incrementAndGet();
                if (currentSequence <= MAX_SEQUENCE) {
                    return generateId(currentTimestamp, currentSequence);
                }
                else {
                    // 시퀀스가 최대값을 초과하면 타임스탬프 갱신 대기
                    while (currentTimestamp <= lastTs) {
                        currentTimestamp = getEpochMilli();
                    }
                    // 타임스탬프가 변경되면 루프 다시 시작
                }
            }
        }
    }

    private long getEpochMilli() {
        return System.currentTimeMillis() - STANDARD_EPOCH_MILLI;
    }

    private synchronized Optional<Long> initializeSequenceAndGetId(long lastTs, long currentTimestamp) {
        if (lastTimestamp.compareAndSet(lastTs, currentTimestamp)) {
            sequence.set(-1L);
            return Optional.of(generateId(currentTimestamp, sequence.incrementAndGet()));
        }
        else return Optional.empty();
    }

    // Snowflake 스타일 ID 생성 (시간, 노드, 시퀀스 결합)
    private long generateId(long timestamp, long sequence) {
        return (timestamp << SEQUENCE_AND_NODE_BIT) // 상위 41비트에 타임스탬프
                | ((long) nodeId << SEQUENCE_BIT) // 중간 10비트에 노드 ID
                | sequence; // 하위 12비트에 시퀀스
    }
}

