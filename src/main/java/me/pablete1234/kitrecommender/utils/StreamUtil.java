package me.pablete1234.kitrecommender.utils;

import java.util.stream.Stream;

public class StreamUtil {

    public static <T> Stream<T> prepend(T obj, Stream<T> stream) {
        return Stream.concat(Stream.of(obj), stream);
    }

    @SafeVarargs
    public static <T> Stream<T> append(Stream<T> stream, T... obj) {
        return Stream.concat(stream, Stream.of(obj));
    }

}
