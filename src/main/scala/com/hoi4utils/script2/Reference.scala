package com.hoi4utils.script2


class Reference[K, T <: PDXEntity & Referable[K]](val id: K, val registry: Registry[K, T]):

  def value: Option[T] = registry resolve id

  def exists: Boolean = value.isDefined

  override def toString: String = s"Reference($id)"

  /** Force-resolve the reference. */
  def $: T = value.getOrElse(
    throw new Exception(s"Reference to $id not resolved!")
  )

  /** Returns the [[Option]] of the resolved value */
  def apply(): Option[T] = value
