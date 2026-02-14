package com.hoi4utils.script2

import com.hoi4utils.parser.{PDXValueType, SeqNode}
import com.hoi4utils.parser.NodeExtensions.find
import com.sun.tools.javac.code.TypeTag

import java.io.File
import scala.reflect.ClassTag

trait PDXEntity:
  // Recursive reflection to find fields in parent classes too
  lazy val properties: Map[String, PDXScript[?]] =
    def getFields(cls: Class[?]): Map[String, PDXScript[?]] =
      val local = cls.getDeclaredFields.toList
        .filter(f => classOf[PDXScript[?]].isAssignableFrom(f.getType))
        .map { f =>
          f.setAccessible(true)
          val p = f.get(this).asInstanceOf[PDXScript[?]]
          p.pdxKey -> p   // map from pdx script key (name) to property
        }.toMap
      val parent = Option(cls.getSuperclass).map(getFields).getOrElse(Map.empty)
      parent ++ local // concat favors rhs (subclass)

    getFields(this.getClass)
  
  def pdx[T <: PDXValueType | PDXEntity | Reference[?]](key: String)(using PDXDecoder[T], ClassTag[T]): PDXProperty[T] =
    new PDXProperty[T](key, None)

  def pdxList[T <: PDXValueType | PDXEntity | Reference[?]](key: String)(using PDXDecoder[T], ClassTag[T]): PDXPropertyList[T] =
    new PDXPropertyList[T](key, None)
