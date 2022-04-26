package fr.poulpogaz.animescandl.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SizedHashMapTest {

    @Test
    public void test() {
        SizedHashMap<Character, Character> map = new SizedHashMap<>();
        map.setMaxSize(10);

        for (int i = 0; i < 100; i++) {
            put(map, (char) i, (char) (i + 1));
            assertEquals(Math.min(i + 1, map.getMaxSize()), map.size());
        }

        map.setMaxSize(1000);
        assertEquals(10, map.size());
        map.setMaxSize(5);
        assertEquals(5, map.size());

        for (int i = 0; i < 100; i++) {
            if (i >= 95) {
                assertTrue(map.containsKey((char) i));
                assertTrue(map.containsValue((char) (i + 1)));
            } else {
                assertFalse(map.containsKey((char) i));
                assertFalse(map.containsValue((char) (i + 1)));
            }
        }

        map.setMaxSize(0);
        assertEquals(0, map.size());

        for (int i = 0; i < 100; i++) {
            put(map, (char) i, (char) (i + 1));
            assertEquals(0, map.size());
        }

        map.setMaxSize(-1);
        for (int i = 0; i < 100; i++) {
            put(map, (char) i, (char) (i + 1));
            assertEquals(i + 1, map.size());
        }

        map.setMaxSize(10);

        for (int i = 0; i < 100; i++) {
            put(map, (char) i, (char) (i + 1));
            assertEquals(10, map.size());
        }
    }

    private <K, V> void put(SizedHashMap<K, V> map, K key, V value) {
        int oldSize = map.size();

        if (oldSize > map.getMaxSize() && !map.isInfinite()) {
            throw new IllegalStateException("Size exceeded");

        } else if (oldSize < map.getMaxSize() || map.isInfinite()) {
            map.put(key, value);

        } else if (map.getMaxSize() == 0) {
            map.put(key, value);
            assertEquals(map.size(), oldSize);

        } else if (oldSize == map.getMaxSize()) {
            map.put(key, value);

            assertEquals(map.size(), oldSize);
            assertTrue(map.containsKey(key));
            assertTrue(map.containsValue(value));
        }
    }
}
