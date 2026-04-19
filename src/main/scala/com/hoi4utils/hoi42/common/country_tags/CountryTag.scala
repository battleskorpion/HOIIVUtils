package com.hoi4utils.hoi42.common.country_tags

import com.hoi4utils.script2.{NameReferable, PDXDecoder, PDXEntity, PDXInlineEntityDynamicKey, PDXProperty, Registry, RegistryMember}

class CountryTag(var tags: CountryTagRegistry) extends PDXInlineEntityDynamicKey[String] with RegistryMember[CountryTag](tags) with NameReferable[String] {

}

object CountryTag:
  // todo need some other way besides Loader to make things. such as NULL_TAG
  val NULL_TAG: CountryTag = null

class CountryTagRegistry extends Registry[CountryTag] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
