package edu.busTracker.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static <T extends Comparable<T>> T earliest(List<T> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty");
        }
        T min = items.get(0);
        for (T item : items) {
            if (item.compareTo(min) < 0) min = item;
        }
        return min;
    }

    public static <T> Optional<T> firstMatch(List<T> items, Predicate<T> predicate) {
        for (T item : items) {
            if (predicate.test(item)) return Optional.of(item);
        }
        return Optional.empty();
    }
}
