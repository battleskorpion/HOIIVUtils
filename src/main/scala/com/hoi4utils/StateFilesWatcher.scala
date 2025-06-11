package com.hoi4utils

import com.hoi4utils.StateFilesWatcher.statesThatChanged
import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.map.State

import java.awt.EventQueue
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class StateFilesWatcher:
  private var stateFilesWatcher: FileWatcher = uninitialized
  private val stateDir = HOIIVFiles.Mod.states_folder
  
  startWatching()

  private def startWatching(): Unit =
    if stateDir == null || !HOIIVFiles.isValidDirectory(stateDir.getPath) then return

    stateFilesWatcher = new FileWatcher(stateDir)
    stateFilesWatcher.addListener(new FileAdapter {
      override def onCreated(event: FileEvent): Unit =
        handleStateFileEvent(event, "created/loaded", file => State.readState(file))
      override def onModified(event: FileEvent): Unit =
        handleStateFileEvent(event, "modified", file => State.readState(file))
      override def onDeleted(event: FileEvent): Unit =
        handleStateFileEvent(event, "deleted", file => State.removeState(file))
    })
    stateFilesWatcher.watch()

  private def handleStateFileEvent(event: FileEvent, actionName: String, stateAction: File => Unit): Unit =
    EventQueue.invokeLater(() =>
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction + 1
      val file = event.getFile
      if file != null then stateAction(file)
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction - 1
      statesThatChanged.addOne(s"State was $actionName: ${State.get(file)}")
    )
    
    
object StateFilesWatcher:
  val statesThatChanged: ListBuffer[String] = ListBuffer.empty[String]