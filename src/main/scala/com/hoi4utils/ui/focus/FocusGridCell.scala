package com.hoi4utils.ui.focus

import com.hoi4utils.hoi4mod.common.national_focus.Focus

class FocusGridCell(
                     private val focus: Option[Focus] = None,
                     private var cellCoordinates: (Int, Int) = (0, 0),
                     private var cellSize: (Double, Double),
                     private var lines: (Boolean, Boolean, Boolean, Boolean, Boolean) = (false, false, false, false, false)
                   ):
  def hasFocus: Boolean = focus.isDefined
  
  def getFocus: Option[Focus] = focus
  
  def getCellCoordinates: (Int, Int) = cellCoordinates
  
  def setCellCoordinates(newCoordinates: (Int, Int)): Unit = cellCoordinates = newCoordinates
  
  def getCellSize: (Double, Double) = cellSize
  
  def setCellSize(newSize: (Double, Double)): Unit = cellSize = newSize
  
  def getLines: (Boolean, Boolean, Boolean, Boolean, Boolean) = lines
  
  def setLines(newLines: (Boolean, Boolean, Boolean, Boolean, Boolean)): Unit = lines = newLines