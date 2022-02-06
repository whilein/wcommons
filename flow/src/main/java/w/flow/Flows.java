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
import w.flow.function.FlowCombiner;
import w.flow.function.FlowConsumer;
import w.flow.function.FlowCountedLoop;
import w.flow.function.FlowMapper;
import w.flow.function.FlowPredicate;
import w.flow.function.FlowSink;
import w.flow.function.FlowSupplier;
import w.flow.function.Object2IntFlowMapper;
import w.flow.function.Object2LongFlowMapper;
import w.util.mutable.Mutables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@UtilityClass
public class Flows {

    public <T> @NotNull Flow<T> ofSupplier(
            final @NotNull FlowSupplier<T> call
    ) {
        return new FlowImpl<>(null, call);
    }

    public <T> @NotNull Flow<T> ofSupplier(
            final @NotNull String name,
            final @NotNull FlowSupplier<T> call
    ) {
        return new FlowImpl<>(name, call);
    }

    public <T> @NotNull Flow<T> emptyFlow(
            final @NotNull String name
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

    public <T> @NotNull FlowItems<T> ofEmitter(
            final @NotNull String name,
            final @NotNull FlowConsumer<FlowSink<T>> sink
    ) {
        return new FlowItemsImpl<>(name, sink);
    }

    public <T> @NotNull FlowItems<T> ofEmitter(
            final @NonNull FlowConsumer<FlowSink<T>> sink
    ) {
        return new FlowItemsImpl<>(null, sink);
    }

    public <T> @NotNull FlowItems<T> emptyFlowItems() {
        return new FlowItemsImpl<>(null, sink -> {
        });
    }

    public <T> @NotNull FlowItems<T> emptyFlowItems(
            final @NotNull String name
    ) {
        return new FlowItemsImpl<>(name, sink -> {
        });
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class FlowImpl<T> extends AbstractFlow implements Flow<T> {

        FlowSupplier<T> call;

        private FlowImpl(final String name, final FlowSupplier<T> call) {
            super(name);

            this.call = call;
        }

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

        @Override
        protected void _callAsync(final Executor executor) {
            executor.execute(this::call);
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
                final @NonNull Object2IntFlowMapper<T> function
        ) {
            return IntFlows.ofSupplier(name, () -> function.map(run()));
        }

        @Override
        public @NotNull LongFlow mapToLong(
                final @NonNull Object2LongFlowMapper<T> function
        ) {
            return LongFlows.ofSupplier(name, () -> function.map(run()));
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
        public @NotNull Flow<T> filter(final @NonNull FlowPredicate<T> filter) {
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
                val ref = Mutables.<A>newReference();

                sink.accept(value -> {
                    A container = ref.get();

                    if (container == null)
                        ref.set(container = collector.init());

                    collector.accumulate(container, value);

                    return true;
                });

                return ref.isNotNull()
                        ? collector.finish(ref.get())
                        : collector.empty();
            });
        }


        @Override
        public @NotNull Flow<T> findFirst() {
            return new FlowImpl<>(name, () -> {
                val ref = Mutables.<T>newOptionalReference();

                sink.accept(value -> {
                    ref.set(value);
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull <A> Flow<A> mapFirst(
                final @NonNull FlowMapper<T, A> function
        ) {
            return new FlowImpl<>(name, () -> {
                val ref = Mutables.<A>newOptionalReference();

                sink.accept(value -> {
                    ref.set(function.map(value));
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
            });
        }

        @Override
        public @NotNull IntFlow mapFirstToInt(
                final @NonNull Object2IntFlowMapper<T> function
        ) {
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
        public @NotNull LongFlow mapFirstToLong(
                final @NonNull Object2LongFlowMapper<T> function
        ) {
            return LongFlows.ofSupplier(name, () -> {
                val ref = Mutables.newOptionalLong();

                sink.accept(value -> {
                    ref.set(function.map(value));
                    return false;
                });

                return ref.orElseThrow(FlowEmpty.INSTANCE);
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
        public @NotNull FlowItems<T> filter(final @NotNull FlowPredicate<T> filter) {
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
        public @NotNull IntFlowItems mapToInt(final @NonNull Object2IntFlowMapper<T> mapper) {
            return IntFlows.ofEmitter(name, newSink -> sink.accept(
                    value -> newSink.next(mapper.map(value))));
        }

        @Override
        public @NotNull LongFlowItems mapToLong(final @NonNull Object2LongFlowMapper<T> mapper) {
            return LongFlows.ofEmitter(name, newSink -> sink.accept(
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
