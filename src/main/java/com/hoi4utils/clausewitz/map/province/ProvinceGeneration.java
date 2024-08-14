package com.hoi4utils.clausewitz.map.province;

import com.hoi4utils.clausewitz.map.ProvinceGenProperties;
import com.hoi4utils.clausewitz.map.gen.AbstractMapGeneration;
import com.hoi4utils.clausewitz.map.gen.Heightmap;
import com.hoi4utils.clausewitz.map.gen.MapPoint;
import com.hoi4utils.clausewitz.map.seed.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProvinceGeneration extends AbstractMapGeneration {
	public BorderMap stateBorderMap; 		// heightmap of preferred borders
	private ProvinceMap provinceMap;
//	private ProvinceMapPointsList points;
	private SeedsSet<MapPoint> seeds;

	private BorderMapping<MapPoint> stateMapList;
	private Heightmap heightmap;
	private SeedGeneration seedGeneration;
	/** threadLimit = 0: max (use all processors/threads). */
	private int threadLimit = 0;        // 0 for no limit
	ProvinceGenProperties properties;

	private ProvinceGeneration() {
		this.properties = new ProvinceGenProperties(95, 4608, 2816);
	}

	public ProvinceGeneration(ProvinceGenProperties properties) {
		this.properties = properties;
	}

	public static void main(String[] args) {
		ProvinceGeneration provinceGeneration = new ProvinceGeneration();
		provinceGeneration.generate("src\\main\\resources\\map\\heightmap.bmp");

		provinceGeneration.writeProvinceMap();
	}

	public void writeProvinceMap() {
		provinceMap.write();
	}

	private void generate() {
		/* create new image (map) */
		provinceMap = new ProvinceMap(heightmap);
		// todo still temp!!!
		stateBorderMap = loadStateBorderMap("src\\main\\resources\\map\\state_borders_none.bmp"); // ! todo temp!!
		/* initialize mapping of seeds to states (regions for purposes of province generation) */
		// TODO: optimization may be possible
		if (properties.determinationType() == ProvinceDeterminationType.DISTANCE_GPU) {
			stateMapList = new BorderMapping_GPU<>();
		} else {
			stateMapList = new BorderMapping_CPU<>();
		}

		/* seeds generation */
		SeedGeneration<MapPoint> seedGeneration;
		if (properties.generationType() == SeedGenType.GRID) {
			seedGeneration = new GridSeedGeneration(properties, heightmap);
		} else {
			seedGeneration = new ProbabilisticSeedGeneration(heightmap, properties);
		}
		seedGeneration.generate(stateMapList, stateBorderMap);

		ProvinceDetermination<MapPoint> provinceDetermination;
		if (properties.determinationType() == ProvinceDeterminationType.DISTANCE_MULTITHREADED) {
			provinceDetermination = new DistanceDetermination_MT<>(heightmap, provinceMap, properties, threadLimit);
		} else {
			provinceDetermination = new DistanceDetermination_GPU<>(heightmap, provinceMap, properties);
		}
		//executeProvinceDetermination();
		provinceDetermination.generate(stateMapList, stateBorderMap);
	}

	public void generate(Heightmap heightmap) {
		this.heightmap = heightmap;
		generate();
	}

	public void generate(String heightmapName) {
		heightmap = loadHeightmap(heightmapName);
		generate();
	}

	/**
	 * values - load heightmap, states map
	 */
	private Heightmap loadHeightmap(String heightmapName) {
		try {
			BufferedImage temp = ImageIO.read(new File(heightmapName));
			return new Heightmap(temp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private BorderMap loadStateBorderMap(String stateBorderMapName) {
		try {
			return stateBorderMap = new BorderMap(ImageIO.read(new File(stateBorderMapName)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ProvinceMap getProvinceMap() {
		return provinceMap;
	}
}


