package com.hoi4utils.script2

import com.hoi4utils.parser.NodeValueType

import scala.collection.mutable
import scala.reflect.ClassTag

abstract class Registry[T <: PDXEntity & Referable[?]]: 
  self => 
  
  type K = T match {case Referable[k] => k}

  private val _referableEntities: mutable.Map[K, T] = mutable.Map.empty[K, T]
  /** Incremented whenever the map changes */
  private var _version: Int = 0

  def version: Int = _version

  infix def register(entity: T): Unit =
    entity.referableID match
      case Some(actualKey: K) =>
        _referableEntities += (actualKey -> entity)
        _version += 1
      case None =>
        // TODO improve error handling in future
        throw new IllegalArgumentException(s"Cannot register entity without an ID.")

  infix def resolve(id: K): Option[T] = _referableEntities.get(id)
  
  def referableEntities: Iterable[T] = _referableEntities.values

  /** The registry MUST provide a decoder for its specific ID type K */ 
  def idDecoder: PDXDecoder[K]

trait RegistryMember[T <: PDXEntity & Referable[?]](val registry: Registry[T]) extends PDXEntity:
  given Registry[T] = registry
