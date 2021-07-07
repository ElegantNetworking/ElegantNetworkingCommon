package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ElegantPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;
import hohserg.elegant.networking.utils.PrintUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static void initPackets(Consumer<String> msgPrintln, Consumer<String> errorPrintln, Consumer<String> channelNameConsumer) {
        new Init(msgPrintln, errorPrintln, channelNameConsumer).registerAllPackets();
    }

    private final Consumer<String> msgPrintln;
    private final Consumer<String> errorPrintln;
    private final Consumer<String> channelNameConsumer;
    private final PrintWriter errorWriter;

    private Init(Consumer<String> msgPrintln, Consumer<String> errorPrintln, Consumer<String> channelNameConsumer) {
        this.msgPrintln = msgPrintln;
        this.errorPrintln = errorPrintln;
        this.channelNameConsumer = channelNameConsumer;
        errorWriter = PrintUtils.getWriterForStringConsumer(errorPrintln);
    }

    private void registerAllPackets() {
        safeIterator(ServiceLoader.load(ISerializerBase.class).iterator(), "Trouble while indexing serializers")
                .forEachRemaining(serializer -> Registry.registerSerializer(getPacketClass(serializer), serializer));

        Map<String, List<Class<? extends IByteBufSerializable>>> channelToPackets =
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                safeIterator(
                                        ServiceLoader.load(IPacketProvider.class).iterator(),
                                        "Trouble while indexing elegant packets:"
                                ),
                                Spliterator.ORDERED
                        ),
                        false
                )
                        .flatMap(safeMapper(
                                packetProvider -> Pair.of(getPacketChannel(packetProvider), packetProvider.getPacketClass()),
                                "Trouble while indexing elegant packets:"
                        ))
                        .collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));

        channelToPackets.forEach((channel, packets) -> {
            printStarted(channel);
            try {
                for (int i = 0; i < packets.size(); i++) {
                    Class<? extends IByteBufSerializable> packetClass = packets.get(i);
                    int packetId = i + 1;
                    Registry.register(channel, packetId, packetClass.getCanonicalName());
                    printRegistered(channel, packetClass, packetId);
                }
                channelNameConsumer.accept(channel);
                printSuccessfully(channel);
            } catch (Throwable e) {
                printFailed(channel, e);
            }
        });
    }

    private void printStarted(String channel) {
        msgPrintln.accept("Starting registration of elegant packets for channel " + channel);
    }

    private void printRegistered(String channel, Class<? extends IByteBufSerializable> packetClass, int packetId) {
        msgPrintln.accept("Registered packet " + packetClass.getSimpleName() + " for channel " + channel + " with id " + packetId);
    }

    private void printSuccessfully(String channel) {
        msgPrintln.accept("Successfully registered packets for channel " + channel);
    }

    private void printFailed(String channel, Throwable e) {
        printError(e, "Failed to register packets for channel " + channel + "\nCaused by:");
    }

    private <A> Iterator<A> safeIterator(Iterator<A> iterator, String errorPrefix) {
        List<A> content = new LinkedList<>();
        while (iterator.hasNext()) {
            try {
                content.add(iterator.next());
            } catch (Throwable e) {
                printError(e, errorPrefix);
            }
        }
        return content.iterator();
    }

    private <A, B> Function<A, Stream<B>> safeMapper(Function<A, B> f, String errorPrefix) {
        return a -> {
            try {
                return Stream.of(f.apply(a));
            } catch (Throwable e) {
                printError(e, errorPrefix);
                return Stream.empty();
            }
        };
    }

    private void printError(Throwable e, String errorPrefix) {
        errorPrintln.accept(errorPrefix);
        e.printStackTrace(errorWriter);
        errorWriter.flush();
    }

    private static String getPacketChannel(IPacketProvider packetProvider) {
        String annotatedChannel = Objects.requireNonNull(packetProvider.getPacketClass().getAnnotation(ElegantPacket.class)).channel();
        return annotatedChannel.equals("$modid") ? packetProvider.modid() : annotatedChannel;
    }

    private static Class<? extends IByteBufSerializable> getPacketClass(ISerializerBase serializer) {
        return serializer.getClass().getAnnotation(SerializerMark.class).packetClass();
    }
}
