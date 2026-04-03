package com.pborsa.api.strategy.execution;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic utility for batching data items and processing them in chunks.
 * Reusable across different data types (bars, quotes, trades, etc.).
 */
@Component
public class DataBatcher {

    /**
     * Processes a list of items in batches, transforming each item before batching.
     *
     * @param items         source items to process
     * @param batchSize     maximum items per batch
     * @param transformer   function to transform each source item to target type
     * @param batchConsumer consumer for each batch of transformed items
     * @param <S>           source item type
     * @param <T>           target item type
     */
    public <S, T> void processBatches(
            List<S> items,
            int batchSize,
            Function<S, T> transformer,
            Consumer<List<T>> batchConsumer
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Objects.requireNonNull(transformer, "transformer");
        Objects.requireNonNull(batchConsumer, "batchConsumer");

        List<T> batch = new ArrayList<>(batchSize);
        for (S item : items) {
            T transformed = transformer.apply(item);
            if (transformed != null) {
                batch.add(transformed);
                if (batch.size() >= batchSize) {
                    batchConsumer.accept(List.copyOf(batch));
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            batchConsumer.accept(List.copyOf(batch));
        }
    }
}
