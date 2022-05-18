package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ElegantPacket;
import hohserg.elegant.networking.api.IByteBufSerializable;

import java.util.*;
import java.util.function.Consumer;

import static hohserg.elegant.networking.utils.ChannelValidator.validateChannel;

public class Init {

    public static void initPackets(Consumer<String> msgPrintln, Consumer<String> warnPrintln, Consumer<String> channelNameConsumer, Config config) {
        msgPrintln.accept("Used " + config.getBackgroundPacketSystem().name() + " as background packet system");
        Init instance = new Init(msgPrintln, warnPrintln, channelNameConsumer);
        instance.registerAllSerializers();
        instance.registerAllPackets();
    }

    private final Consumer<String> msgPrintln;
    private final Consumer<String> warnPrintln;
    private final Consumer<String> channelNameConsumer;

    private Init(Consumer<String> msgPrintln, Consumer<String> warnPrintln, Consumer<String> channelNameConsumer) {
        this.msgPrintln = msgPrintln;
        this.warnPrintln = warnPrintln;
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
                } catch (TypeNotPresentException e) {
                    warnPrintln.accept("Broken serializer. It's normal in dev environment. Caused by: " + e.toString());

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
                        throw new InvalidPacketProviderException(packetProvider, "Provided class is null");

                    if (!IByteBufSerializable.class.isAssignableFrom(packetClass))
                        throw new InvalidPacketProviderException(packetProvider, "Provided class is not implementation of IByteBufSerializable");

                    channelToPackets.computeIfAbsent(getPacketChannel(packetProvider), __ -> new ArrayList<>(3)).add(packetClass);

                } catch (TypeNotPresentException | NoClassDefFoundError | InvalidPacketProviderException e) {
                    warnPrintln.accept("Broken packet provider. It's normal in dev environment. Caused by: " + e.toString());

                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        });


        channelToPackets.forEach((channel, packets) -> {
            String actualChannel = validateChannel(channel, warnPrintln);
            printStarted(actualChannel);

            handleErrors("Failed to register packets for channel " + actualChannel, errors -> {
                try {
                    for (int i = 0; i < packets.size(); i++) {
                        Class<? extends IByteBufSerializable> packetClass = packets.get(i);
                        int packetId = i + 1;
                        Registry.register(actualChannel, packetId, packetClass);
                        printRegistered(actualChannel, packetClass, packetId);
                    }
                    channelNameConsumer.accept(actualChannel);
                    printSuccessfully(actualChannel);
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
        ElegantPacket annotation = packetProvider.getPacketClass().getAnnotation(ElegantPacket.class);
        if (annotation == null)
            throw new InvalidPacketProviderException(packetProvider, "Provided class is not marked by @ElegantPacket");
        String annotatedChannel = annotation.channel();
        return trimChannel(annotatedChannel.equals("$modid") ? packetProvider.modid() : annotatedChannel);
    }

    private static String trimChannel(String channel) {
        return channel.substring(0, Math.min(channel.length(), 20));
    }

    private static Class<? extends IByteBufSerializable> getPacketClass(ISerializerBase serializer) {
        return Objects.requireNonNull(serializer.getClass().getAnnotation(SerializerMark.class), "Missed annotation @SerializerMark at serializer " + serializer).packetClass();
    }
}
