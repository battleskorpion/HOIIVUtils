package com.hoi4utils.clausewitz.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.hoi4utils.clausewitz.map.SeedGenProperties;
import com.hoi4utils.clausewitz.map.AbstractMapGeneration;
import com.hoi4utils.clausewitz.map.Heightmap;
import com.hoi4utils.clausewitz.map.MapPoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * SeedProbabilityMap, designed for GPU efficiency.
 */
public class SeedProbabilityMap_GPU_3 extends AbstractMapGeneration {
    /**
     * [y, x] since the rows are the y values and the columns are the x values, corresponding to the map.
     */
    int[][] seedProbabilityMap;
    long[][] cumulativeProbabilities;
    final Heightmap heightmap;
    final int width;
    final int height;
    double probabilitySum = 0;
    SeedGenProperties properties;

    public SeedProbabilityMap_GPU_3(Heightmap heightmap, SeedGenProperties properties) {
        this.heightmap = heightmap;
        this.width = heightmap.width();
        this.height = heightmap.height();
        seedProbabilityMap = new int[height][width]; // y, x
        cumulativeProbabilities = new long[height][width];
        this.properties = properties;

        initializeProbabilityMap();
    }

    private void initializeProbabilityMap() {
        final int[][] _seedMap = new int[height][width];
        final byte[][] _heightmap = heightmap.snapshot();
        final int seaLevel = properties.seaLevel(); // necessary as can not use interface related call in kernel
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int x = getGlobalId(0);
                int y = getGlobalId(1);
                if (x < getGlobalSize(0) && y < getGlobalSize(1)) {
                    int xyheight = _heightmap[y][x] & 0xFF;
                    // int type = provinceType(xyheight, seaLevel); // is this bad here? probably.
                    int type = xyheight < seaLevel ? 0 : 1;
                    _seedMap[y][x] = type == 0 ? 115 : 85; // todo very magic numbers :(
                }
            }
        };

        // The group size must always be a ‘factor’ of the global range. So globalRange
        // % groupSize == 0
        // [prime numbers run bad with this impl, but that's fine bc we use specific
        // multiples of 2, 4, 8 for the maps]
        Range range = Range.create2D(width, height, 16, 16); 
        kernel.execute(range);
        kernel.dispose();
        System.out.println("Ran initialize probability map Kernel");
        seedProbabilityMap = _seedMap;

        //normalize(); // initial normalization of probabilities to sum of 1.0    // todo may not be necessary
    }

    /**
     * Iteratively generate a final seed probability map using random probabilities and the current seed probability map.
     *
     * @param matrix            the base seed probability map
     * @param randProbabilities the random probabilities to use
     * @return the final seed probability map
     */
    private int[][] iterativeProbabilisticMap2D(int[][] matrix, double[] randProbabilities) {
        // iteratively find the first index(x, y) in the matrix where cumulativeProbability(x, y) >= p
        // we need as much as possible in *one* kernel
        /* if a cell has been selected (by the probability), it should not be selectable again
           (but this could be handled if necessary for some optimization).
           This cell should be set to have a probability of 0.0 (zeroProbability) to represent being selected.
           This may seem counter-intuitive, but consider it representing not being able to be selected again,
           which in this case only previously selected points can not be selected in the future. Additionally,
           setting the probability to 0.0 instead of maxProbability [1.0], means no special case checks are
           required between iterations to ignore selected points. They are inconsequential algorithmically.
         */
        int rows = matrix.length;
        int cols = matrix[0].length;
        int size = rows * cols;
        final int numProbabilities = randProbabilities.length;
        final int zeroProbability = 0;
//        final double maxProbability = 1.0;
        final int cReduceIterations = (int) Math.ceil(Math.log(size) / Math.log(2));
        int pp = 0;
        final int r = 25;
        double[] distanceModifiers = new double[r + 1]; // 0 to r
        distanceModifiers[0] = 0.0;
        for (int i = 1; i <= r; i++) {
            distanceModifiers[i] = (i - 0.2) / r; // Normalize the value to be between 0 and 1
        }

        final int[] _data = new int[size];

        // Flatten the matrix into a 1D array
        System.arraycopy(Stream.of(matrix)
                        .parallel()
                        .flatMapToInt(Arrays::stream)
                        .toArray(),
                0, _data, 0, size);

        for (int i = 0; i < numProbabilities; i++) {
            IntReduceKernel reduceKernel = new IntReduceKernel();
            reduceKernel.reduce(_data);
            reduceKernel.reduce();
            int[] reducedData = reduceKernel.results();
            reduceKernel.dispose();
            System.out.println("Ran reduceKernel");
            int probabilitySum = IntStream.of(reducedData).sum();
            IntCumulativeReduceKernel cReduceKernel = new IntCumulativeReduceKernel();
            cReduceKernel.cumulativeReduce(_data);    // just once
            int[][] cReducedData = cReduceKernel.results(); // again results are per workgroup
            cReduceKernel.dispose();
            System.out.println("Ran cReduceKernel");

            int p = (int) Math.ceil(randProbabilities[i] * probabilitySum);
            pp = p;

            int[] pointX = { 0 };
            int[] pointY = { 0 };
            Kernel pKernel = new Kernel() {
                @Override
                public void run() {
                    int gid = getGlobalId();
                    int localID = getLocalId();
                    int groupID = getGroupId();
                    int prevWkgsCProbability = 0;    // previous workgroup cumulative property
                    // find previous workgroups' cumulative probability
                    for (int i = 0; i < groupID; i++) {
                        prevWkgsCProbability += cReducedData[i][cReducedData[i].length - 1];
                    }
                    int prevP = ((gid == 0 || localID == 0) ? 0 : cReducedData[groupID][localID - 1]) + prevWkgsCProbability;
                    // todo do i have to fix the calcs?
                    if ((cReducedData[groupID][localID] + prevWkgsCProbability) >= p && prevP < p) {
                        _data[gid] = zeroProbability;
                        pointX[0] = gid % cols;
                        pointY[0] = gid / cols;
                    }
//                    _data[gid] = cReducedData[groupID][localID];
                }
            };
            Range range = Range.create(size, 256);
            range.setLocalIsDerived(true);
            range.setMaxWorkGroupSize(256);
            pKernel.execute(range);
            pKernel.dispose();
            System.out.println("Ran pKernel");

//            int minY = Math.max(pointY[0] - r, 0);
//            int maxY = Math.min(pointY[0] + r, seedProbabilityMap.length - 1);
//            int minX = Math.max(pointX[0] - r, 0);
//            int maxX = Math.min(pointX[0] + r, seedProbabilityMap[0].length - 1);
            Kernel adjKernel = new Kernel() {
                @Override
                public void run() {
                    int gid = getGlobalId();
                    int localID = getLocalId();
                    int groupID = getGroupId();
                    int x = gid % cols - pointX[0];
                    int y = gid / cols - pointY[0];
                    double distance = Math.sqrt(x * x + y * y);
                    if (distance <= r) {
                        _data[gid] = (int) Math.ceil(_data[gid] * distanceModifiers[(int) distance]);
                    }
                }
            };
            adjKernel.execute(range);
            adjKernel.dispose();
            System.out.println("Ran adjKernel");
        }

        System.out.println("thread 256 data: " + _data[256]);
        System.out.println("probability: " + pp);
        System.out.println("...last thread data: " + _data[size - 1]);
//        System.out.println("...last thread cumulativeP: " + _cProbabilityData[size - 1]);
        System.out.println("Probability sum: " + probabilitySum);

        /*
         * Reconstruct a result matrix
         */
        int[][] result = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(_data, i * cols, result[i], 0, cols);
        }

        System.out.println("Finished calculating iterative probabilitistic map.");
        return result;
    }

    public MapPoint[] getPoints(Random random, int numPoints) {
        MapPoint[] points = new MapPoint[numPoints];
        // generate numPoints random numbers
        double[] pList = nRandomDoubles(numPoints, random);
        // combine mapReduce2D, multiplyAll, and/or cumulativeReduce2D. We want to iteratively find the first index where
        // cumulative probability >= p, but without having to re-load the kernel/gpu memory each time for each matrix operation.
        seedProbabilityMap = iterativeProbabilisticMap2D(seedProbabilityMap, pList);
        // todo can be optimized:
        int mpCounter = 0;
        for (int x = 0; x < width; x++) {
//            System.out.println("x: " + x);
            for (int y = 0; y < height && mpCounter < numPoints; y++) {
                if (seedProbabilityMap[y][x] == 0) {  // again, todo.
                    MapPoint mp = getMapPoint(new Point(x, y));
                    points[mpCounter++] = mp;
                    System.out.println("added mapPoint of x: " + x + ", y: " + y);
                    //adjustProbabilitiesInRadius(mp, 9);
                }
            }
        }

        return points;
    }

    private double[] nRandomDoubles(int n, Random random) {
        double[] pList = new double[n];
        for (int i = 0; i < n; i++) {
            pList[i] = random.nextDouble();
        }
        return pList;
    }

    private @NotNull MapPoint getMapPoint(Point cumulativeP) {
        return new MapPoint(cumulativeP.x, cumulativeP.y,
                provinceType(heightmap.height_xy(cumulativeP.x, cumulativeP.y),
                        properties.seaLevel()));
    }

    private void setSeedProbabilityMap_YX(int y, int x, int v) {
        // todo optimize?
        System.out.println("psum " + probabilitySum);
        probabilitySum -= seedProbabilityMap[y][x];
        seedProbabilityMap[y][x] = v;
        probabilitySum += v;
        System.out.println("psum " + probabilitySum);
    }

    private void adjustProbabilitiesInRadius(MapPoint mp, int r) {
        // todo parallelize
        int minY = Math.max(mp.y - r, 0);
        int maxY = Math.min(mp.y + r, seedProbabilityMap.length - 1);
        int minX = Math.max(mp.x - r, 0);
        int maxX = Math.min(mp.x + r, seedProbabilityMap[0].length - 1);
        double[] distanceModifiers = new double[r + 1]; // 0 to r
        distanceModifiers[0] = 0.0;
        for (int i = 1; i <= r; i++) {
            distanceModifiers[i] = (i - 0.2) / r; // Normalize the value to be between 0 and 1
        }

        // int rSquared = r * r;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double distance = mp.distance(x, y);
                // Check if the distance is within the specified radius
                if (distance <= r) {
                    // Adjust the probability at position (y, x)
                    // todo optimize??
                    setSeedProbabilityMap_YX(y, x, seedProbabilityMap[y][x] *= distanceModifiers[(int) distance]);
                }
            }
        }
    }

//    public Point findCumulativeProbabilityIndex(double p) {
//        for (int y = 0; y < cumulativeProbabilities.length; y++) {
//            for (int x = 0; x < cumulativeProbabilities[y].length; x++) {
//                if (Double.compare(cumulativeProbabilities[y][x], p) >= 0) {
//                    return new Point(x, y);
//                }
//            }
//        }
//        return null;
//    }
}