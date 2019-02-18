package ru.ifmo.rain.ilina.arrayset;

import java.util.*;


public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> container;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        comparator = null;
        container = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> other) {
        comparator = null;
        container = new ArrayList<>(new TreeSet<>(other));
    }

    public ArraySet(Comparator<? super T> cmp) {
        container = Collections.emptyList();
        comparator = cmp;
    }

    public ArraySet(Collection<? extends T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        TreeSet<T> tmp = new TreeSet<>(cmp);
        tmp.addAll(other);
        container = new ArrayList<>(tmp);
    }

    private ArraySet(List<T> arr, Comparator<? super T> cmp) {
        comparator = cmp;
        container = arr;
        if (arr instanceof ReversedList) {
            ((ReversedList) arr).reverse();
        }
    }

    @Override
    public int size() {
        return container.size();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(container).iterator();
    }

    @Override
    public boolean contains(Object o) {
        return (Collections.binarySearch(container, (T) Objects.requireNonNull(o), comparator) >= 0);
    }

    private int check(int x) {
        if( 0 <= x && x < this.size()) {
            return x;
        } else {
            return -1;
        }
    }

    private int indexGetter(T t, int found, int notFound) {
        int res = Collections.binarySearch(container, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
            int temp = res + notFound;
            return check(temp);
        }
        return check(res + found);
    }

    private int lowerInd(T t) {
        return indexGetter(t, -1, -1);
    }

    private int higherInd(T t) {
        return indexGetter(t, 1, 0);
    }

    private int floorInd(T t) {
        return indexGetter(t, 0, -1);
    }

    private int ceilingInd(T t) {
        return indexGetter(t, 0, 0);
    }

    @Override
    public T first() {
        if (container.isEmpty()) {
            throw new NoSuchElementException();
        }
        return container.get(0);
    }

    @Override
    public T last() {
        if (container.isEmpty()) {
            throw new NoSuchElementException();
        }
        return container.get(size() - 1);
    }

    private T getElement(int ind) {
        return (ind < 0) ? null : container.get(ind);
    }

    @Override
    public T lower(T t) {
        return getElement(lowerInd(t));
    }

    @Override
    public T higher(T t) {
        return getElement(higherInd(t));
    }

    @Override
    public T floor(T t) {
        return getElement(floorInd(t));
    }

    @Override
    public T ceiling(T t) {
        return getElement(ceilingInd(t));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(container), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int l = fromInclusive ? ceilingInd(fromElement) : higherInd(fromElement);
        int r = toInclusive ? floorInd(toElement) : lowerInd(toElement);
        if (l == -1 || r == -1 || l > r) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        } else {
            return new ArraySet<>(container.subList(l, r + 1), comparator);
        }
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (container.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (container.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }
}


class ReversedList<E> extends AbstractList<E> {
    private boolean reversed;
    private List<E> container;

    public int size() {
        return container.size();
    }

    public void reverse() {
        reversed = !reversed;
    }

    public ReversedList(List<E> other) {
        container = other;
    }

    @Override
    public E get(int index) {
        return reversed ? container.get(size() - 1 - index) : container.get(index);
    }
}