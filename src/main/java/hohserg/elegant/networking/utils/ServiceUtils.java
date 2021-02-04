package hohserg.elegant.networking.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static hohserg.elegant.networking.Refs.getServicePath;
import static java.util.stream.Collectors.toList;

public class ServiceUtils {

    public static List<String> readService(InputStream inputStream) {
        List<String> r = new ArrayList<>();
        try (Scanner s = new Scanner(inputStream)/*.useDelimiter("\\A")*/) {
            while (s.hasNextLine())
                r.add(s.nextLine());

            return r
                    .stream()
                    .map(ServiceUtils::removeComment)
                    .map(String::trim)
                    .filter(ServiceUtils::isClassName)
                    .collect(toList());
        }
    }

    private static String removeComment(String line) {
        int commentStart = line.indexOf('#');
        if (commentStart >= 0)
            return line.substring(0, commentStart);
        else
            return line;
    }

    private static boolean isClassName(String line) {
        return line.matches("[A-z][A-z.0-9]+");
    }

    public static Stream<? extends Class<?>> loadClassesFromJarService(JarFile jar, String path) {
        ZipEntry entry = jar.getEntry(getServicePath(path));
        if (entry != null) {
            try (InputStream inputStream = jar.getInputStream(entry)) {
                return loadClassesFromService(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Stream.empty();
    }

    public static Stream<? extends Class<?>> loadClassesFromFileService(File modLocation, String path) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(modLocation, path))) {
            return loadClassesFromService(fileInputStream);
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Stream.empty();
    }

    public static Stream<? extends Class<?>> loadClassesFromService(InputStream inputStream) {
        return readService(inputStream)
                .stream()
                .flatMap(className -> {
                    try {
                        return Stream.of(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class from service not found: " + className, e);
                    }
                });
    }
}
