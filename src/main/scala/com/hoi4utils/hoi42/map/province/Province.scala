package com.hoi4utils.hoi42.map.province

import com.hoi4utils.script2.{IDReferable, NameReferable, PDXDecoder, PDXInlineEntity, PDXInlineEntityDynamicKey, Registry}

class Province(pdxKey: String) extends PDXInlineEntity[Int](pdxKey) with NameReferable[Int] {

}
// todo force accept (var provinces: ProvinceRegistry) ... with Registrymember[Province](provinces)
//// todo testing this instead:
//class Province extends PDXInlineEntityDynamicKey[Int] with NameReferable[Int] {
//
//}

object Province {}

class ProvinceRegistry extends Registry[Province] {

  override def idDecoder: PDXDecoder[Int] = summon[PDXDecoder[Int]]
}

