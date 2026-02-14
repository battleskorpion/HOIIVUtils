package com.hoi4utils.script2

import com.hoi4utils.parser.{NodeSeq, NodeValueType}
import com.sun.tools.javac.resources.ct

import scala.reflect.ClassTag

trait PDXDecoder[T]:
  /** Returns Right(T) if successful, Left(ErrorMessage) if the type is wrong */
  def decode(value: NodeValueType): Either[String, T]

  def createEmpty(context: Any): Option[T] = None

object PDXDecoder:
  given PDXDecoder[String] with
    override def decode(v: NodeValueType): Either[String, String] = v match
      case s: String => Right(s)
      case _ => Left(s"Expected String, got ${v.getClass.getSimpleName}")

  given PDXDecoder[Double] with
    override def decode(v: NodeValueType): Either[String, Double] = v match
      case d: Double => Right(d)
      case i: Int    => Right(i.toDouble)
      case _ => Left(s"Expected Double/Number, got ${v.getClass.getSimpleName}")

  given PDXDecoder[Int] with
    override def decode(v: NodeValueType): Either[String, Int] = v match
      case i: Int => Right(i)
      case d: Double => Right(d.toInt)
      case _ => Left(s"Expected Int, got ${v.getClass.getSimpleName}")

  given PDXDecoder[Boolean] with
    override def decode(v: NodeValueType): Either[String, Boolean] = v match
      case b: Boolean   => Right(b)
      case s: String => s.toLowerCase match
        case "yes" | "true" => Right(true)
        case "no" | "false" => Right(false)
        case _ => Left(s"Expected 'yes'/'no', got string '$s'")
      case _ => Left(s"Expected Boolean, got ${v.getClass.getSimpleName}")

  given entityDecoder[T <: PDXEntity](using ct: ClassTag[T]): PDXDecoder[T] with
    override def decode(v: NodeValueType): Either[String, T] = v match
      case e: T @unchecked => Right(e) // Already instantiated and loaded by PDXLoader
      case _ => Left(s"Expected nested entity, got ${v.getClass.getSimpleName}")

    override def createEmpty(context: Any): Option[T] =
      val clazz = ct.runtimeClass
      val instance = clazz.getConstructors.find { c =>
        c.getParameterTypes.exists(_.isAssignableFrom(context.getClass))
      } match
        case Some(c) => c.newInstance(context)
        case None => clazz.getConstructor().newInstance()

      Some(instance.asInstanceOf[T])

  given listDecoder[T](using elementDecoder: PDXDecoder[T]): PDXDecoder[List[T]] with
    override def decode(v: NodeValueType): Either[String, List[T]] =
      elementDecoder.decode(v).map(List(_))

    override def createEmpty(context: Any): Option[List[T]] =
      elementDecoder.createEmpty(context).map(List(_))
