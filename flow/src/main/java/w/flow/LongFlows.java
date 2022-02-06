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
import w.flow.function.Long2IntFlowMapper;
import w.flow.function.Long2LongFlowMapper;
import w.flow.function.Long2ObjectFlowMapper;
import w.flow.function.LongFlowCombiner;
import w.flow.function.LongFlowConsumer;
import w.flow.function.LongFlowCountedLoop;
import w.flow.function.LongFlowPredicate;
import w.flow.function.LongFlowSink;
import w.flow.function.LongFlowSupplier;
import w.util.mutable.Mutables;

import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@UtilityClass
public class LongFlows {

    public @NotNull LongFlow ofSupplier(
            final @NotNull LongFlowSupplier call
    ) {
        return new LongFlowImpl(null, call);
    }

    public @NotNull LongFlow ofSupplier(
            final @NotNull String name,
            final @NotNull LongFlowSupplier call
    ) {
        return new LongFlowImpl(name, call);
    }

    public @NotNull LongFlow emptyFlow() {
        return new LongFlowImpl(null, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public @NotNull LongFlowItems emptyFlowItems() {
        return new LongFlowItemsImpl(null, sink -> {
        });
    }

    public @NotNull LongFlowItems emptyFlowItems(
            final @NotNull String name
    ) {
        return new LongFlowItemsImpl(name, sink -> {
        });
    }

    public @NotNull LongFlow emptyFlow(
            final @NotNull String name
    ) {
        return new LongFlowImpl(name, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class LongFlowItemsImpl extends AbstractFlowItems
            implements LongFlowItems {

        FlowConsumer<LongFlowSink> sink;

        public LongFlowItemsImpl(
                final String name,
                final FlowConsumer<LongFlowSink> sink
        ) {
            super(name);

            this.sink = sink;
        }

        @Override
        public @NotNull <A, R> Flow<R> collect(final @NonNull LongFlowCollector<A, R> collector) {
            return Flows.ofSupplier(name, () -> {
                val ref = Mutables.<A>newReference();

                sink.accept(value -> {
                    A collection = ref.get();

                    if (collection == null)
                        ref.set(collection = collector.init());

                    collector.accumulate(collection, value);

                    return true;
                });

                return ref.isNotNull()
                        ? collector.finish(ref.get())
                        : collector.empty();
            });
        }

        @Override
        public @NotNull LongFlow findFirst() {
            return new LongFlowImpl(name, () -> {
                val ref = Mutables.newOptionalLong();

                sink.accept(value -> {
                    ref.set(value);
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull <A> Flow<A> mapFirstToObj(
                final @NonNull Long2ObjectFlowMapper<A> function
        ) {
            return Flows.ofSupplier(name, () -> {
                val ref = Mutables.<A>newOptionalReference();

                sink.accept(value -> {
                    ref.set(function.map(value));
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull IntFlow mapFirstToInt(final @NotNull Long2IntFlowMapper function) {
            return IntFlows.ofSupplier(name, () -> {
                val ref = Mutables.newOptionalInt();

                sink.accept(value -> {
                    ref.set(function.map(value));
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull LongFlow mapFirst(
                final @NonNull Long2LongFlowMapper function
        ) {
            return new LongFlowImpl(name, () -> {
                val ref = Mutables.newOptionalLong();

                sink.accept(value -> {
                    ref.set(function.map(value));
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull LongFlowItems map(final @NonNull Long2LongFlowMapper mapper) {
            return new LongFlowItemsImpl(name, newSink -> sink.accept(value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull IntFlowItems mapToInt(final @NotNull Long2IntFlowMapper mapper) {
            return IntFlows.ofEmitter(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull <A> FlowItems<A> mapToObj(final @NotNull Long2ObjectFlowMapper<A> mapper) {
            return Flows.ofEmitter(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull LongFlowItems filter(final @NotNull LongFlowPredicate filter) {
            return new LongFlowItemsImpl(name, newSink -> sink.accept(value -> {
                if (!filter.test(value))
                    return true;

                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull LongFlowItems forEach(final @NonNull LongFlowConsumer loop) {
            return new LongFlowItemsImpl(name, newSink -> sink.accept(value -> {
                loop.accept(value);
                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull LongFlowItems forEachCounted(final @NonNull LongFlowCountedLoop loop) {
            return new LongFlowItemsImpl(name, newSink -> {
                val counter = Mutables.newInt();

                sink.accept(value -> {
                    loop.accept(counter.getAndIncrement(), value);

                    return newSink.next(value);
                });
            });
        }

        @Override
        public void run() throws Exception {
            sink.accept(value -> true);
        }
    }

    public @NotNull LongFlowItems ofEmitter(
            final @NotNull String name,
            final @NotNull FlowConsumer<LongFlowSink> sink
    ) {
        return new LongFlowItemsImpl(name, sink);
    }

    public @NotNull LongFlowItems ofEmitter(
            final @NotNull FlowConsumer<LongFlowSink> sink
    ) {
        return new LongFlowItemsImpl(null, sink);
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class LongFlowImpl extends AbstractFlow implements LongFlow {

        LongFlowSupplier call;

        public LongFlowImpl(final String name, final LongFlowSupplier call) {
            super(name);

            this.call = call;
        }

        @Override
        public long run() throws Exception {
            return call.get();
        }

        @Override
        public long call() {
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
        public @NotNull LongFlow orElse(final long value) {
            return new LongFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value;
                }
            });
        }

        @Override
        public @NotNull LongFlow orElseGet(final @NonNull LongSupplier value) {
            return new LongFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.getAsLong();
                }
            });
        }

        @Override
        public @NotNull LongFlow orElseCall(final @NotNull Supplier<@NotNull LongFlow> value) {
            return new LongFlowImpl(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.get().run();
                }
            });
        }

        @Override
        public void callAsync(
                final @NonNull LongConsumer consumer
        ) {
            _callAsync(consumer, ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(
                final @NonNull LongConsumer consumer,
                final @NonNull Executor executor) {
            _callAsync(consumer, executor);
        }

        private void _callAsync(
                final LongConsumer consumer,
                final Executor executor
        ) {
            executor.execute(() -> {
                long result = 0;

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
                final @NonNull Long2ObjectFlowMapper<@Nullable A> function
        ) {
            return Flows.ofSupplier(name, () -> function.map(run()));
        }

        @Override
        public @NotNull LongFlow map(
                final @NonNull Long2LongFlowMapper function
        ) {
            return new LongFlowImpl(name, () -> function.map(run()));
        }

        @Override
        public @NotNull IntFlow mapToInt(
                final @NonNull Long2IntFlowMapper function
        ) {
            return IntFlows.ofSupplier(name, () -> function.map(run()));
        }

        @Override
        public @NotNull LongFlow compose(
                final @NonNull Long2ObjectFlowMapper<@NotNull LongFlow> function
        ) {
            return new LongFlowImpl(name, () -> function.map(run()).run());
        }

        @Override
        public @NotNull Flow<@NotNull OptionalLong> toOptional() {
            return Flows.ofSupplier(name, () -> {
                try {
                    return OptionalLong.of(run());
                } catch (final FlowEmpty e) {
                    return OptionalLong.empty();
                }
            });
        }

        @Override
        public @NotNull LongFlow filter(final @NonNull LongFlowPredicate filter) {
            return new LongFlowImpl(name, () -> {
                val value = run();

                if (!filter.test(value)) {
                    throw FlowEmpty.INSTANCE;
                }

                return value;
            });
        }

        @Override
        public @NotNull LongFlow then(
                final @NonNull LongFlow another
        ) {
            return new LongFlowImpl(name, () -> {
                try {
                    run();
                } catch (final FlowEmpty ignored) {
                    // похуй на этот, нам нужен ответ следующего
                }

                return another.run();
            });
        }

        @Override
        public @NotNull LongFlow combine(
                final @NonNull Long2ObjectFlowMapper<@NotNull LongFlow> another,
                final @NonNull LongFlowCombiner combiner
        ) {
            return new LongFlowImpl(name, () -> {
                // может быть вызван только первый флов

                val first = run();
                val second = another.map(first).run();

                return combiner.combine(first, second);
            });
        }

        @Override
        public @NotNull LongFlow combine(
                final @NonNull LongFlow another,
                final @NonNull LongFlowCombiner combiner
        ) {
            return new LongFlowImpl(name, () -> {
                // оба флова должны быть вызваны

                long first = 0;
                long second = 0;

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
