package com.hoi4utils.hoi42.common.country_tags

import com.hoi4utils.script2.{PDXDecoder, PDXEntity, PDXInlineEntityDynamicKey, PDXProperty, Registry, RegistryMember}
import com.hoi4utils.script2.NameReferable

class CountryTag(var tags: CountryTagRegistry) extends PDXInlineEntityDynamicKey[String] with RegistryMember[CountryTag](tags) with NameReferable[String] {

}

class CountryTagRegistry extends Registry[CountryTag] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
