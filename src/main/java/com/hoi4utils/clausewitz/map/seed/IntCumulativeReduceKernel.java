package com.hoi4utils.clausewitz.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class IntCumulativeReduceKernel extends Kernel {
    private int[] _reduceInput;
    private int[][] _reduceOutput;
    private int _cReduceIterations;

    public void cumulativeReduce(int[] reduceInput) {
        _reduceInput = reduceInput.clone();
        final Range range = Range.create(_reduceInput.length);
        _reduceOutput = new int[range.getNumGroups(0)][range.getLocalSize(0)];
        _cReduceIterations = (int) Math.ceil(Math.log(_reduceInput.length) / Math.log(2));
        this.execute(range);
    }

    public int[][] results() {
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


        _reduceOutput[blockId][localId] = _reduceInput[gid];
        this.localBarrier();

        // Perform prefix sum in shared memory
        // todo would it be faster actually in local shared memory?
        for (int stride = 1; stride < localBlockSize; stride *= 2) {
            int temp = (localId >= stride) ? _reduceOutput[blockId][localId - stride] : 0;
            this.localBarrier(); // all threads have read shared memory
            _reduceOutput[blockId][localId] += temp;
            this.localBarrier(); // all threads updated shared memory
        }

        this.globalBarrier(); // all threads have completed the prefix sum
    }
}
