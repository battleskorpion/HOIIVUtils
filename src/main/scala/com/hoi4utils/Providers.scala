package com.hoi4utils

import scala.Tuple.:*

object Providers {
  import TLP.*

  opaque type Provider[A] = A // zero-cost wrapper

  def provide[A](value: A): Provider[A] = value
  def provided[A](using p: Provider[A]): A = p

  given [T <: Tuple, A](using p: Provider[T], select: Select[T, A]): Provider[A] =
    select(p)

  given [A, T <: Tuple](using p: Provider[A], pt: Provider[T]): Provider[A *: T] =
    p *: pt

  given Provider[EmptyTuple] = EmptyTuple
}

object TLP {
  import compiletime.ops.int.S

  trait Select[T <: Tuple, A]  {
    def apply(t: T): A
  }

  // IndexOf[(Int, String, List[String]), String] = 1
  type IndexOf[T <: Tuple, A] <: Int = T match
    case A *: _ => 0  // the TYPE zero
    case _ *: rt => S[IndexOf[rt, A]]

  // (Int, String, List[Int])
  given [T <: NonEmptyTuple, A](using index: ValueOf[IndexOf[T, A]]): Select[T, A] with
    def apply(tuple: T): A = tuple(index.value).asInstanceOf[A]
}
