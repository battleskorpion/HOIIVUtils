package com.hoi4utils

import java.beans.{PropertyChangeListener, PropertyChangeSupport}
import java.util

class PublicFieldChangeNotifier(private val targetClass: Class[?]) {
  final private val pcs = new PropertyChangeSupport(this)
  final private val fieldValues = new util.HashMap[String, AnyRef]
  initializeFieldValues()

  private def initializeFieldValues(): Unit = {
    for (field <- targetClass.getFields) {
      try fieldValues.put(field.getName, field.get(null))
      catch {
        case e: IllegalAccessException =>
          e.printStackTrace()
      }
    }
  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    pcs.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    pcs.removePropertyChangeListener(listener)
  }

  def checkAndNotifyChanges(): Unit = {
    for (field <- targetClass.getFields) {
      try {
        val oldValue = fieldValues.get(field.getName)
        val newValue = field.get(null)
        if ((oldValue == null && newValue != null) || (oldValue != null && !(oldValue == (newValue)))) {
          fieldValues.put(field.getName, newValue)
          pcs.firePropertyChange(field.getName, oldValue, newValue)
        }
      } catch {
        case e: IllegalAccessException =>
          e.printStackTrace()
      }
    }
  }
}