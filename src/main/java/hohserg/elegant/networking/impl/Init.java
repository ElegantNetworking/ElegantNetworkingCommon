package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ElegantPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;

import java.util.*;
import java.util.function.Consumer;

public class Init {

    public static void initPackets(Consumer<String> msgPrintln, Consumer<String> channelNameConsumer) {
        Init instance = new Init(msgPrintln, channelNameConsumer);
        instance.registerAllSerializers();
        instance.registerAllPackets();
    }

    private final Consumer<String> msgPrintln;
    private final Consumer<String> channelNameConsumer;

    private Init(Consumer<String> msgPrintln, Consumer<String> channelNameConsumer) {
        this.msgPrintln = msgPrintln;
        this.channelNameConsumer = channelNameConsumer;
    }

    private static void handleErrors(String msg, Consumer<List<Throwable>> f) {
        List<Throwable> errors = new ArrayList<>();

        f.accept(errors);

        if (errors.size() > 0)
            throw new MultipleRuntimeException(msg, errors);
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private void registerAllSerializers() {
        handleErrors("Trouble while indexing serializers", errors -> {
            Iterator<ISerializerBase> iterator = ServiceLoader.load(ISerializerBase.class).iterator();
            while (iterator.hasNext()) {
                try {
                    ISerializerBase serializer = iterator.next();
                    Registry.registerSerializer(getPacketClass(serializer), serializer);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        });
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private void registerAllPackets() {

        Map<String, List<Class<? extends IByteBufSerializable>>> channelToPackets = new HashMap<>();

        handleErrors("Trouble while indexing elegant packets", errors -> {
            Iterator<IPacketProvider> iterator = ServiceLoader.load(IPacketProvider.class).iterator();
            while (iterator.hasNext()) {
                try {
                    IPacketProvider packetProvider = iterator.next();

                    Class<? extends IByteBufSerializable> packetClass = packetProvider.getPacketClass();

                    if (packetClass == null)
                        throw new InvalidPacketProviderException(packetProvider, "getPacketClass return null");

                    if (!IByteBufSerializable.class.isAssignableFrom(packetClass))
                        throw new InvalidPacketProviderException(packetProvider, packetClass.getName() + " is not implementation of IByteBufSerializable");

                    channelToPackets.computeIfAbsent(getPacketChannel(packetProvider), __ -> new ArrayList<>(3)).add(packetClass);

                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        });


        channelToPackets.forEach((channel, packets) -> {
            printStarted(channel);

            handleErrors("Failed to register packets for channel " + channel, errors -> {
                try {
                    for (int i = 0; i < packets.size(); i++) {
                        Class<? extends IByteBufSerializable> packetClass = packets.get(i);
                        int packetId = i + 1;
                        Registry.register(channel, packetId, packetClass);
                        printRegistered(channel, packetClass, packetId);
                    }
                    channelNameConsumer.accept(channel);
                    printSuccessfully(channel);
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
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

    private static String getPacketChannel(IPacketProvider packetProvider) {
        String annotatedChannel = Objects.requireNonNull(packetProvider.getPacketClass().getAnnotation(ElegantPacket.class), "Missed annotation @ElegantPacket at " + packetProvider.getPacketClass().getName()).channel();
        return annotatedChannel.equals("$modid") ? packetProvider.modid() : annotatedChannel;
    }

    private static Class<? extends IByteBufSerializable> getPacketClass(ISerializerBase serializer) {
        return Objects.requireNonNull(serializer.getClass().getAnnotation(SerializerMark.class), "Missed annotation @SerializerMark at serializer " + serializer).packetClass();
    }
}
