package com.hoi4utils.script2

import com.sun.crypto.provider.ML_KEM_Impls.K


class Reference[T <: PDXEntity & Referable[?]](val id: T match { case Referable[k] => k }, val registry: Registry[T]):
  type K = T match { case Referable[k] => k }
  
  def value: Option[T] = registry resolve id

  def exists: Boolean = value.isDefined

  override def toString: String = s"Reference($id)"

  /** Force-resolve the reference. */
  def $: T = value.getOrElse(
    throw new Exception(s"Reference to $id not resolved!")
  )

  /** Returns the [[Option]] of the resolved value */
  def apply(): Option[T] = value
