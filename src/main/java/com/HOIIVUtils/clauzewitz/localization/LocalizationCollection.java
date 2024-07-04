package com.HOIIVUtils.clauzewitz.localization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalizationCollection extends HashMap<File, List<Localization>> {
    /**
     * very important for lookup by key optimization.
     */
    private final ConcurrentMap<String, Localization> localizationKeyMap = new ConcurrentHashMap<>();

    // Custom method to add a Localization
    public void add(Localization localization, File file) {
        if (localization == null || file == null) {
            throw new IllegalArgumentException("Localization and file must not be null");
        }
        List<Localization> localizationsList = this.computeIfAbsent(file, k -> new ArrayList<>());
        localizationsList.add(localization);
        localizationKeyMap.put(localization.ID(), localization); // Indexing for fast retrieval
    }

    // Custom method to remove a Localization
    public boolean remove(Localization localization) {
        if (localization == null) {
            return false;
        }
        boolean removed = this.values().parallelStream().anyMatch(localizationsList -> localizationsList.remove(localization));
        if (removed) localizationKeyMap.remove(localization.ID()); // Remove from index
        removeListIfEmpty();
        return removed;
    }

    private boolean removeListIfEmpty() {
        return this.values().removeIf(List::isEmpty);
    }

    // Custom method to get all Localizations
    public List<Localization> getAll() {
        return localizationKeyMap.values().parallelStream().toList();
    }

    public List<Localization> getAll(List<String> localizationKeys) {
        return localizationKeyMap.values().parallelStream()
                .filter(localization -> localizationKeys.contains(localization.ID()))
                .toList();
    }

    // total number of localizations
    public int numLocalization() {
        return this.values().parallelStream().mapToInt(List::size).sum();
    }

    @Override
    public List<Localization> put(File key, List<Localization> value) {
        throw new UnsupportedOperationException("Use add(Localization, File) method instead");
    }

    @Override
    public List<Localization> remove(Object key) {
        throw new UnsupportedOperationException("Use remove(Localization) method instead");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Use containsLocalizationKey(String) method instead");
    }

    public boolean containsLocalizationKey(String id) {
        return localizationKeyMap.containsKey(id); // Fast lookup from index
    }

    public Localization get(String key) {
        return localizationKeyMap.get(key); // Fast lookup from index
    }

    // Method to filter localizations by status
    public List<Map.Entry<File, List<Localization>>> filterByStatus(Localization.Status status) {
        return this.entrySet().parallelStream()
                .map(entry -> Map.entry(entry.getKey(),
                        entry.getValue().parallelStream()
                                .filter(localization -> localization.status() == status)
                                .toList()))
                .filter(entry -> !entry.getValue().isEmpty())
                .toList();
    }

    public Localization replace(String key, Localization localization) {
        if (localization == null) {
            throw new IllegalArgumentException("Localization must not be null");
        }

        for (Map.Entry<File, List<Localization>> entry : this.entrySet()) {
            List<Localization> localizationsList = entry.getValue();
            for (int i = 0; i < localizationsList.size(); i++) {
                Localization currentLocalization = localizationsList.get(i);
                if (currentLocalization.ID().equals(key)) {
                    // Replace the localization and return the old one
                    localizationsList.set(i, localization);
                    return currentLocalization;
                }
            }
        }
        // Return null if no localization with the given key was found
        return null;
    }


    public File getLocalizationFile(String key) {
        return this.entrySet().parallelStream()
                .filter(entry -> entry.getValue().parallelStream()
                        .anyMatch(localization -> localization.ID().equals(key)))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }
}
