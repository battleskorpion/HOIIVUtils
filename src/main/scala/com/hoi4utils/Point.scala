package com.hoi4utils

case class Point[N <: Int | Double](x: N, y: N):

  infix def +(other: Point[N]): Point[N] = Point[N](x + other.x, y + other.y)

  infix def -(other: Point[N]): Point[N] = Point[N](x - other.x, y - other.y)
