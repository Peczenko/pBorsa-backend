package com.pborsa.api.shared.config.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Temporal client (used by API to start workflows).
 * Does not create workers - workers run in separate pborsa-trading-worker module.
 */
@Configuration
@Slf4j
public class TemporalConfiguration {
    
    @Value("${spring.temporal.connection.target:localhost:7233}")
    private String temporalTarget;
    
    @Value("${spring.temporal.namespace:default}")
    private String namespace;
    
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        log.info("Creating Temporal service stubs connecting to: {}", temporalTarget);
        return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalTarget)
                .build()
        );
    }
    
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        log.info("Creating Temporal workflow client for namespace: {}", namespace);
        return WorkflowClient.newInstance(serviceStubs,
            WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build()
        );
    }

    /**
     * Creates the worker factory (used by API-hosted workers such as strategy execution).
     */
    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }
}

