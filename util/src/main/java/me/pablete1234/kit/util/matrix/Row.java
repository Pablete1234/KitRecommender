package me.pablete1234.kit.util.matrix;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import org.jetbrains.annotations.NotNull;

import java.util.PrimitiveIterator;

public class Row implements Iterable<Double> {
    private static final int ROW_LEN = 9;

    private final double[] data = new double[ROW_LEN];
    private double max = 0;
    private int maxIdx;
    private double total;

    public void set(int i, double value) {
        double old = this.data[i];
        this.total = this.total - old + value;
        this.data[i] = value;

        // If max is calculated
        if (!Double.isNaN(this.max)) {
            if (value > this.max) { // New max! update accordingly
                this.max = value;
                this.maxIdx = i;
            } else if (this.max == old) { // Max is gone, reset
                this.max = Double.NaN;
                this.maxIdx = -1;
            }
        }
    }

    public void add(int i, double value) {
        set(i, data[i] + value);
    }

    public double get(int i) {
        return data[i];
    }

    public double total() {
        return total;
    }

    public double max() {
        if (Double.isNaN(this.max)) computeMax();
        return this.max;
    }

    public int maxIdx() {
        if (maxIdx == -1) computeMax();
        return maxIdx;
    }

    public int size() {
        return ROW_LEN;
    }

    private void computeMax() {
        this.max = data[0];
        this.maxIdx = 0;
        for (int i = 1; i < ROW_LEN; i++) {
            if (data[i] <= max) continue;
            this.max = this.data[i];
            this.maxIdx = i;
        }
    }

    public void plusChances(Row other) {
        for (int i = 0; i < ROW_LEN; i++)
            this.data[i] += (other.data[i] / other.total);
        total += 1; // In total, all chances should sum to 1
        max = Double.NaN;
        maxIdx = -1;
    }

    @NotNull
    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return new DoubleIterator() {
            int cursor = 0;

            @Override
            public double nextDouble() {
                return data[cursor++];
            }

            @Override
            public boolean hasNext() {
                return cursor < ROW_LEN;
            }
        };
    }
}
