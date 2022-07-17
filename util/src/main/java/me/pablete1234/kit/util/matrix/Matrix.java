package me.pablete1234.kit.util.matrix;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class Matrix implements Iterable<Row> {
    public final int ROWS = 10;

    private final Row[] data = new Row[ROWS];

    public Matrix() {
        for (int i = 0; i < ROWS; i++) {
            data[i] = new Row();
        }
    }

    public Row getRow(int rowIdx) {
        if (rowIdx >= ROWS) rowIdx = ROWS - 1;
        return data[rowIdx];
    }

    public int size() {
        return ROWS;
    }

    @NotNull
    @Override
    public Iterator<Row> iterator() {
        return Iterators.forArray(data);
    }

}
