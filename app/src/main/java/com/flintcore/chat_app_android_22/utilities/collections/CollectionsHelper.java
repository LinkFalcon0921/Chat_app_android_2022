package com.flintcore.chat_app_android_22.utilities.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CollectionsHelper {

//    Maps
    public static <K, V> HashMap<K, V> getHashMap() {
        return new HashMap<>();
    }

//    Arrays and Lists
    public static <K> List<K> getArrayList() { return new ArrayList<>();}
    public static <K> List<K> getLinkedList() { return new LinkedList<>();}

//    Sets
public static <K> Set<K> getHashSet() { return new HashSet<>();}

}
