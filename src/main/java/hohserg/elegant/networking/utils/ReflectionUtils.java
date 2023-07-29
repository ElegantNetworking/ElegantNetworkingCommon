package hohserg.elegant.networking.utils;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
    public static <A> A create(String className) {
        try {
            return ((A) Class.forName(className).getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
