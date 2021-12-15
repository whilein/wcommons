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

package w.flow;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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

    @Getter
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    private static abstract class AbstractFlow implements BaseFlow {
        String name;

        protected abstract void _callAsync(final Executor executor);

        @Override
        public final void callAsync() {
            _callAsync(ForkJoinPool.commonPool());
        }

        @Override
        public final void callAsyncUsing(final @NonNull Executor executor) {
            _callAsync(executor);
        }

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

        @Getter
        String name;

        FlowSupplier<T> call;

        @Override
        public @NotNull CompletableFuture<T> toFuture() {
            return CompletableFuture.supplyAsync(this::call);
        }

        @Override
        public @NotNull CompletableFuture<T> toFuture(final @NotNull Executor executor) {
            return CompletableFuture.supplyAsync(this::call, executor);
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
                throw new RuntimeException("Error occurred whilst executing flow a " + name, e);
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
        public void callAsyncUsing(final @NonNull Executor executor) {
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
        public @NotNull Flow<T> orElseCall(final @NotNull Supplier<@NotNull Flow<T>> value) {
            return new FlowImpl<>(name, () -> {
                try {
                    return run();
                } catch (final FlowEmpty e) {
                    return value.get().run();
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
                final @NonNull FlowConsumer<T> consumer
        ) {
            _callAsync(consumer, ForkJoinPool.commonPool());
        }

        @Override
        public void callAsync(
                final @NonNull FlowConsumer<T> consumer,
                final @NonNull Executor executor) {
            _callAsync(consumer, executor);
        }

        private void _callAsync(
                final @NonNull FlowConsumer<T> consumer,
                final @NonNull Executor executor
        ) {
            executor.execute(() -> {
                T result = null;

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

        private <A, R> Flow<R> _parallel(
                final Flow<A> another,
                final FlowCombiner<T, A, R> combiner,
                final Executor executor
        ) {
            return new FlowImpl<>(name, () -> {
                val x = toFuture(executor);
                val y = another.toFuture(executor);

                return combiner.combine(x.get(), y.get());
            });
        }

        @Override
        public @NotNull <A, R> Flow<R> parallel(
                final @NonNull Flow<A> another,
                final @NonNull FlowCombiner<T, A, R> combiner
        ) {
            return _parallel(another, combiner, ForkJoinPool.commonPool());
        }

        @Override
        public @NotNull <A, R> Flow<R> parallel(
                final @NonNull Flow<A> another,
                final @NonNull FlowCombiner<T, A, R> combiner,
                final @NonNull Executor executor
        ) {
            return _parallel(another, combiner, executor);
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

    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    private static abstract class AbstractFlowItems extends AbstractFlow implements BaseFlowItems {

        public AbstractFlowItems(final String name) {
            super(name);
        }

        @Override
        public abstract void run() throws Exception;

        protected final void _callAsync(final Executor executor) {
            executor.execute(this::call);
        }

        @Override
        public void call() {
            try {
                run();
            } catch (final Exception e) {
                throw new RuntimeException("Error occurred whilst executing flow " + name, e);
            }
        }

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
            return new FlowImpl<>(name, () -> {
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
            return new IntFlowItemsImpl(name, newSink -> sink.accept(value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull <A> FlowItems<A> mapToObj(final @NotNull IntFlowMapper<A> mapper) {
            return new FlowItemsImpl<>(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull IntFlowItems filter(final @NotNull IntFlowFilter filter) {
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

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class FlowItemsImpl<T> extends AbstractFlowItems
            implements FlowItems<T> {

        FlowConsumer<FlowSink<T>> sink;

        public FlowItemsImpl(
                final String name,
                final FlowConsumer<FlowSink<T>> sink
        ) {
            super(name);

            this.sink = sink;
        }


        @Override
        public @NotNull <A, R> Flow<R> collect(final @NonNull FlowCollector<? super T, A, R> collector) {
            return new FlowImpl<>(name, () -> {
                final var ref = new Object() {
                    A container;
                };

                sink.accept(value -> {
                    A container = ref.container;

                    if (container == null)
                        ref.container = container = collector.init();

                    collector.accumulate(container, value);

                    return true;
                });

                return ref.container == null
                        ? collector.empty()
                        : collector.finish(ref.container);
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
        public @NotNull <A> FlowItems<A> map(final @NonNull FlowMapper<T, A> mapper) {
            return new FlowItemsImpl<>(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @SuppressWarnings("unchecked")
        private <A> FlowItems<A> _flatMapParallel(final FlowMapper<T, FlowItems<A>> fn,
                                                  final Executor executor) {
            return new FlowItemsImpl<>(name, newSink -> {
                // чтобы сохранить порядок, нам нужно знать, в каком порядке это было изначально
                val tasks = new ArrayList<FlowItems<A>>();

                sink.accept(value -> tasks.add(fn.map(value)));

                if (tasks.isEmpty()) {
                    return;
                }

                val taskCount = tasks.size();

                val completedArray = new Object[taskCount][];

                val latch = new CountDownLatch(taskCount);

                for (int i = 0; i < taskCount; i++) {
                    final int idx = i;

                    tasks.get(idx).collect(FlowCollectors.toArray(Object[]::new))
                            .callAsync(result -> {
                                completedArray[idx] = result;
                                latch.countDown();
                            }, executor);
                }

                // ждём, пока у нас выполнится всё
                latch.await();

                // отдаём по очереди дальше
                for (val array : completedArray) {
                    for (val value : array) {
                        newSink.next((A) value);
                    }
                }
            });
        }

        @Override
        public @NotNull <A> FlowItems<A> flatMap(final @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn) {
            return new FlowItemsImpl<>(name, newSink -> {
                val subject = new LinkedList<FlowItems<A>>();

                sink.accept(value -> {
                    subject.add(fn.map(value));
                    return true;
                });

                for (val item : subject) {
                    item.forEach(newSink::next).run();
                }
            });
        }

        @Override
        public @NotNull <A> FlowItems<A> flatMapParallel(final @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn) {
            return _flatMapParallel(fn, ForkJoinPool.commonPool());
        }

        @Override
        public @NotNull <A> FlowItems<A> flatMapParallel(
                final @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn,
                final @NotNull Executor executor
        ) {
            return _flatMapParallel(fn, ForkJoinPool.commonPool());
        }

        @Override
        public @NotNull <A> FlowItems<A> compose(final @NotNull FlowMapper<T, @NotNull Flow<A>> fn) {
            return new FlowItemsImpl<>(name, newSink -> sink.accept(
                    value -> newSink.next(fn.map(value).run())));
        }

        @SuppressWarnings("unchecked")
        private <A> FlowItems<A> _composeParallel(final FlowMapper<T, Flow<A>> fn,
                                                  final Executor executor) {
            return new FlowItemsImpl<>(name, newSink -> {
                // чтобы сохранить порядок, нам нужно знать, в каком порядке это было изначально
                val tasks = new ArrayList<Flow<A>>();

                sink.accept(value -> tasks.add(fn.map(value)));

                if (tasks.isEmpty()) {
                    return;
                }

                val completedArray = new Object[tasks.size()];
                val latch = new CountDownLatch(tasks.size());

                for (int i = 0; i < tasks.size(); i++) {
                    final int idx = i;

                    tasks.get(idx).callAsync(result -> {
                        completedArray[idx] = result;
                        latch.countDown();
                    }, executor);
                }

                // ждём, пока у нас выполнится всё
                latch.await();

                // отдаём по очереди дальше
                for (val value : completedArray) {
                    newSink.next((A) value);
                }
            });
        }

        @Override
        public @NotNull <A> FlowItems<A> composeParallel(final @NotNull FlowMapper<T, @NotNull Flow<A>> fn) {
            return _composeParallel(fn, ForkJoinPool.commonPool());
        }

        @Override
        public @NotNull <A> FlowItems<A> composeParallel(
                final @NotNull FlowMapper<T, @NotNull Flow<A>> fn,
                final @NotNull Executor executor
        ) {
            return _composeParallel(fn, executor);
        }

        @Override
        public @NotNull FlowItems<T> filter(final @NotNull FlowFilter<T> filter) {
            return new FlowItemsImpl<>(name, newSink -> sink.accept(value -> {
                if (!filter.test(value))
                    return true;

                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull FlowItems<T> forEach(final @NonNull FlowConsumer<T> loop) {
            return new FlowItemsImpl<>(name, newSink -> sink.accept(value -> {
                loop.accept(value);
                return newSink.next(value);
            }));
        }

        @Override
        public @NotNull IntFlowItems mapToInt(final @NonNull ToIntFlowMapper<T> mapper) {
            return new IntFlowItemsImpl(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull FlowItems<T> forEachCounted(final @NonNull FlowCountedLoop<T> loop) {
            return new FlowItemsImpl<>(name, newSink -> {
                final var t = new Object() {
                    int counter;
                };

                sink.accept(value -> {
                    loop.accept(t.counter++, value);
                    return newSink.next(value);
                });
            });
        }

        @Override
        public void run() throws Exception {
            sink.accept(value -> true);
        }
    }

}
