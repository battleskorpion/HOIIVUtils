package com.hoi4utils.hoi4.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class DoubleReduceKernel extends Kernel {
    @Local
    double[] _sReduceData;

    private double[] _reduceInput;
    private double[] _reduceOutput;

    public void reduce(double[] reduceInput) {
        _reduceInput = reduceInput.clone();
        final Range range = Range.create(_reduceInput.length);
        _reduceOutput = new double[range.getNumGroups(0)];
        _sReduceData = new double[range.getLocalSize(0)];
        execute(range);
    }

    public void reduce() {
        _reduceInput = _reduceOutput;
        final Range range = Range.create(_reduceInput.length);
        _reduceOutput = new double[range.getNumGroups(0)];
        _sReduceData = new double[range.getLocalSize(0)];
        execute(range);
    }

    public double[] results() {
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
