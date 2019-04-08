package ru.ifmo.rain.ilina.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    private static <T> List<Stream<? extends T>> split(final int threads, final List<? extends T> list) {
        final int n = Math.min(threads, list.size());
        final int len = list.size() / n;
        int g = list.size() % n;
        List<Stream<? extends T>> subStreams = new ArrayList<>();
        for (int cur = 0; cur < list.size(); g--) {
            final int prev = cur;
            cur += len + (g > 0 ? 1 : 0);
            subStreams.add(list.subList(prev, cur).stream());
        }
        return subStreams;
    }

    private static void joinThreads(final List<Thread> threads) throws InterruptedException {
        InterruptedException exception = null;
        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                if (exception == null) {
                    exception = new InterruptedException("Threads were not joined");
                }
                exception.addSuppressed(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    private <T, R> R baseSupply(int threads, final List<? extends T> list,
                                final Function<Stream<? extends T>, ? extends R> function,
                                final Function<? super Stream<R>, R> resultCollector) throws InterruptedException {
        final List<R> result;
        final List<Stream<? extends T>> subStreams = split(threads, list);
        if (mapper != null) {
            result = mapper.map(function, subStreams);
        } else {

            result = new ArrayList<>(Collections.nCopies(subStreams.size(), null));
            final List<Thread> myThreads = IntStream.range(0, subStreams.size())
                    .mapToObj(threadPosition ->
                            new Thread(() -> result.set(threadPosition, function.apply(subStreams.get(threadPosition)))))
                    .collect(Collectors.toList());
            myThreads.forEach(Thread::start);
            joinThreads(myThreads);
        }

        return resultCollector.apply(result.stream());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> function = s -> s.max(comparator).orElse(null);
        return baseSupply(threads, list, function, function);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, Boolean> function = s -> s.allMatch(predicate);
        Function<Stream<Boolean>, Boolean> merger = s -> s.allMatch(Boolean::booleanValue);
        return baseSupply(threads, list, function, merger);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, list, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        Function<Stream<?>, String> function = s -> s.map(Object::toString).collect(Collectors.joining());
        Function<Stream<String>, String> merger = s -> s.collect(Collectors.joining());
        return baseSupply(threads, list, function, merger);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, List<T>> function = s -> s.filter(predicate).collect(Collectors.toList());
        Function<Stream<List<T>>, List<T>> merger = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return baseSupply(threads, list, function, merger);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> mapper) throws InterruptedException {
        Function<Stream<? extends T>, List<U>> function = s -> s.map(mapper).collect(Collectors.toList());
        Function<Stream<List<U>>, List<U>> merger = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return baseSupply(threads, list, function, merger);
    }
}