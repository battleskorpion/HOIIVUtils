package com.hoi4utils.hoi4.map.province;

import com.hoi4utils.hoi4.map.gen.*;
import com.hoi4utils.hoi4.map.seed.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProvinceGeneration extends AbstractMapGeneration {
	public BorderMap stateBorderMap; 		// heightmap of preferred borders
	public String defaultStateBoarderMap = "map/state_borders_none.bmp";

	private ProvinceMap provinceMap;
//	private ProvinceMapPointsList points;
	private SeedsSet<MapPoint> seeds;

	private BorderMapping<MapPoint> stateBorderMapping;
	private Heightmap heightmap;
	private SeedGeneration<MapPoint> seedGeneration;
	/** threadLimit = 0: max (use all processors/threads). */
	private ProvinceGenConfig config;

	private ProvinceGeneration() {
		this.config = new ProvinceGenConfig(95, 4608, 2816, 0);
	}

	public ProvinceGeneration(ProvinceGenConfig config) {
		this.config = config;
	}

	/**
	 * Main method for province generation.
	 * TODO: what is this for?
	 * @param args
	 */
	public static void main(String[] args) {		
		ProvinceGeneration provinceGeneration = new ProvinceGeneration();
		
		provinceGeneration.generate(Heightmap.DEFAULT);

		provinceGeneration.writeProvinceMap();
	}

	/**
	 * Main method for province generation.
	 */
	private void generate() {
		/* create new image (map) */
		provinceMap = new ProvinceMap(heightmap);
		
		stateBorderMap = loadStateBorderMap(defaultStateBoarderMap);
		
		/* initialize mapping of seeds to states (regions for purposes of province generation) */
		// TODO: optimization may be possible
		stateBorderMapping = borderMappingFactory();

		/* seeds generation */
		SeedGeneration<MapPoint> seedGeneration = seedGenerationFactory();
		seedGeneration.generate(stateBorderMapping, stateBorderMap);

		ProvinceDetermination<MapPoint> provinceDetermination = provinceDeterminationFactory();
		provinceDetermination.generate(stateBorderMapping, stateBorderMap);
	}

	public void generate(Heightmap heightmap) {
		this.heightmap = heightmap;
		generate();
	}

	public void generate(String heightmapName) {
		heightmap = loadHeightmap(heightmapName);
		generate();
	}

	public void writeProvinceMap() {
		provinceMap.write();
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
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(stateBorderMapName)) {
			if (inputStream == null) {
				throw new RuntimeException("Resource not found: " + stateBorderMapName);
			}
			return stateBorderMap = new BorderMap(ImageIO.read(inputStream));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load state border map", e);
		}
	}

	private BorderMapping<MapPoint> borderMappingFactory() {
		return switch (config.determinationType()) {
			case DISTANCE_MULTITHREADED -> new BorderMapping_CPU<>();
			case DISTANCE_GPU -> new BorderMapping_GPU<>();
			case DISTANCE -> new BorderMapping_CPU<>();
		};
	}

    private @NotNull ProvinceDetermination<MapPoint> provinceDeterminationFactory() {
		return switch (config.determinationType()) {
            case DISTANCE_MULTITHREADED -> new DistanceDetermination_MT<>(heightmap, provinceMap, config);
            case DISTANCE_GPU -> new DistanceDetermination_GPU<>(heightmap, provinceMap, config);
			case DISTANCE -> new DistanceDetermination_MT<>(heightmap, provinceMap, config);
        };
	}

	private @NotNull SeedGeneration<MapPoint> seedGenerationFactory() {
		return switch (config.generationType()) {
			case GRID -> new GridSeedGeneration(config, heightmap);
			case HEX_GRID -> new HexGridSeedGeneration(config, heightmap); 
			case PROBABILISTIC_GPU -> new ProbabilisticSeedGeneration(heightmap, config);
			case RANDOM -> new RandomSeedGeneration(config, heightmap);
		};
	}

	public ProvinceMap getProvinceMap() {
		return provinceMap;
	}
}


