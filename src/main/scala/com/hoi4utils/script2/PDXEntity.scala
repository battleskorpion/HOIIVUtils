package com.hoi4utils.script2

import com.hoi4utils.parser.SeqNode
import com.hoi4utils.parser.NodeExtensions.find

import java.io.File

trait PDXEntity:
  // Recursive reflection to find fields in parent classes too
  private lazy val properties: Map[String, PDXScript[?]] =
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

  def pdx[T](key: String): PDXProperty[T] =
    new PDXProperty[T](key, None)
    
  def pdxList[T](key: String): PDXPropertyList[T] =
    new PDXPropertyList[T](key, None)
