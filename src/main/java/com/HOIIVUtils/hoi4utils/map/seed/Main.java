package com.HOIIVUtils.hoi4utils.map.seed;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int size = 1048576;
        final int count = 3;
        final double[] V = new double[size];

        // lets fill in V randomly...
        for (int i = 0; i < size; i++) {
            // random number between 0.0 (inclusive) and 1.0 (exclusive)
            V[i] = Math.random();
        }

        // this will hold our values between the phases.
        double[][] totals = new double[count][size];

        ///////////////
        // MAP PHASE //
        ///////////////
        final double[][] kernelTotals = totals;
        Kernel mapKernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                double value = V[gid];
                for (int index = 0; index < count; index++) {
                    if (value >= (double) index / count && value < (double) (index + 1) / count)
                        kernelTotals[index][gid] = 1.0;
                }
            }
        };
        mapKernel.execute(Range.create(size));
        mapKernel.dispose();
        totals = kernelTotals;
        //System.out.println(Arrays.deepToString(totals));

        //////////////////
        // REDUCE PHASE //
        //////////////////
        while (size > 1) {
            int nextSize = size / 2;
            final double[][] currentTotals = totals;
            final double[][] nextTotals = new double[count][nextSize];
            Kernel reduceKernel = new Kernel() {
                @Override
                public void run() {
                    int gid = getGlobalId();
                    for (int index = 0; index < count; index++) {
                        nextTotals[index][gid] = currentTotals[index][gid * 2] + currentTotals[index][gid * 2 + 1];
                    }
                }
            };
            reduceKernel.execute(Range.create(nextSize));
            reduceKernel.dispose();

            totals = nextTotals;
            size = nextSize;
        }
        assert size == 1;

        /////////////////////////////
        // Done, just print it out //
        /////////////////////////////
        double[] results = new double[count];
        for (int index = 0; index < count; index++) {
            results[index] = totals[index][0];
        }

        System.out.println(Arrays.toString(results));
    }
}
