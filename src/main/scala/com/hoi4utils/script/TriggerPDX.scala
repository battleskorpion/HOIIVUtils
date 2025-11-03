package com.hoi4utils.script

import com.hoi4utils.databases.effect.EffectDatabase._effects
import com.hoi4utils.databases.effect.{BlockEffect, Effect, SimpleEffect}
import com.hoi4utils.databases.trigger.{Trigger, TriggerDatabase}
import com.hoi4utils.parser.Node

class TriggerPDX extends CollectionPDX[Trigger](TriggerDatabase(), "trigger") {

	override def getPDXTypeName: String = "Trigger"

}

