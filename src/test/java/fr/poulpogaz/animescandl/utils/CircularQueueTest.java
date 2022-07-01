package fr.poulpogaz.animescandl.utils;

import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CircularQueueTest {

    @Test
    void add() {
        CircularQueue<Integer> q = new CircularQueue<>(10);
        assertEquals(0, q.size());

        for (int i = 0; i < 100; i++) {
            q.add(i);
            int expectSize = Math.min(i + 1, q.maxElements());
            assertEquals(expectSize, q.size());
        }
    }

    @Test
    void remove() {
        CircularQueue<Integer> q = new CircularQueue<>(10);

        for (int i = 0; i < q.maxElements() + 5; i++) {
            q.add(i);
        }

        for (int i = 0; i < q.maxElements(); i++) {
            assertEquals(i + 5, q.remove());
        }

        assertThrows(NoSuchElementException.class, q::remove);
    }

    @Test
    void poll() {
        CircularQueue<Integer> q = new CircularQueue<>(10);

        for (int i = 0; i < q.maxElements() + 5; i++) {
            q.add(i);
        }

        for (int i = 0; i < q.maxElements(); i++) {
            assertEquals(i + 5, q.poll());
        }

        assertNull(q.poll());
    }

    @Test
    void element() {
        CircularQueue<Integer> q = new CircularQueue<>(10);

        for (int i = 0; i < q.maxElements() + 5; i++) {
            q.add(i);
        }

        for (int i = 0; i < q.maxElements(); i++) {
            assertEquals(i + 5, q.element());
            q.remove();
        }

        assertThrows(NoSuchElementException.class, q::element);
    }

    @Test
    void peek() {
        CircularQueue<Integer> q = new CircularQueue<>(10);

        for (int i = 0; i < q.maxElements() + 5; i++) {
            q.add(i);
        }

        for (int i = 0; i < q.maxElements(); i++) {
            assertEquals(i + 5, q.peek());
            q.remove();
        }

        assertNull(q.peek());
    }

    @Test
    void isFull() {
        CircularQueue<Integer> q = new CircularQueue<>(10);

        for (int i = 0; i < q.maxElements() + 5; i++) {
            q.add(i);

            if (i + 1 >= q.maxElements()) {
                assertTrue(q.isFull());
            } else {
                assertFalse(q.isFull());
            }
        }
    }

    @Test
    void iterator() {
        CircularQueue<Integer> q = new CircularQueue<>(10);
        q.add(0);
        q.add(0);
        q.add(0);

        for (int i = 0; i < q.maxElements(); i++) {
            q.add(i);
        }

        int k = 0;
        for (int i : q) {
            assertEquals(k, i);
            k++;
        }
        assertEquals(q.maxElements(), k);

        assertThrows(ConcurrentModificationException.class, () -> {
            for (int i : q) {
                q.offer(i);
            }
        });
    }
}