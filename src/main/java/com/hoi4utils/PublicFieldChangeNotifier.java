package com.hoi4utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PublicFieldChangeNotifier {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<String, Object> fieldValues = new HashMap<>();
    private final Class<?> targetClass;

    public PublicFieldChangeNotifier(Class<?> targetClass) {
        this.targetClass = targetClass;
        initializeFieldValues();
    }

    private void initializeFieldValues() {
        for (Field field : targetClass.getFields()) {
            try {
                fieldValues.put(field.getName(), field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void checkAndNotifyChanges(Runnable runnable) {
        for (Field field : targetClass.getFields()) {
            try {
                Object oldValue = fieldValues.get(field.getName());
                Object newValue = field.get(null);
                if ((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue))) {
                    fieldValues.put(field.getName(), newValue);
                    pcs.firePropertyChange(field.getName(), oldValue, newValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
