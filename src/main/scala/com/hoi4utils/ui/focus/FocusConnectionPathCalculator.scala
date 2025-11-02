package com.hoi4utils.ui.focus

/**
 * Calculates connection paths between focuses and their prerequisites.
 * Algorithm: Focus exits from NORTH (top), prerequisite enters from SOUTH (bottom).
 * - Prerequisites ABOVE: Horizontal first (at focus level), then vertical up
 * - Prerequisites BELOW: Vertical first (down), then horizontal (at prerequisite level)
 */
class FocusConnectionPathCalculator(
                                     val cellWidth: Double,
                                     val cellHeight: Double,
                                     val lineOffset: Double = 15.0
                                   ):

  case class Point(x: Double, y: Double)
  case class PathSegment(from: Point, to: Point)

  /**
   * Calculate the path from a focus to one of its prerequisites.
   *
   * @param fromButton The source focus button
   * @param toButton The prerequisite focus button  
   * @return List of line segments forming the path
   */
  def calculatePath(fromButton: FocusToggleButton, toButton: FocusToggleButton): List[PathSegment] =
    val fromGridX = fromButton.getColumnIndex
    val fromGridY = fromButton.getRowIndex
    val toGridX = toButton.getColumnIndex
    val toGridY = toButton.getRowIndex

    // Exit point: NORTH (top center) of source focus
    val exitPoint = Point(
      fromGridX * cellWidth + cellWidth / 2,
      fromGridY * cellHeight + lineOffset
    )

    // Entry point: SOUTH (bottom center) of prerequisite
    val entryPoint = Point(
      toGridX * cellWidth + cellWidth / 2,
      toGridY * cellHeight + cellHeight - lineOffset
    )

    if (toGridY < fromGridY) then
      // Prerequisite is ABOVE
      calculatePathGoingUp(exitPoint, entryPoint, fromGridX, toGridX)
    else
      // Prerequisite is BELOW
      calculatePathGoingDown(exitPoint, entryPoint, fromGridX, toGridX)

  /**
   * Calculate path when prerequisite (entry focus) is ABOVE the focus (exit focus).
   * Pattern: Vertical up, then horizontal, then up to the entry focus.
   */
  private def calculatePathGoingUp(
                                    exitPoint: Point,
                                    entryPoint: Point,
                                    fromCol: Int,
                                    toCol: Int
                                  ): List[PathSegment] =
    if fromCol == toCol then
      // Same column: straight line up
      List(PathSegment(exitPoint, entryPoint))
    else
      // Different column: vertical then horizontal, then up to the entry focus
      val corner = Point(exitPoint.x, entryPoint.y - 1)
      val belowEntry = Point(entryPoint.x, entryPoint.y - 1)
      List(
        PathSegment(exitPoint, corner),       // Vertical almost up to entry focus
        PathSegment(corner, belowEntry),      // Horizontal from corner to below entry focus
        PathSegment(belowEntry, entryPoint)   // Vertical up into entry focus
      )

  /**
   * Calculate path when prerequisite is BELOW the focus.
   * Pattern: Vertical first (down), then horizontal (at prerequisite entry level).
   */
  private def calculatePathGoingDown(
                                      exitPoint: Point,
                                      entryPoint: Point,
                                      fromCol: Int,
                                      toCol: Int
                                    ): List[PathSegment] =
    if (fromCol == toCol) then
      // Same column: straight line down
      List(PathSegment(exitPoint, entryPoint))
    else
      // Different column: vertical then horizontal
      val corner = Point(exitPoint.x, entryPoint.y)
      List(
        PathSegment(exitPoint, corner),       // Vertical down
        PathSegment(corner, entryPoint)       // Horizontal at prerequisite level
      )

/**
 * Extension methods for FocusToggleButton to get grid indices.
 */
extension (btn: FocusToggleButton)
  def getColumnIndex: Int =
    val idx = javafx.scene.layout.GridPane.getColumnIndex(btn)
    if idx == null then 0 else idx

  def getRowIndex: Int =
    val idx = javafx.scene.layout.GridPane.getRowIndex(btn)
    if idx == null then 0 else idx