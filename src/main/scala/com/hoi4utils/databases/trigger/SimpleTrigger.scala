package com.hoi4utils.databases.trigger

trait SimpleTrigger extends Trigger {
	
	override def clone(): AnyRef = 
		super.clone().asInstanceOf[SimpleTrigger]
	
}
