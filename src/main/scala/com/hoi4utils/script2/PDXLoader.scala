package com.hoi4utils.script2

import com.hoi4utils.parser.{Node, PDXValueNode, SeqNode}
import jdk.jpackage.internal.Arguments.CLIOptions.context

import scala.collection.mutable.ListBuffer
import scala.reflect.TypeTest

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
            script.load(sn, context, load) match {
              case Left(errs) => errors ++= errs
              case Right(_) => ()
            }
          case None => ()
    }

    entity match
      case registry: Registry[?] => registry.registerFrom(entity)
      case _ => ()
    entity match
      case referable: Referable[?] => handleReferable(node, referable)
      case _ => ()

    errors.toList

  /**
   * Loads a PDXValueNode into an existing entity.
   *
   * @param context The strictly typed context (e.g., a FocusTree or Registry)
   */
  def load(node: PDXValueNode[?], entity: PDXEntity, context: C): List[String] =
    val errors = ListBuffer[String]()

    for {
      id <- node.identifierToken.map(_.value)
      script <- entity.properties.get(id)
    } script.extractAndSet(node.rawValue) match
      case Left(err) => errors += s"Error in ${entity.getClass.getSimpleName} at $id: $err"
      case Right(_) => ()

    entity match
      case referable: Referable[?] => handleReferable(node, referable)    // todo this is ACTUALLY where stuck
      case _ => ()

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

  private def handleReferable(node: Node[?], referable: Referable[?])(using
    ttInt: TypeTest[Any, NameReferable[Int]],
    ttString: TypeTest[Any, NameReferable[String]]
  ): Unit =
    node.identifier match
      case Some(id) =>
        referable match
          case ttInt(r) => r.referableID =
            try id.toInt // TODO make sure this works right.s
            catch
              case e: NumberFormatException => throw PDXFormatException(s"Cannot convert $id to an Int", e)
          case ttString(r) => r.referableID = id
          case _ => ()
      case None =>
        referable match
          case r: NameReferable[?] => r.clearReferableID()
          case _ => ()
