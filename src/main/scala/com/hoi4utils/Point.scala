package com.hoi4utils

import scala.math.Numeric.Implicits.infixNumericOps

type IntPoint = Point[Int]

case class Point[N : Numeric](x: N, y: N):

  infix def +(other: Point[N]): Point[N] = Point[N](x + other.x, y + other.y)
  infix def -(other: Point[N]): Point[N] = Point[N](x - other.x, y - other.y)
  
  infix def unary_- : Point[N] = Point(-x, -y) 
  
  def manhattanDistance(p: Point[N]): N = (x - p.x).abs + (y - p.y).abs
  
  def isZero: Boolean = this == Point.Zero 
  
object Point:
  val Up    = Point(0, -1)
  val Down  = Point(0, 1)
  val Left  = Point(-1, 0)
  val Right = Point(1, 0)
  val Zero = Point(0, 0)
