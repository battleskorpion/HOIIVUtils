package com.hoi4utils.hoi4.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.hoi4utils.hoi4.map.gen.AbstractMapGeneration;
import com.hoi4utils.hoi4.map.gen.Heightmap;
import com.hoi4utils.hoi4.map.gen.MapPoint;
import com.hoi4utils.hoi4.map.gen.SeedGenProperties;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * SeedProbabilityMap, designed for GPU efficiency.
 */
public class SeedProbabilityMap_GPU_2 extends AbstractMapGeneration {
    /**
     * [y, x] since the rows are the y values and the columns are the x values, corresponding to the map.
     */
    double[][] seedProbabilityMap;
    double[][] cumulativeProbabilities; // per x, inclusive of previous maps // todo this comment makes no sense
    final Heightmap heightmap;
    final int width;
    final int height;
    double probabilitySum = 0;
    SeedGenProperties properties;

    public SeedProbabilityMap_GPU_2(Heightmap heightmap, SeedGenProperties properties) {
        this.heightmap = heightmap;
        this.width = heightmap.width();
        this.height = heightmap.height();
        seedProbabilityMap = new double[height][width]; // y, x
        cumulativeProbabilities = new double[height][width];
        this.properties = properties;

        initializeProbabilityMap();
    }

    private void initializeProbabilityMap() {
        final double[][] _seedMap = new double[height][width];
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
                    _seedMap[y][x] = type == 0 ? 1.15 : 0.85; // todo very magic numbers :(
                }
            }
        };

        // The group size must always be a ‘factor’ of the global range. So globalRange
        // % groupSize == 0
        // [prime numbers run bad with this impl, but that's fine bc we use specific
        // multiples of 2, 4, 8 for the maps]
        Range range = Range.create2D(width, height);
        kernel.execute(range);
        kernel.dispose();
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
    private double[][] iterativeProbabilisticMap2D(double[][] matrix, double[] randProbabilities) {
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
        final int count = 3;
        final int numProbabilities = randProbabilities.length;
        final double zeroProbability = 0.0;
        final double maxProbability = 1.0;
        final int cReduceIterations = (int) Math.ceil(Math.log(size) / Math.log(2));

        final double[] _data = new double[size];
        final double[] _cProbabilityData = new double[size];

        // Flatten the matrix into a 1D array
        System.arraycopy(Stream.of(matrix)
                        .parallel()
                        .flatMapToDouble(Arrays::stream)
                        .toArray(),
                0, _data, 0, size);
//        // secondary matrices for operations
//        System.arraycopy(Stream.of(matrix)
//                        .parallel()
//                        .flatMapToDouble(Arrays::stream)
//                        .toArray(),
//                0, reducedData, 0, size);

        for (int i = 0; i < numProbabilities; i++) {
            DoubleReduceKernel reduceKernel = new DoubleReduceKernel();
            reduceKernel.reduce(_data);
            reduceKernel.reduce();
            double[] reducedData = reduceKernel.results();
            double probabilitySum = DoubleStream.of(reducedData).sum();
            double sumRecip = 1.0 / probabilitySum;
            DoubleNormalizeKernel normalizeKernel = new DoubleNormalizeKernel();
            normalizeKernel.normalize(_data, sumRecip);
            //        CumulativeReduceKernel cumulativeReduceKernel = new CumulativeReduceKernel();
            //        cumulativeReduceKernel.cumulativeReduce(_data);
            //        double[] cumulativeP = cumulativeReduceKernel.results();
            _cProbabilityData[0] = _data[0];
            for (int ii = 1; ii < size; ii++) {
                _cProbabilityData[ii] = _data[ii];
                _cProbabilityData[ii] += _cProbabilityData[ii - 1];
            }

            // todo this isnt working?
            Kernel pKernel = new Kernel() {
                @Override
                public void run() {
                    int gid = getGlobalId();
                    double prevP = gid == 0 ? 0 : _cProbabilityData[gid - 1];
                    // fix prevP 0. todo. mutliple handlings necessary.
                    if (_cProbabilityData[gid] >= randProbabilities[0] && prevP < randProbabilities[0]) {
                        _data[gid] = zeroProbability;
                    }
                }
            };
            Range range = Range.create(size);
            pKernel.execute(range);
            pKernel.dispose();
        }

//        Kernel ipmKernel = new Kernel() {
//            @Local
//            double[] _sReduceData = new double[range.getLocalSize(0)];   // OpenCL does not support dynamic size.
//            @Local
//            double[] _sSumRecip = { 0.0 };
//
//            @Override
//            public void run() {
//                int gid = getGlobalId();
//                int localId = getLocalId(0);
//                int blockId = getGroupId(0);
//                int localBlockSize = getLocalSize(0);
//                for (int i = 0; i < numProbabilities && i < 1; i++) {
//                    _sReduceData[localId] = _data[gid];
//                    this.localBarrier();
//                    for (int stride = 1; stride < localBlockSize; stride *= 2) {
//                        if (localId % (2 * stride) == 0) {
//                            _sReduceData[localId] += _sReduceData[localId + stride];
//                        }
//                        this.localBarrier();
//                    }
//                    // write result for this block to global memory
//                    if (localId == 0) {
//                        _reducedData[blockId] = _sReduceData[0];
//                    }
//                    this.globalBarrier();
////                    _data[gid] = _sReduceData[localId];   // todo test
//
////                    // After reduction, the total sum for each bin will be in _reducedData.
////                    // Find the final sum of probabilities.
////                    if (gid == 0) {
////                        _probabilitySum[0] = _reducedData[0];
////                        for (int j = 1; j < _reducedData.length; j++) {
////                            _probabilitySum[0] += _reducedData[j];
////                        }
////                        _sumRecip[0] = 1.0 / _probabilitySum[0];
////                    }
////                    // Sync for final probabilitySum value.
////                    this.globalBarrier();
////                    _data[gid] = _sumRecip[0];
//
////                    /*
////                     * Normalize the matrix to a sum of 1.0. That is, the sum of all probabilities in the matrix should be
////                     * maxProbability (1.0).
////                     * The matrix (now, represented by the array _data), can be normalized by dividing each probability by
////                     * the sum of all probabilities. This is equivalent to multiplying by the reciprocal of the probability
////                     * sum, which may be more efficient.
////                     */
////                    // if cell has been selected, maintain probability as minProbability
////                    if (_data[gid] != zeroProbability)
////                        _data[gid] *= _sumRecip[0];
////                    // wait for all data to be normalized.
////                    this.globalBarrier();
//
////                    /*
////                     * Apply cumulative reduce operation to the matrix.
////                     * reduce phase:
////                     * if gid % 2^(index + 1) >= 2^(index)
////                     * add data[gid - ([gid % 2^(index)] + 1)] to data[gid]
////                     * ~12.8m array -> 24 iterations
////                     */
////                    for (int ii = 0; ii < cReduceIterations; ii++) {
////                        int pow_2_i = 1 << ii;
////                        if (gid % (pow_2_i << 1) >= pow_2_i)
////                            _cumulativeP[gid] += _cumulativeP[gid - (gid % pow_2_i + 1)];
////                        else
////                            _cumulativeP[gid] += 0;
////                        // wait for all applicable data to be accumulated in this iteration.
////                        this.localBarrier();
////                    }
////
////                    /*
////                     * Find the first index where the cumulative probability >= p. At this index, set the probability to zeroProbability.
////                     * How to do this in a matrix operation: the exact index where cumulative probability >= p, is precisely the
////                     * index where data[index - 1] < p and data[index] >= p. If index == 0, the previous probability is zero.
////                     */
////                    double prevP = gid == 0 ? 0 : _cumulativeP[gid - 1];
////                    if (_cumulativeP[gid] >= randProbabilities[i] && prevP < randProbabilities[i]) {
////                        _data[gid] = zeroProbability;
////                    }
//                }
//            }
//        };
//        ipmKernel.execute(range);
//        System.out.println("Disposing ipm kernel.");
//        ipmKernel.dispose();
//        System.out.println("Disposed ipm kernel.");
        System.out.println("thread 256 data: " + _data[256]);
        System.out.println("...last thread data: " + _data[size - 1]);
        System.out.println("...last thread cumulativeP: " + _cProbabilityData[size - 1]);
        //System.out.println("Reduced: " + Arrays.toString(reducedData));
        System.out.println("Probability sum: " + probabilitySum);
        //System.out.println("Sum recip: " + sumRecip);

        /*
         * Reconstruct a result matrix
         */
        double[][] result = new double[rows][cols];
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
                if (seedProbabilityMap[y][x] == 1.0) {  // again, todo.
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

    private void setSeedProbabilityMap_YX(int y, int x, double v) {
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

    public Point findCumulativeProbabilityIndex(double p) {
        for (int y = 0; y < cumulativeProbabilities.length; y++) {
            for (int x = 0; x < cumulativeProbabilities[y].length; x++) {
                if (Double.compare(cumulativeProbabilities[y][x], p) >= 0) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }
}