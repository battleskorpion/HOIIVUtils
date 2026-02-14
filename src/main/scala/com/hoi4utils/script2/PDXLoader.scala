package com.hoi4utils.script2

import com.hoi4utils.parser.{PDXValueNode, SeqNode}
import jdk.jpackage.internal.Arguments.CLIOptions.context

import scala.collection.mutable.ListBuffer

class PDXLoader[C]:
  
  /**
   * Loads a SeqNode into an existing entity.
   *
   * @param context The strictly typed context (e.g., a FocusTree or Registry)
   */
  def load(node: SeqNode, entity: PDXEntity, context: C): List[String] =
    val errors = ListBuffer[String]()

    node.rawValue.foreach {
      case vn: PDXValueNode[?] =>
        for {
          id <- vn.identifierToken.map(_.value)
          script <- entity.properties.get(id)
        } script.extractAndSet(vn.rawValue) match
          case Left(err) => errors += s"Error in ${entity.getClass.getSimpleName} at $id: $err"
          case Right(_)  => ()

      case sn: SeqNode =>
        val id = sn.identifierToken.map(_.value).getOrElse("")
        entity.properties.get(id) match
          case Some(script) =>
            script.getEmptyInstance(context) match
              case Some(childEntity) =>
                // Recursively load the block
                errors ++= load(sn, childEntity, context)
                script match
                  case s: PDXScript[t] =>
                    // check if childEntity can actually be treated as 't'
                    childEntity match
                      case validChild: t => s.set(validChild)
                      case _ => errors += s"Type mismatch: $id expects ${script.getClass.getSimpleName}, but got ${childEntity.getClass.getSimpleName}"
              case None =>
                sn.rawValue.foreach {
                  case cvn: PDXValueNode[?] => script.extractAndSet(cvn.rawValue)
                  case _ => ()
                }
          case None => ()
    }
    errors.toList

  private def instantiateEntity(clazz: Class[?], context: C): PDXEntity =
    // We check for a constructor matching C or a supertype of C
    val constructor = clazz.getConstructors.find { c =>
      c.getParameterTypes.exists(_.isAssignableFrom(context.getClass))
    }.getOrElse(clazz.getConstructor())
  
    val instance = if (constructor.getParameterCount == 1)
      constructor.newInstance(context)
    else
      constructor.newInstance()
  
    instance.asInstanceOf[PDXEntity]

