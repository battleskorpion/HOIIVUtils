package com.hoi4utils.hoi42.map.resource

import com.hoi4utils.script2.{NameReferable, PDXDecoder, PDXEntity, PDXInlineEntityDynamicKey, PDXProperty, Registry, RegistryMember}

// todo todo copied countrytag but will prob cause issues and need to change pdx 
class Resource(var resourceTypes: ResourceTypesRegistry) extends PDXInlineEntityDynamicKey[Double] with RegistryMember[Resource](resourceTypes) with NameReferable[String] {

}

object Resource:
  // todo need some other way besides Loader to make things.
  val NONE: Set[Resource] = Set.empty

class ResourceTypesRegistry extends Registry[Resource] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
