//package com.hoi4utils.hoi4.map.resource
//
//import com.hoi4utils.script.PDXReadable
//import com.typesafe.scalalogging.LazyLogging
//import zio.{Task, UIO, URIO, URLayer, ZIO, ZLayer}
//
//trait ResourcesFileService extends PDXReadable with LazyLogging {
//
//}
//
//object ResourcesFileService {
//  val live: URLayer[Any, ResourcesFileService] =
//    ZLayer.derive[ResourcesFileServiceImpl]
//}
//
//case class ResourcesFileServiceImpl() extends ResourcesFileService {
//
//  override def read(): Task[Boolean] = ResourcesFile.read()
//
//  override def clear(): Task[Unit] = ResourcesFile.clear()
//
//}
