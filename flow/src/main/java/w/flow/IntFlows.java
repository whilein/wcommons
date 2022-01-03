/*
 *    Copyright 2022 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.flow;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.flow.function.FlowConsumer;
import w.flow.function.Int2IntFlowMapper;
import w.flow.function.Int2ObjectFlowMapper;
import w.flow.function.IntFlowCombiner;
import w.flow.function.IntFlowConsumer;
import w.flow.function.IntFlowCountedLoop;
import w.flow.function.IntFlowPredicate;
import w.flow.function.IntFlowSink;
import w.flow.function.IntFlowSupplier;

import java.util.OptionalInt;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@UtilityClass
public class IntFlows {

    public @NotNull IntFlow ofSupplier(
            final @NotNull IntFlowSupplier call
    ) {
        return new IntFlowImpl(null, call);
    }

    public @NotNull IntFlow ofSupplier(
            final @NotNull String name,
            final @NotNull IntFlowSupplier call
    ) {
        return new IntFlowImpl(name, call);
    }

    public @NotNull IntFlow emptyFlow() {
        return new IntFlowImpl(null, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public @NotNull IntFlowItems emptyFlowItems() {
        return new IntFlowItemsImpl(null, sink -> {
        });
    }

    public @NotNull IntFlowItems emptyFlowItems(
            final @NotNull String name
    ) {
        return new IntFlowItemsImpl(name, sink -> {
        });
    }

    public @NotNull IntFlow emptyFlow(
            final @NotNull String name
    ) {
        return new IntFlowImpl(name, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class IntFlowItemsImpl extends AbstractFlowItems
            implements IntFlowItems {

        FlowConsumer<IntFlowSink> sink;

        public IntFlowItemsImpl(
                final String name,
                final FlowConsumer<IntFlowSink> sink
        ) {
            super(name);

            this.sink = sink;
        }

        @Override
        public @NotNull <A, R> Flow<R> collect(final @NonNull IntFlowCollector<A, R> collector) {
            return Flows.ofSupplier(name, () -> {
                final var ref = new Object() {
                    A collection;
                };

                sink.accept(value -> {
                    A collection = ref.collection;

                    if (collection == null)
                        ref.collection = collection = collector.init();

                    collector.accumulate(collection, value);

                    return true;
                });

                return ref.collection == null
                        ? collector.empty()
                        : collector.finish(ref.collection);
            });
        }

        @Override
        public @NotNull IntFlow findFirst() {
            return IntFlows.ofSupplier(name, () -> {
                final var output = new Object() {
                    int value;
                    boolean hasValue;
                };

                sink.accept(value -> {
                    output.value = value;
                    output.hasValue = true;

                    return false;
                });

                if (!output.hasValue) {
                    throw FlowEmpty.INSTANCE;
                }

                return output.value;
            });
        }


        @Override
        public @NotNull IntFlowItems map(final @NonNull Int2IntFlowMapper mapper) {
            return new IntFlowItemsImpl(name, newSink -> sink.accept(value -> newSink.next(mapper.applyAsInt(value))));
        }

        @Override
        public @NotNull <A> FlowItems<A> mapToObj(final @NotNull Int2ObjectFlowMapper<A> mapper) {
            return Flows.ofEmitter(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull IntFlowItems filter(final @NotNull IntFlowPredicate filter) {
            return new IntFlowItemsImpl(name, newSink -> sink.accept(value -> {
                if (!filter.test(value))
                    return true;

                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull IntFlowItems forEach(final @NonNull IntFlowConsumer loop) {
            return new IntFlowItemsImpl(name, newSink -> sink.accept(value -> {
                loop.accept(value);
                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull IntFlowItems forEachCounted(final @NonNull IntFlowCountedLoop loop) {
            return new IntFlowItemsImpl(name, newSink -> {
                final var counter = new Object() {
                    int value;
                };

                sink.accept(value -> {
                    loop.accept(counter.value++, value);
                    return newSink.next(value);
                });
            });
        }

        @Override
        public void run() throws Exception {
            sink.accept(value -> true);
        }
    }

    public @NotNull IntFlowItems ofEmitter(
            final @NotNull String name,
            final @NotNull FlowConsumer<IntFlowSink> sink
    ) {
        return new IntFlowItemsImpl(name, sink);
    }

    public @NotNull IntFlowItems ofEmitter(
            final @NotNull FlowConsumer<IntFlowSink> sink
    ) {
        return new IntFlowItemsImpl(null, sink);
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class IntFlowImpl extends AbstractFlow implements IntFlow {

        IntFlowSupplier call;

        public IntFlowImpl(final String name, final IntFlowSupplier call) {
            super(name);

            this.call = call;
        }

        @Override
        public int run() throws Exception {
            return call.get();
        }

        @Override
        public int call() {
            try {
                return run();
            } catch (final FlowEmpty e) {
                return 0;
            } catch (final Exception e) {
                throw new RuntimeException("Error occurred whilst executing flow " + name, e);
            }
        }

        protected void _callAsync(final Executor executor) {
            executor.execute(this::call);
        }

        @Override
        public @NotNull IntFlow orElse(final int value) {
            return new IntFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value;
                }
            });
        }

        @Override
        public @NotNull IntFlow orElseGet(final @NonNull IntSupplier value) {
            return new IntFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.getAsInt();
                }
            });
        }

        @Override
        public @NotNull IntFlow orElseCall(final @NotNull Supplier<@NotNull IntFlow> value) {
            return new IntFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.get().run();
                }
            });
        }

        @Override
        public void callAsync(
                final @NonNull IntConsumer consumer
        ) {
            _callAsync(consumer, ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(
                final @NonNull IntConsumer consumer,
                final @NonNull Executor executor) {
            _callAsync(consumer, executor);
        }

        private void _callAsync(
                final IntConsumer consumer,
                final Executor executor
        ) {
            executor.execute(() -> {
                int result = 0;

                try {
                    result = run();
                } catch (final FlowEmpty ignored) {
                } catch (final Exception e) {
                    throw new RuntimeException("Error occurred whilst asynchronously executing a flow " + name, e);
                }

                try {
                    consumer.accept(result);
                } catch (final Exception e) {
                    throw new RuntimeException("Error occurred whilst asynchronously executing a flow " + name, e);
                }
            });
        }

        @Override
        public @NotNull <A> Flow<A> mapToObj(
                final @NonNull Int2ObjectFlowMapper<@Nullable A> function
        ) {
            return Flows.ofSupplier(name, () -> function.map(run()));
        }

        @Override
        public @NotNull IntFlow map(
                final @NonNull Int2IntFlowMapper function
        ) {
            return new IntFlowImpl(name, () -> function.applyAsInt(run()));
        }

        @Override
        public @NotNull IntFlow compose(
                final @NonNull Int2ObjectFlowMapper<@NotNull IntFlow> function
        ) {
            return new IntFlowImpl(name, () -> function.map(run()).run());
        }

        @Override
        public @NotNull Flow<@NotNull OptionalInt> toOptional() {
            return Flows.ofSupplier(name, () -> {
                try {
                    return OptionalInt.of(run());
                } catch (final FlowEmpty e) {
                    return OptionalInt.empty();
                }
            });
        }

        @Override
        public @NotNull IntFlow filter(final @NonNull IntFlowPredicate filter) {
            return new IntFlowImpl(name, () -> {
                val value = run();

                if (!filter.test(value)) {
                    throw FlowEmpty.INSTANCE;
                }

                return value;
            });
        }

        @Override
        public @NotNull IntFlow then(
                final @NonNull IntFlow another
        ) {
            return new IntFlowImpl(name, () -> {
                try {
                    run();
                } catch (final FlowEmpty ignored) {
                    // похуй на этот, нам нужен ответ следующего
                }

                return another.run();
            });
        }

        @Override
        public @NotNull IntFlow combine(
                final @NonNull Int2ObjectFlowMapper<@NotNull IntFlow> another,
                final @NonNull IntFlowCombiner combiner
        ) {
            return new IntFlowImpl(name, () -> {
                // может быть вызван только первый флов

                val first = run();
                val second = another.map(first).run();

                return combiner.combine(first, second);
            });
        }

        @Override
        public @NotNull IntFlow combine(
                final @NonNull IntFlow another,
                final @NonNull IntFlowCombiner combiner
        ) {
            return new IntFlowImpl(name, () -> {
                // оба флова должны быть вызваны

                int first = 0;
                int second = 0;

                FlowEmpty empty = null;

                try {
                    first = run();
                } catch (final FlowEmpty e) {
                    empty = e;
                }

                try {
                    second = another.run();
                } catch (final FlowEmpty e) {
                    empty = e;
                }

                if (empty != null) {
                    throw empty;
                }

                return combiner.combine(first, second);
            });
        }

    }

}
