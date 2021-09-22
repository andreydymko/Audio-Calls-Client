package com.andreydymko.spoaudiocalls.Utils;

import java.util.Collection;
import java.util.List;

public class CollectionsCompat {
    public static <T> boolean addUnique(Collection<T> collection, T item) {
        if (collection.contains(item)) {
            return false;
        }
        return collection.add(item);
    }

    public static <T> int addUniqueUpdating(List<T> collection, T item) {
        int idx = collection.indexOf(item);
        if (idx >= 0) {
            collection.set(idx, item);
            return idx;
        }
        collection.add(item);
        return collection.size() - 1;
    }
}
