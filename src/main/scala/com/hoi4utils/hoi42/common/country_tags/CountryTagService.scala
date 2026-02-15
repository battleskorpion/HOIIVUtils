package com.hoi4utils.hoi42.common.country_tags

import com.hoi4utils.script2.{PDXDecoder, Registry}

class CountryTagService extends Registry[CountryTag] {
//  def allTags: UIO[List[CountryTag]]

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
