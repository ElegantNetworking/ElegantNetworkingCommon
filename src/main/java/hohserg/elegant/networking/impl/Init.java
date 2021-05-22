package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.utils.PrintUtils;
import hohserg.elegant.networking.utils.ServiceUtils;
import lombok.Value;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.Refs.*;

public class Init {

    public static Config initConfig(File configFolder) {
        File configFile = new File(configFolder, "elegant_networking.cfg");
        Config config = new Config();

        if (configFile.exists())
            loadConfig(configFile, config);
        else
            saveDefaultConfig(configFile, config);

        return config;
    }

    private static void saveDefaultConfig(File configFile, Config config) {
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write("# How many bytes can contains received packet\n");
            fileWriter.write("packetSizeLimit = " + config.packetSizeLimit + "\n");
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(File configFile, Config config) {
        try (
                FileInputStream fileInputStream = new FileInputStream(configFile);
                Scanner s = new Scanner(fileInputStream)
        ) {

            while (s.hasNextLine()) {
                String line = s.nextLine();
                int commentStart = line.indexOf('#');
                String withoutComment = line.substring(0, commentStart == -1 ? line.length() : commentStart);
                if (!withoutComment.isEmpty()) {
                    String[] split = withoutComment.split("=");
                    if (split.length == 2) {
                        String fieldName = split[0].trim();
                        String value = split[1].trim();
                        if (fieldName.equals("packetSizeLimit"))
                            config.packetSizeLimit = Integer.parseInt(value);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Value
    public static class ModInfo {
        String modid;
        File source;
    }

    public static void registerAllPackets(List<ModInfo> mods, Consumer<String> msgPrintln, Consumer<String> errorPrintln, Consumer<String> channelNameConsumer) {
        Set<String> excludedModid = new HashSet<>();
        excludedModid.add("minecraft");
        PrintWriter errorWriter = PrintUtils.getWriterForStringConsumer(errorPrintln);

        Set<String> channelsToRegister = new HashSet<>();
        for (ModInfo mod : mods) {
            if (!excludedModid.contains(mod.modid))
                try {
                    msgPrintln.accept("Started registration of elegant packets for modid " + mod.modid);
                    File source = mod.getSource();

                    List<Class<?>> packets;

                    Predicate<Class<?>> packetInterfaceFilter = cl -> Arrays.stream(cl.getInterfaces())
                            .anyMatch(i -> i.getCanonicalName().equals(ClientToServerPacket_name) || i.getCanonicalName().equals(ServerToClientPacket_name));

                    if (source.isDirectory()) {
                        packets = Stream.concat(
                                ServiceUtils.loadClassesFromFileService(source, getServicePath(ClientToServerPacket_name)),
                                ServiceUtils.loadClassesFromFileService(source, getServicePath(ServerToClientPacket_name))
                        ).filter(packetInterfaceFilter).collect(Collectors.toList());

                        ServiceUtils.loadClassesFromFileService(source, getServicePath(ISerializer_name)).forEachOrdered(cl -> registerSerializer(cl, errorWriter));

                    } else {
                        try (JarFile jar = new JarFile(source)) {
                            packets = Stream.concat(
                                    ServiceUtils.loadClassesFromJarService(jar, getServicePath(ClientToServerPacket_name)),
                                    ServiceUtils.loadClassesFromJarService(jar, getServicePath(ServerToClientPacket_name))
                            ).filter(packetInterfaceFilter).collect(Collectors.toList());


                            ServiceUtils.loadClassesFromJarService(jar, getServicePath(ISerializer_name)).forEachOrdered(cl -> registerSerializer(cl, errorWriter));
                        } catch (IOException e) {
                            e.printStackTrace(errorWriter);
                            packets = new ArrayList<>();
                        }
                    }

                    for (int i = 0; i < packets.size(); i++) {
                        int id = i + 1;
                        Registry.register(new Registry.PacketInfo(mod.getModid(), id, packets.get(i).getCanonicalName()));
                        channelsToRegister.add(mod.getModid());
                        msgPrintln.accept("Registered packet " + packets.get(i).getSimpleName() + " for channel " + mod.getModid() + " with id " + id);
                    }
                } catch (Throwable e) {
                    errorWriter.println("Unable to register elegant packets for mod " + mod.getModid() + ". Caused by:");
                    e.printStackTrace(errorWriter);
                    errorWriter.println();
                }
        }
        channelsToRegister.forEach(channelNameConsumer);
    }

    private static void registerSerializer(Class<?> cl, PrintWriter errorPrintln) {
        Class<? extends IByteBufSerializable> serializable = cl.getAnnotation(SerializerMark.class).packetClass();
        try {
            Registry.registerSerializer(serializable, (ISerializerBase) cl.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            errorPrintln.println("Unable to create serializer for " + serializable);
            e.printStackTrace(errorPrintln);
        }
    }
}
