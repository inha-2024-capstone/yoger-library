package org.library.yogerLibrary.tsid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

@Slf4j
public class SequenceTsidGenerator implements IdentifierGenerator {

    private static final int DEFAULT_NODE_ID = 1;

    private static final int NODE_ID = setNodeId();

    private final Map<String, SequenceTsidFactory> tsidFactory = new ConcurrentHashMap<>();


    private static int setNodeId() {
        String envNodeId = System.getenv("NODE_ID");
        if (envNodeId == null || envNodeId.isBlank()) {
            log.info("NODE_ID is not set. Use default node id: {}", DEFAULT_NODE_ID);
            return DEFAULT_NODE_ID;
        }
        else return Integer.parseInt(envNodeId);
    }


    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        SequenceTsidFactory instance = getInstance(o.getClass().getSimpleName());
        return instance.getId();
    }

    private SequenceTsidFactory getInstance(String entityName) {
        return tsidFactory.computeIfAbsent(entityName, key -> new SequenceTsidFactory(NODE_ID));
    }

}
