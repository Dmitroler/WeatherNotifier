package com.finalProject.dmitroLer.weathernotifier.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerItems {

    public static final List<RecyclerItem> ITEMS = new ArrayList<>();

    public static final Map<String, RecyclerItem> ITEM_MAP = new HashMap<>();

    private static void addItem(RecyclerItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class RecyclerItem {
        public String id;
        public final String content;

        public RecyclerItem(String id, String content) {
            this.id = id;
            this.content = content;
            addItem(this);
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
