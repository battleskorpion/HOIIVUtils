package com.hoi4utils.clausewitz.map.province;

import com.hoi4utils.clausewitz.map.ProvinceGenConfig;
import com.hoi4utils.clausewitz.map.gen.AbstractMapGeneration;
import com.hoi4utils.clausewitz.map.gen.Heightmap;
import com.hoi4utils.clausewitz.map.gen.MapPoint;
import com.hoi4utils.clausewitz.map.seed.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProvinceGeneration extends AbstractMapGeneration {
	public BorderMap stateBorderMap; 		// heightmap of preferred borders
	@Getter
    private ProvinceMap provinceMap;
//	private ProvinceMapPointsList points;
	private SeedsSet<MapPoint> seeds;

	private BorderMapping<MapPoint> stateBorderMapping;
	private Heightmap heightmap;
	private SeedGeneration<MapPoint> seedGeneration;
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
		stateBorderMapping = borderMappingFactory();

		/* seeds generation */
		SeedGeneration<MapPoint> seedGeneration = seedGenerationFactory();
		seedGeneration.generate(stateBorderMapping, stateBorderMap);

		ProvinceDetermination<MapPoint> provinceDetermination = provinceDeterminationFactory();
		//executeProvinceDetermination();
		provinceDetermination.generate(stateBorderMapping, stateBorderMap);
	}

	private BorderMapping<MapPoint> borderMappingFactory() {
		if (config.determinationType() == ProvinceDeterminationType.DISTANCE_GPU) {
			return new BorderMapping_GPU<>();
		} else {
			return new BorderMapping_CPU<>();
		}
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

    private @NotNull ProvinceDetermination<MapPoint> provinceDeterminationFactory() {
		ProvinceDetermination<MapPoint> provinceDetermination;
		if (config.determinationType() == ProvinceDeterminationType.DISTANCE_MULTITHREADED) {
			provinceDetermination = new DistanceDetermination_MT<>(heightmap, provinceMap, config);
		} else if (config.determinationType() == ProvinceDeterminationType.DISTANCE_GPU) {
			provinceDetermination = new DistanceDetermination_GPU<>(heightmap, provinceMap, config);
		} else {
			// default for now
			provinceDetermination = new DistanceDetermination_MT<>(heightmap, provinceMap, config);
		}
		return provinceDetermination;
	}

	private @NotNull SeedGeneration<MapPoint> seedGenerationFactory() {
		SeedGeneration<MapPoint> seedGeneration;
		if (config.generationType() == SeedGenType.GRID) {
			seedGeneration = new GridSeedGeneration(config, heightmap);
		} else {
			seedGeneration = new ProbabilisticSeedGeneration(heightmap, config);
		}
		return seedGeneration;
	}
}


