package com.hoi4utils.script2

import com.hoi4utils.parser.NodeValueType

import scala.collection.mutable
import scala.reflect.ClassTag

trait Registry[T <: PDXEntity & Referable[?]](using val ct: ClassTag[T]):
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

  infix def register(entities: Iterable[T]): Unit =
    val entries = entities.map(entity =>
      entity.referableID match
        case Some(actualKey: K) =>
          actualKey -> entity
        case None =>
          // TODO improve error handling in future
          throw new IllegalArgumentException(s"Cannot register entity without an ID.")
    )
    _referableEntities ++= entries
    _version += 1

  infix def resolve(id: K): Option[T] = _referableEntities.get(id)

  def referableEntities: Iterable[T] = _referableEntities.values

  /** The registry MUST provide a decoder for its specific ID type K */
  def idDecoder: PDXDecoder[K]

  // TODO change to register from self using self => pdxEntity or something. (or just addtl. to this)
  def registerFrom(entity: PDXEntity): Unit =
    val entities: Iterable[T] =
      entity.properties.values.flatMap {
        case p: PDXProperty[_] => p() match
          case Some(v: T) => Some(v)
          case _ => None
//        case v: PDXPropertyList[_] => v() match
//          case Some(l) => if ct.runtimeClass.isAssignableFrom(v.runtimeClass) then l.map(_.asInstanceOf[T]) else Nil
//          case _ => Nil
        case pl: PDXPropertyList[_] =>
          pl() match
            case Some(list) => list.collect { case tt: T => tt }
            case None => Nil
        case _ => Nil
      }
    this register entities

trait RegistryMember[T <: PDXEntity & Referable[?]](val registry: Registry[T]) extends PDXEntity:
//  self: Referable[T] =>
  given Registry[T] = registry
