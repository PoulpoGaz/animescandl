package fr.poulpogaz.animescandl.utils;

import java.util.*;

public class CircularQueue<E> extends AbstractCollection<E> implements Queue<E> {

    private final E[] elements;
    private int start = 0;
    private int end = 0;
    private boolean full = false;

    private final int maxElements;

    private int modCount;

    public CircularQueue(int maxElements) {
        if (maxElements <= 0) {
            throw new IllegalArgumentException("The maximal size must be greater than 0");
        }

        this.maxElements = maxElements;
        this.elements = (E[]) new Object[maxElements];
    }

    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(index);
        }

        return elements[(index + start) % maxElements];
    }

    @Override
    public int size() {
        if (end < start) {
            return maxElements - start + end;
        } else if (end == start) {
            return full ? maxElements : 0;
        } else {
            return end - start;
        }
    }

    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e);

        if (isFull()) {
            remove();
        }

        elements[end] = e;
        end++;

        if (end >= maxElements) {
            end = 0;
        }

        if (end == start) {
            full = true;
        }
        modCount++;

        return true;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }

        E element = elements[start];
        if (element != null) {
            elements[start] = null;
            start++;

            if (start >= maxElements) {
                start = 0;
            }

            full = false;
            modCount++;
        }

        return element;
    }

    @Override
    public E poll() {
        if (isEmpty()) {
            return null;
        }

        return remove();
    }

    @Override
    public E element() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }

        return peek();
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        }

        return elements[start];
    }

    /**
     * @return the last element added
     */
    public E tail() {
        if (isEmpty()) {
            return null;
        }

        if (end == 0) {
            return elements[maxElements - 1];
        } else {
            return elements[end - 1];
        }
    }

    public boolean isFull() {
        return full;
    }

    public int maxElements() {
        return maxElements;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private int index = start;
            private final int end = CircularQueue.this.end;
            private final int modCount = CircularQueue.this.modCount;

            private boolean isFirst = isFull();

            @Override
            public boolean hasNext() {
                if (index == end) {
                    return isFirst;
                }

                return true;
            }

            @Override
            public E next() {
                if (modCount != CircularQueue.this.modCount) {
                    throw new ConcurrentModificationException();
                }

                E e = elements[index++];
                if (index >= maxElements) {
                    index = 0;
                }

                isFirst = false;
                return e;
            }
        };
    }
}
