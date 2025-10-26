package com.hoi4utils.hoi4.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class DoubleNormalizeKernel extends Kernel {

    private double[] _data;
    private double _dataSumReciprocal;

    public void normalize(double[] data, double dataSumReciprocal) {
        final Range range = Range.create(data.length);
        _data = data;
        _dataSumReciprocal = dataSumReciprocal;
        execute(range);
    }

    @Override
    public void run() {
        int gid = getGlobalId();
        _data[gid] *= _dataSumReciprocal;
    }
}
