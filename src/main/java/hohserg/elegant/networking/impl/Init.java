package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.utils.ServiceUtils;
import lombok.Value;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.Refs.*;

public class Init {

    @Value
    public static class ModInfo {
        String modid;
        File source;
    }

    public static void registerAllPackets(List<ModInfo> mods, Consumer<String> errorPrintln, Consumer<String> channelNameConsumer) {
        Set<String> channelsToRegister = new HashSet<>();
        for (ModInfo mod : mods) {
            File source = mod.getSource();

            List<Class<?>> packets;

            Predicate<Class<?>> packetInterfaceFilter = cl -> Arrays.stream(cl.getInterfaces())
                    .anyMatch(i -> i.getCanonicalName().equals(ClientToServerPacket_name) || i.getCanonicalName().equals(ServerToClientPacket_name));

            if (source.isDirectory()) {
                packets = Stream.concat(
                        ServiceUtils.loadClassesFromFileService(source, getServicePath(ClientToServerPacket_name)),
                        ServiceUtils.loadClassesFromFileService(source, getServicePath(ServerToClientPacket_name))
                ).filter(packetInterfaceFilter).collect(Collectors.toList());

                ServiceUtils.loadClassesFromFileService(source, getServicePath(ISerializer_name)).forEachOrdered(cl -> registerSerializer(cl, errorPrintln));

            } else {
                try (JarFile jar = new JarFile(source)) {
                    packets = Stream.concat(
                            ServiceUtils.loadClassesFromJarService(jar, getServicePath(ClientToServerPacket_name)),
                            ServiceUtils.loadClassesFromJarService(jar, getServicePath(ServerToClientPacket_name))
                    ).filter(packetInterfaceFilter).collect(Collectors.toList());


                    ServiceUtils.loadClassesFromJarService(jar, getServicePath(ISerializer_name)).forEachOrdered(cl -> registerSerializer(cl, errorPrintln));
                } catch (IOException e) {
                    e.printStackTrace();
                    packets = new ArrayList<>();
                }
            }

            for (int i = 0; i < packets.size(); i++) {
                Registry.register(new Registry.PacketInfo(mod.getModid(), i + 1, packets.get(i).getCanonicalName()));
                channelsToRegister.add(mod.getModid());
            }
        }
        channelsToRegister.forEach(channelNameConsumer);
    }

    private static void registerSerializer(Class<?> cl, Consumer<String> errorPrintln) {
        Class<? extends IByteBufSerializable> serializable = cl.getAnnotation(SerializerMark.class).packetClass();
        try {
            Registry.registerSerializer(serializable, (ISerializerBase) cl.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            errorPrintln.accept("Unable to create serializer for " + serializable);
            e.printStackTrace();
        }
    }
}
