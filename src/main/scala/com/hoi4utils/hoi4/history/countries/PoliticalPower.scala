package com.hoi4utils.hoi4.history.countries

class PoliticalPower(private var politicalPower: Int) {
  def amt: Int = politicalPower

  def set(politicalPower: Int): Unit = {
    this.politicalPower = politicalPower
  }
}

