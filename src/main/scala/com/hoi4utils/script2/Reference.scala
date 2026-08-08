package com.hoi4utils.script2

import com.hoi4utils.parser.NodeValueType


class Reference[T <: PDXEntity & Referable[?]](val id: T match { case Referable[k] => k }, val registry: Registry[? <: T]):
  type K = T match { case Referable[k] => k }

//  def value: Option[T] = registry resolve id
  // Cast id to registry.K so registry.resolve accepts it.
  // Because Option[+A] is covariant, Option[? <: T] safely conforms to Option[T].
  def value: Option[T] = registry.resolve(id.asInstanceOf[registry.K])    // !!!! cast is NOT redundant

  def exists: Boolean = value.isDefined

  override def toString: String = s"Reference($id)"

  /** Force-resolve the reference. */
  def $: T = value.getOrElse(
    throw new Exception(s"Reference to $id not resolved!")
  )

  /** Returns the [[Option]] of the resolved value */
  def apply(): Option[T] = value

object Reference:
  given referenceDecoder[T <: PDXEntity & Referable[?]](using reg: Registry[? <: T]): PDXDecoder[Reference[T]] with
    def decode(v: NodeValueType): Either[String, Reference[T]] =
      reg.idDecoder.decode(v).map { id =>
        // !!!! cast is NOT redundant
        new Reference[T](id.asInstanceOf[T match { case Referable[k] => k }], reg)
      }
