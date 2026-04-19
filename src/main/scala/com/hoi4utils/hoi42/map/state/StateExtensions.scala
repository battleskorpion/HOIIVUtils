package com.hoi4utils.hoi42.map.state

object StateExtensions {
  extension (state: State):
    def civMilFactoryRatio: Double =
      if state.civilianFactories == 0 then 0.0
      else state.militaryFactories.toDouble / state.civilianFactories

    def populationFactoryRatio: Double =
      if state.civilianFactories == 0 then 0.0
      else state.population.toDouble / state.civilianFactories

    def populationCivFactoryRatio: Double =
      if state.civilianFactories == 0 then 0.0
      else state.population.toDouble / state.civilianFactories

    def populationMilFactoryRatio: Double =
      if state.militaryFactories == 0 then 0.0
      else state.population.toDouble / state.militaryFactories

    def populationAirCapacityRatio: Double =
      if state.airfields == 0 then 0.0
      else state.population.toDouble / state.airfields

    def infrastructureMaxLevel: Int =
      if state.buildingsMaxLevelFactor.getOrElse(0) == 0 then 0
      else (state.infrastructure * state.buildingsMaxLevelFactor.getOrElse(0)).toInt
