package hohserg.elegant.networking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.util.stream.Collectors.toList;

public class ServiceUtils {

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
        ZipEntry entry = jar.getEntry(path);
        if (entry != null) {
            try (InputStream inputStream = jar.getInputStream(entry)) {
                return loadClassesFromService(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Stream.empty();
    }

    public static Stream<? extends Class<?>> loadClassesFromFileService(File modLocation, String path) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(modLocation, path))) {
            return loadClassesFromService(fileInputStream);
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Stream.empty();
    }

    public static List<String> loadClassNamesFromService(InputStream inputStream) {
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

    public static Stream<? extends Class<?>> loadClassesFromService(InputStream inputStream) {
        return loadClassNamesFromService(inputStream)
                .stream()
                .flatMap(className -> {
                    try {
                        return Stream.of(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        return Stream.empty();
                    }
                });
    }
}
