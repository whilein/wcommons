/*
 *    Copyright 2021 Whilein
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

package w.commons.flow;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author whilein
 */
@UtilityClass
public final class Flows {

    public <T> @NotNull Flow<T> ofSupplier(
            final @NonNull FlowSupplier<T> call
    ) {
        return new FlowImpl<>(null, call);
    }

    public <T> @NotNull Flow<T> ofSupplier(
            final @NonNull String name,
            final @NonNull FlowSupplier<T> call
    ) {
        return new FlowImpl<>(name, call);
    }

    public @NotNull IntFlow ofIntSupplier(
            final @NonNull IntFlowSupplier call
    ) {
        return new IntFlowImpl(null, call);
    }

    public @NotNull IntFlow ofIntSupplier(
            final @NonNull String name,
            final @NonNull IntFlowSupplier call
    ) {
        return new IntFlowImpl(name, call);
    }

    public @NotNull IntFlow emptyIntFlow() {
        return new IntFlowImpl(null, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public @NotNull IntFlow emptyIntFlow(
            final @NonNull String name
    ) {
        return new IntFlowImpl(name, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public <T> @NotNull Flow<T> emptyFlow(
            final @NonNull String name
    ) {
        return new FlowImpl<>(name, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public <T> @NotNull Flow<T> emptyFlow() {
        return new FlowImpl<>(null, () -> {
            throw FlowEmpty.INSTANCE;
        });
    }

    public @NotNull IntFlowItems ofIntEmitter(
            final @NonNull String name,
            final @NonNull FlowConsumer<IntFlowSink> sink
    ) {
        return new IntFlowItemsImpl(name, sink);
    }

    public @NotNull IntFlowItems ofIntEmitter(
            final @NonNull FlowConsumer<IntFlowSink> sink
    ) {
        return new IntFlowItemsImpl(null, sink);
    }

    public <T> @NotNull FlowItems<T> ofEmitter(
            final @NonNull String name,
            final @NonNull FlowConsumer<FlowSink<T>> sink
    ) {
        return new FlowItemsImpl<>(name, sink);
    }

    public <T> @NotNull FlowItems<T> ofEmitter(
            final @NonNull FlowConsumer<FlowSink<T>> sink
    ) {
        return new FlowItemsImpl<>(null, sink);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class IntFlowImpl implements IntFlow {

        String name;
        IntFlowSupplier call;

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

        private void _callAsync(final Executor executor) {
            executor.execute(this::call);
        }
        
        @Override
        public void callAsync() {
            _callAsync(ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(final @NonNull Executor executor) {
            _callAsync(executor);
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
                final @NonNull IntConsumer consumer,
                final @NonNull Executor executor
        ) {
            executor.execute(() -> consumer.accept(call()));
        }

        @Override
        public @NotNull <A> Flow<A> mapToObj(
                final @NonNull IntFlowMapper<@Nullable A> function
        ) {
            return new FlowImpl<>(name, () -> function.map(run()));
        }

        @Override
        public @NotNull IntFlow map(
                final @NonNull IntToIntFlowMapper function
        ) {
            return new IntFlowImpl(name, () -> function.map(run()));
        }

        @Override
        public @NotNull IntFlow compose(
                final @NonNull IntFlowMapper<@NotNull IntFlow> function
        ) {
            return new IntFlowImpl(name, () -> function.map(run()).run());
        }

        @Override
        public @NotNull Flow<@NotNull OptionalInt> toOptional() {
            return new FlowImpl<>(name, () -> {
                try {
                    return OptionalInt.of(run());
                } catch (final FlowEmpty e) {
                    return OptionalInt.empty();
                }
            });
        }

        @Override
        public @NotNull IntFlow filter(final @NonNull IntFlowFilter filter) {
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
                final @NonNull IntFlowMapper<@NotNull IntFlow> another,
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

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FlowImpl<T> implements Flow<T> {

        String name;
        FlowSupplier<T> call;

        @Override
        public CompletableFuture<T> toFuture() {
            return CompletableFuture.supplyAsync(this::call);
        }

        @Override
        public T run() throws Exception {
            return call.get();
        }

        @Override
        public T call() {
            try {
                return run();
            } catch (final FlowEmpty e) {
                return null;
            } catch (final Exception e) {
                throw new RuntimeException("Error occurred whilst executing flow " + name, e);
            }
        }

        private void _callAsync(final Executor executor) {
            executor.execute(this::call);
        }

        @Override
        public void callAsync() {
            _callAsync(ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(final @NonNull Executor executor) {
            _callAsync(executor);
        }

        @Override
        public @NotNull Flow<T> orElse(final @NonNull T value) {
            return new FlowImpl<>(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value;
                }
            });
        }

        @Override
        public @NotNull Flow<T> orElseGet(final @NonNull Supplier<T> value) {
            return new FlowImpl<>(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.get();
                }
            });
        }

        @Override
        public void callAsync(
                final @NonNull Consumer<T> consumer
        ) {
            _callAsync(consumer, ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(
                final @NonNull Consumer<T> consumer,
                final @NonNull Executor executor) {
            _callAsync(consumer, executor);
        }

        private void _callAsync(
                final @NonNull Consumer<T> consumer,
                final @NonNull Executor executor
        ) {
            executor.execute(() -> consumer.accept(call()));
        }

        @Override
        public @NotNull <A> Flow<A> map(
                final @NonNull FlowMapper<T, A> function
        ) {
            return new FlowImpl<>(name, () -> function.map(run()));
        }

        @Override
        public @NotNull IntFlow mapToInt(
                final @NonNull ToIntFlowMapper<T> function
        ) {
            return new IntFlowImpl(name, () -> function.map(run()));
        }

        @Override
        public <A> @NotNull Flow<A> compose(
                final @NonNull FlowMapper<T, @NotNull Flow<A>> function
        ) {
            return new FlowImpl<>(name, () -> function.map(run()).run());
        }

        @Override
        public @NotNull Flow<@NotNull Optional<T>> toOptional() {
            return new FlowImpl<>(name, () -> {
                try {
                    return Optional.of(run());
                } catch (final FlowEmpty e) {
                    return Optional.empty();
                }
            });
        }

        @Override
        public @NotNull Flow<T> filter(final @NonNull FlowFilter<T> filter) {
            return new FlowImpl<>(name, () -> {
                val result = run();

                if (!filter.test(result)) {
                    throw FlowEmpty.INSTANCE;
                }

                return result;
            });
        }

        @Override
        public @NotNull <A> Flow<A> then(final @NonNull Flow<@NotNull A> another) {
            return new FlowImpl<>(name, () -> {
                try {
                    run();
                } catch (final FlowEmpty ignored) {
                    // похуй на этот, нам нужен ответ следующего
                }

                return another.run();
            });
        }

        @Override
        public @NotNull <A, R> Flow<A> combine(
                final @NonNull FlowMapper<T, Flow<R>> another,
                final @NonNull FlowCombiner<T, R, A> combiner
        ) {
            return new FlowImpl<>(name, () -> {
                // может быть вызван только первый флов

                val first = run();
                val second = another.map(first).run();

                return combiner.combine(first, second);
            });
        }

        @Override
        public @NotNull <A, R> Flow<A> combine(
                final @NonNull Flow<R> another,
                final @NonNull FlowCombiner<T, R, A> combiner
        ) {
            return new FlowImpl<>(name, () -> {
                // оба флова должны быть вызваны

                T first = null;
                R second = null;

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

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class IntFlowItemsImpl
            implements IntFlowItems {

        String name;
        FlowConsumer<IntFlowSink> sink;

        @Override
        public @NotNull <C> Flow<C> collect(final @NonNull IntFlowCollector<C> collector) {
            return new FlowImpl<>(name, () -> {
                val collection = collector.init();

                sink.accept(value -> {
                    collector.accumulate(collection, value);
                    return true;
                });

                return collection;
            });
        }

        @Override
        public @NotNull IntFlow findFirst() {
            return new IntFlowImpl(name, () -> {
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
        public @NotNull IntFlowItems map(final @NonNull IntToIntFlowMapper mapper) {
            return new IntFlowItemsImpl(name, newEmitter -> sink.accept(value -> newEmitter.next(mapper.map(value))));
        }

        @Override
        public @NotNull <A> FlowItems<A> mapToObj(final @NotNull IntFlowMapper<A> mapper) {
            return new FlowItemsImpl<>(name, newEmitter -> sink.accept(
                    value -> newEmitter.next(mapper.map(value))));
        }

        @Override
        public @NotNull IntFlowItems filter(final @NotNull IntFlowFilter filter) {
            return new IntFlowItemsImpl(name, newEmitter -> sink.accept(value -> {
                if (!filter.test(value))
                    return true;

                return newEmitter.next(value);
            }));
        }


        @Override
        public @NotNull IntFlowItems forEach(final @NonNull IntFlowConsumer loop) {
            return new IntFlowItemsImpl(name, newEmitter -> sink.accept(value -> {
                loop.accept(value);
                return newEmitter.next(value);
            }));
        }

        @Override
        public @NotNull IntFlowItems forEachCounted(final @NonNull IntFlowCountedLoop loop) {
            return new IntFlowItemsImpl(name, newEmitter -> {
                final var counter = new Object() {
                    int value;
                };

                sink.accept(value -> {
                    loop.accept(counter.value++, value);
                    return newEmitter.next(value);
                });
            });
        }

        @Override
        public void call() {
            try {
                sink.accept(value -> false);
            } catch (final Exception e) {
                throw new RuntimeException("Error occurred whilst executing flow " + name, e);
            }
        }

        @Override
        public void callAsync() {
            ForkJoinPool.commonPool().execute(this::call);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FlowItemsImpl<T>
            implements FlowItems<T> {

        String name;
        FlowConsumer<FlowSink<T>> sink;

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull <R, A> Flow<R> collect(final @NotNull Collector<? super T, A, R> collector) {
            return new FlowImpl<>(name, () -> {
                val accumulator = collector.accumulator();
                val result = collector.supplier().get();

                sink.accept(value -> {
                    accumulator.accept(result, value);

                    return true;
                });

                return collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)
                        ? (R) result
                        : collector.finisher().apply(result);
            });
        }

        @Override
        public @NotNull Flow<T> findFirst() {
            return new FlowImpl<>(name, () -> {
                final var output = new Object() {
                    T value;
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
        public @NotNull <A> Flow<A> mapFirst(
                final @NonNull FlowMapper<T, A> function
        ) {
            return new FlowImpl<>(name, () -> {
                final var output = new Object() {
                    A value;
                    boolean hasValue;
                };

                sink.accept(value -> {
                    output.value = function.map(value);
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
        public @NotNull IntFlow mapFirstToInt(
                final @NonNull ToIntFlowMapper<T> function
        ) {
            return new IntFlowImpl(name, () -> {
                final var output = new Object() {
                    int value;
                    boolean hasValue;
                };

                sink.accept(value -> {
                    output.value = function.map(value);
                    return false;
                });

                if (!output.hasValue) {
                    throw FlowEmpty.INSTANCE;
                }

                return output.value;
            });
        }

        @Override
        public @NotNull <A> FlowItems<A> map(final @NonNull FlowMapper<T, A> mapper) {
            return new FlowItemsImpl<>(name, newEmitter -> sink.accept(
                    value -> newEmitter.next(mapper.map(value))));
        }

        @Override
        public @NotNull FlowItems<T> filter(final @NotNull FlowFilter<T> filter) {
            return new FlowItemsImpl<>(name, newEmitter -> sink.accept(value -> {
                if (!filter.test(value))
                    return true;

                return newEmitter.next(value);
            }));
        }

        @Override
        public @NotNull FlowItems<T> forEach(final @NonNull FlowConsumer<T> loop) {
            return new FlowItemsImpl<>(name, newEmitter -> sink.accept(value -> {
                loop.accept(value);
                return newEmitter.next(value);
            }));
        }

        @Override
        public @NotNull IntFlowItems mapToInt(final @NonNull ToIntFlowMapper<T> mapper) {
            return new IntFlowItemsImpl(name, newEmitter -> sink.accept(
                    value -> newEmitter.next(mapper.map(value))));
        }

        @Override
        public @NotNull FlowItems<T> forEachCounted(final @NonNull FlowCountedLoop<T> loop) {
            return new FlowItemsImpl<>(name, newEmitter -> {
                val counter = new AtomicInteger();

                sink.accept(value -> {
                    loop.accept(counter.getAndIncrement(), value);
                    return newEmitter.next(value);
                });
            });
        }

        @Override
        public void call() {
            try {
                sink.accept(value -> false);
            } catch (final Exception e) {
                throw new RuntimeException("Error occurred whilst executing flow " + name, e);
            }
        }

        @Override
        public void callAsync() {
            ForkJoinPool.commonPool().execute(this::call);
        }

    }

}
