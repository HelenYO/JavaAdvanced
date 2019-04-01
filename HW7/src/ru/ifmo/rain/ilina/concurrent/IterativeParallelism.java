package ru.ifmo.rain.ilina.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private <T> List<Stream<? extends T>> toPart(final int threads, final List<? extends T> list) {
        int threadsNumber = threads == 0 ? 1 : threads > list.size() ? list.size() : threads;
        List<Stream<? extends T>> parts = new ArrayList<>();
        int left = 0;
        int length = list.size() / threadsNumber;
        int t = list.size() % threadsNumber;
        int right = length + (t-- > 0 ? 1 : 0);
        for (int i = 0; i < threadsNumber; i++) {
            parts.add(list.subList(left, right).stream());
            left = right;
            right += length + (t-- > 0 ? 1 : 0);
        }
        return parts;
    }

    private static abstract class ParallelWorker<R> implements Runnable {

        private R result;

        void setResult(R result) {
            this.result = result;
        }

        R getResult() {
            return result;
        }

    }

    private <T, R> R parallelism(int threads, List<? extends T> values,
                                 Function<Stream<? extends T>, R> action,
                                 Function<? super Stream<R>, R> resultAction) throws InterruptedException {
        List<Stream<? extends T>> parts = toPart(threads, values);
        List<Thread> threadList = new ArrayList<>();
        List<ParallelWorker<R>> workers = new ArrayList<>();
        for (Stream<? extends T> part : parts) {
            ParallelWorker<R> worker = new ParallelWorker<>() {
                @Override
                public void run() {
                    setResult(action.apply(part));
                }
            };
            workers.add(worker);
            Thread thread = new Thread(worker);
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        return resultAction.apply(workers.stream().map(ParallelWorker::getResult));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        StringBuilder result = new StringBuilder();
        map(threads, values, Object::toString).forEach(result::append);
        return result.toString();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelism(threads, values, o -> o.filter(predicate).collect(Collectors.toList()),
                o -> o.reduce(new ArrayList<>(), (o1, o2) -> {
                    o1.addAll(o2);
                    return o1;
                }));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelism(threads, values, o -> o.map(f).collect(Collectors.toList()),
                o -> o.reduce(new ArrayList<>(), (o1, o2) -> {
                    o1.addAll(o2);
                    return o1;
                }));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> func = o -> o.max(comparator).orElse(null);
        return parallelism(threads, values, func, func);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelism(threads, values, o -> o.allMatch(predicate), o -> o.allMatch(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}