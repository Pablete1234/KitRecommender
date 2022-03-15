package me.pablete1234.kit.aggregator.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ReflectionUtils {

    private static final Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            //noinspection unchecked
            return (T) f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Object obj, Class<?> cl, String field, Object val) {
        try {
            Field f = cl.getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T allocateInstance(Class<T> clazz) {
        try {
            //noinspection unchecked
            return (T) unsafe.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

}
