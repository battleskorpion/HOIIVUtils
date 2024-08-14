package com.hoi4utils.clausewitz.map.province;

import com.hoi4utils.clausewitz.map.ProvinceGenConfig;
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
	ProvinceGenConfig config;

	private ProvinceGeneration() {
		this.config = new ProvinceGenConfig(95, 4608, 2816, 0);
	}

	public ProvinceGeneration(ProvinceGenConfig properties) {
		this.config = properties;
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
		if (config.determinationType() == ProvinceDeterminationType.DISTANCE_GPU) {
			stateMapList = new BorderMapping_GPU<>();
		} else {
			stateMapList = new BorderMapping_CPU<>();
		}

		/* seeds generation */
		SeedGeneration<MapPoint> seedGeneration;
		if (config.generationType() == SeedGenType.GRID) {
			seedGeneration = new GridSeedGeneration(config, heightmap);
		} else {
			seedGeneration = new ProbabilisticSeedGeneration(heightmap, config);
		}
		seedGeneration.generate(stateMapList, stateBorderMap);

		ProvinceDetermination<MapPoint> provinceDetermination;
		if (config.determinationType() == ProvinceDeterminationType.DISTANCE_MULTITHREADED) {
			provinceDetermination = new DistanceDetermination_MT<>(heightmap, provinceMap, config);
		} else if (config.determinationType() == ProvinceDeterminationType.DISTANCE_GPU) {
			provinceDetermination = new DistanceDetermination_GPU<>(heightmap, provinceMap, config);
		} else {
			// default for now
			provinceDetermination = new DistanceDetermination_MT<>(heightmap, provinceMap, config);
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


