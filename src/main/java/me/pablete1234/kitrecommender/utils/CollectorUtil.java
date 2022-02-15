package me.pablete1234.kitrecommender.utils;

import com.google.common.collect.ImmutableSet;

import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public class CollectorUtil {

    public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
        return Collector.of(
                ImmutableSet::builder,
                ImmutableSet.Builder::add,
                (builder1, builder2) -> builder1.addAll(builder2.build()),
                ImmutableSet.Builder::build,
                UNORDERED, CONCURRENT);
    }


}
