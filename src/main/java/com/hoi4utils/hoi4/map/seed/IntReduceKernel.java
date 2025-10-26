package com.hoi4utils.hoi4.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class IntReduceKernel extends Kernel {
    @Local
    int[] _sReduceData;

    private int[] _reduceInput;
    private int[] _reduceOutput;

    public void reduce(int[] reduceInput) {
        _reduceInput = reduceInput.clone();
        final Range range = Range.create(_reduceInput.length, 256);
        _reduceOutput = new int[range.getNumGroups(0)];
        _sReduceData = new int[range.getLocalSize(0)];
        execute(range);
    }

    public void reduce() {
        _reduceInput = _reduceOutput;
        final Range range = Range.create(_reduceInput.length, 256);
        _reduceOutput = new int[range.getNumGroups(0)];
        _sReduceData = new int[range.getLocalSize(0)];
        execute(range);
    }

    public int[] results() {
        return _reduceOutput;
    }

    @Override
    public void run() {
        int gid = getGlobalId();
        int localId = getLocalId();
        int blockId = getGroupId();
        int localBlockSize = getLocalSize();

        _sReduceData[localId] = _reduceInput[gid];
        this.localBarrier();
        for (int stride = 1; stride < localBlockSize; stride *= 2) {
            if (localId % (2 * stride) == 0) {
                _sReduceData[localId] += _sReduceData[localId + stride];
            }
            this.localBarrier();
        }
        // write result for this block to global memory
        if (localId == 0) {
            _reduceOutput[blockId] = _sReduceData[0];
        }
        this.globalBarrier();
    }
}