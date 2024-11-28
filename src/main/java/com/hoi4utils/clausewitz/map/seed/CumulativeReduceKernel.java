package com.hoi4utils.clausewitz.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class CumulativeReduceKernel extends Kernel {
    private double[] _reduceInput;
    private double[][] _reduceOutput;
    private int _cReduceIterations;

    public void cumulativeReduce(double[] reduceInput) {
        _reduceInput = reduceInput.clone();
        final Range range = Range.create(_reduceInput.length);
        _reduceOutput = new double[range.getNumGroups(0)][range.getLocalSize(0)];
        _cReduceIterations = (int) Math.ceil(Math.log(_reduceInput.length) / Math.log(2));
        execute(range, 2);
    }

    public double[][] results() {
        return _reduceOutput;
    }

    @Override
    public void run() {
        /*
         * Apply cumulative reduce operation to the matrix.
         * reduce phase:
         * if gid % 2^(index + 1) >= 2^(index)
         * add data[gid - ([gid % 2^(index)] + 1)] to data[gid]
         * ~12.8m array -> 24 iterations
         */
        int gid = getGlobalId();
        int localId = getLocalId();
        int blockId = getGroupId();
        int localBlockSize = getLocalSize();
        int pass = getPassId();

        if (pass == 0 || pass == 2) {
            if (pass == 0) _reduceOutput[blockId][localId] = _reduceInput[gid];
            this.localBarrier();

            // Perform prefix sum in shared memory
            for (int stride = 1; stride < localBlockSize; stride *= 2) {
                double temp = (localId >= stride) ? _reduceOutput[blockId][localId - stride] : 0.0;
                this.localBarrier(); // all threads have read shared memory
                _reduceOutput[blockId][localId] += temp;
                this.localBarrier(); // all threads updated shared memory
            }

            this.globalBarrier(); // all threads have completed the prefix sum
        } else if (pass == 1) {
            for (int i = 0; i < _reduceOutput.length; i++) {
                if (i < blockId) {
                    // add last element sum
                    _reduceOutput[blockId][localId] += _reduceOutput[i][_reduceOutput[i].length - 1];
                }
            }
        }
    }
}
