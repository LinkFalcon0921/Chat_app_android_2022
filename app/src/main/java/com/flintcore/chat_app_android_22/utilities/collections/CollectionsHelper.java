package com.flintcore.chat_app_android_22.utilities.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CollectionsHelper {

    public static <K, V> HashMap<K, V> getHashMap() {
        return new HashMap<>();
    }

    public static <K> List<K> getArrayList() { return new ArrayList<>();}
}
