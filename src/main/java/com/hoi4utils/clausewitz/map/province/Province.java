package com.hoi4utils.clausewitz.map.province;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Province implements Iterable<Province> {
	static final List<Province> provinces = new ArrayList<>();
	private final int _id;

	protected Province(int provinceid) throws IllegalArgumentException {
		if (provinceid < 0) {
			throw new IllegalArgumentException("Province id must be non-negative");
		}
		this._id = provinceid;
	}

	public static Province of(int provinceid) throws IllegalArgumentException {
		for (Province province : provinces) {
			if (province._id == provinceid) {
				return province;
			}
		}
		return new Province(provinceid);
	}

	@Override
	public @NotNull Iterator<Province> iterator() {
		return provinces.iterator();
	}

	@Override
	public void forEach(Consumer<? super Province> action) {
		provinces.forEach(action);
	}

	@Override
	public Spliterator<Province> spliterator() {
		return provinces.spliterator();
	}

	public static List<Province> list() {
		return new ArrayList<>(provinces);
	}

	public int id() {
		return _id;
	}

	public String idStr() { return Integer.toString(_id); }
}
