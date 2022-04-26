package fr.poulpogaz.animescandl.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SizedHashMap<K, V> extends LinkedHashMap<K, V> {

    /**
     * A negative number means no limit
     */
    private int maxSize = -1;

    public SizedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SizedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public SizedHashMap() {
    }

    public SizedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public SizedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    @Override
    public V put(K key, V value) {
        if (maxSize != 0) {
            return super.put(key, value);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (maxSize != 0) {
            super.putAll(m);
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (maxSize != 0) {
            return super.compute(key, remappingFunction);
        } else {
            return null;
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (maxSize != 0) {
            return super.computeIfAbsent(key, mappingFunction);
        } else {
            return null;
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (maxSize != 0) {
            return super.computeIfPresent(key, remappingFunction);
        } else {
            return null;
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize && maxSize >= 0;
    }

    public boolean isInfinite() {
        return maxSize < 0;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        if (maxSize == 0) {
            clear();
        }  else if (size() > maxSize && maxSize > 0) {
            entrySet().stream()
                    .limit(size() - maxSize)
                    .toList()
                    .forEach(n -> remove(n.getKey(), n.getValue()));
        }
        this.maxSize = maxSize;
    }
}
