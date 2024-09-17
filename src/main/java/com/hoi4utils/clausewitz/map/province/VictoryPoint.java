package com.hoi4utils.clausewitz.map.province;

import com.hoi4utils.clausewitz.localization.*;
import org.jetbrains.annotations.NotNull;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * localizable: victory point name
 */
public class VictoryPoint implements Localizable {
	static List<VictoryPoint> victoryPoints = new ArrayList<>();
	int value;
	Province province;

	protected VictoryPoint(int province, int value) {
		this.province = Province.of(province);
		this.value = value;
	}

	public static VictoryPoint of(int province, int value) {
		for (VictoryPoint vp : victoryPoints) {
			if (vp.province.id() == province) {
				return vp;
			}
		}
		return new VictoryPoint(province, value);
	}

	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public @NotNull scala.collection.mutable.Map<Property, String> getLocalizableProperties() {
		return CollectionConverters.asScala(Map.of(Property.NAME, "VP_NAME"));
	}

	@Override
	public @NotNull scala.collection.Iterable<? extends Localizable> getLocalizableGroup() {
		return CollectionConverters.asScala(victoryPoints);
	}
}
