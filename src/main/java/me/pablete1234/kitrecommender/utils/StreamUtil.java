package me.pablete1234.kitrecommender.utils;

import java.util.stream.Stream;

public class StreamUtil {

    public static <T> Stream<T> prepend(T o1, Stream<T> stream) {
        return Stream.concat(Stream.of(o1), stream);
    }

    public static <T> Stream<T> prepend(T o1, T o2, Stream<T> stream) {
        return Stream.concat(Stream.of(o1, o2), stream);
    }

    public static <T> Stream<T> prepend(T o1, T o2, T o3, Stream<T> stream) {
        return Stream.concat(Stream.of(o1, o2, o3), stream);
    }

    public static <T> Stream<T> prepend(T o1, T o2, T o3, T o4, Stream<T> stream) {
        return Stream.concat(Stream.of(o1, o2, o3, o4), stream);
    }

    @SafeVarargs
    public static <T> Stream<T> append(Stream<T> stream, T... obj) {
        return Stream.concat(stream, Stream.of(obj));
    }

}
