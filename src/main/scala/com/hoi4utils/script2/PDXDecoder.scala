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

      val instanceOpt = clazz.getConstructors.iterator.flatMap { c =>
        val paramTypes = c.getParameterTypes
        val resolvedArgs = paramTypes.flatMap(p => findArg(context, p))
        if resolvedArgs.length == paramTypes.length then
          try Some(c.newInstance(resolvedArgs*).asInstanceOf[T])
          catch case _ => None
        else None
      }.nextOption()
      val instance = instanceOpt.getOrElse {
        try
          clazz.getConstructor().newInstance()
        catch
          case e: NoSuchMethodException => throw PDXDecoderException(s"There is no constructor for ${clazz.getName} which supports $context")
      }

//      val instance = clazz.getConstructors.find { c =>
//        c.getParameterTypes.exists(_.isAssignableFrom(context.getClass))
//      } match
//        case Some(c) => c.newInstance(context)
//        case None =>
//          try
//            clazz.getConstructor().newInstance()
//          catch
//            case e: NoSuchMethodException => throw PDXDecoderException(s"There is no constructor for ${clazz.getName} which supports $context")

      Some(instance.asInstanceOf[T])

    // todo worried about this...
    private def findArg(context: Any, paramType: Class[?]): Option[AnyRef] =
      if context == null then None
      else if paramType.isAssignableFrom(context.getClass) then
        Some(context.asInstanceOf[AnyRef])
      else
        // 1. Look for zero-parameter methods on context (e.g. Scala 3 givens/getters)
        val methodMatch = context.getClass.getMethods
          .filter(m => m.getParameterCount == 0 && paramType.isAssignableFrom(m.getReturnType))
          .flatMap { m =>
            try
              m.setAccessible(true)
              Option(m.invoke(context))
            catch case _ => None
          }.headOption

        methodMatch.orElse {
          // 2. Look for fields on context matching the parameter type
          def getAllFields(c: Class[?]): List[java.lang.reflect.Field] =
            if c == null || c == classOf[Object] then Nil
            else c.getDeclaredFields.toList ::: getAllFields(c.getSuperclass)

          getAllFields(context.getClass)
            .filter(f => paramType.isAssignableFrom(f.getType))
            .flatMap { f =>
              try
                f.setAccessible(true)
                Option(f.get(context))
              catch case _ => None
            }.headOption
        }

  given listDecoder[T](using elementDecoder: PDXDecoder[T]): PDXDecoder[List[T]] with
    override def decode(v: NodeValueType): Either[String, List[T]] =
      elementDecoder.decode(v).map(List(_))

    override def createEmpty(context: Any): Option[List[T]] =
      elementDecoder.createEmpty(context).map(List(_))
