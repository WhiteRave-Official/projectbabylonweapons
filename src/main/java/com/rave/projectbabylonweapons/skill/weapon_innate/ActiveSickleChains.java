package com.rave.projectbabylonweapons.skill.weapon_innate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

public final class ActiveSickleChains<T> {
    private final LinkedHashMap<Integer, T> entries = new LinkedHashMap<>();
    private int anchor = -1;

    public void add(int id, T value) { entries.put(id, value); }
    public void select(int id) { if (entries.containsKey(id)) anchor = id; }
    public void remove(int id) { entries.remove(id); if (anchor == id) anchor = -1; }
    public int size() { return entries.size(); }
    public List<T> values() { return new ArrayList<>(entries.values()); }

    public T anchor(Predicate<T> tethered) {
        T current = entries.get(anchor);
        if (current != null && tethered.test(current)) return current;
        T selected = null;
        for (var entry : entries.entrySet()) {
            if (tethered.test(entry.getValue())) {
                anchor = entry.getKey();
                selected = entry.getValue();
            }
        }
        return selected;
    }
}
