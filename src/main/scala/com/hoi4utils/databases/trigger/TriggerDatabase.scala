package com.hoi4utils.databases.trigger

import com.hoi4utils.databases.effect.EffectDatabase._effects
import com.hoi4utils.databases.effect.{BlockEffect, SimpleEffect}
import com.hoi4utils.parser.{Node, PDXValueNode, SeqNode}
import com.hoi4utils.script.PDXSupplier

object TriggerDatabase:

	private var _triggers: List[Trigger] = List()

	def init(): Unit = {

	}

	def apply(): PDXSupplier[Trigger] =
		new PDXSupplier[Trigger] {
			override def simplePDXSupplier(): Option[PDXValueNode[?] => Option[SimpleTrigger]] = {
				Some((expr: PDXValueNode[?]) => {
					_triggers.filter(_.isInstanceOf[SimpleTrigger])
						.find(_.pdxIdentifier == expr.name)
						.map(_.clone().asInstanceOf[SimpleTrigger])
				})
			}

			override def blockPDXSupplier(): Option[SeqNode => Option[BlockTrigger]] = {
				Some((expr: SeqNode) => {
					_triggers.filter(_.isInstanceOf[BlockTrigger])
						.find(_.pdxIdentifier == expr.name)
						.map(_.clone().asInstanceOf[BlockTrigger])
				})
			}
		}

	def triggers: List[Trigger] = _triggers


class TriggerDatabase {


}
