package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.IByteBufSerializable;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Registry {
    private static Map<String, String> channelByPacketClassName = new HashMap<>();
    private static Map<String, Integer> packetIdByPacketClassName = new HashMap<>();
    private static Map<Pair<String, Integer>, String> packetClassNameByChannelId = new HashMap<>();
    private static Map<String, ISerializerBase> serializerByPacketClassName = new HashMap<>();

    static String getChannelForPacket(String className) {
        return channelByPacketClassName.get(className);
    }

    static List<String> getPacketsForChannel(String channel) {
        return channelByPacketClassName.entrySet().stream().filter(i -> i.getValue().equals(channel)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    static int getPacketId(String className) throws IllegalArgumentException {
        Integer exists = packetIdByPacketClassName.get(className);
        if (exists == null)
            throw new IllegalArgumentException("Packet is not registered: " + className + ". Need to add @ElegantPacket annotation to packet class");
        else
            return exists;
    }

    static String getPacketName(String channel, int id) {
        return packetClassNameByChannelId.get(Pair.of(channel, id));
    }

    public static <A extends IByteBufSerializable> ISerializerBase<A> getSerializer(String className) {
        return serializerByPacketClassName.get(className);
    }

    public static <A extends IByteBufSerializable> ISerializerBase<A> getSerializerFor(Class<A> serializable) {
        return getSerializer(serializable.getCanonicalName());
    }

    static void registerSerializer(Class<? extends IByteBufSerializable> serializable, ISerializerBase serializer) {
        serializerByPacketClassName.put(serializable.getCanonicalName(), serializer);
    }

    static void register(String channel, int packetId, String packetClassName) {
        ISerializerBase serializer = serializerByPacketClassName.get(packetClassName);
        if (serializer == null)
            throw new RuntimeException("Serializer for packet " + packetClassName + " not found");
        channelByPacketClassName.put(packetClassName, channel);
        packetIdByPacketClassName.put(packetClassName, packetId);
        packetClassNameByChannelId.put(Pair.of(channel, packetId), packetClassName);
    }

    @Value
    static class PacketInfo {
        public String channel;
        public int id;
        public String className;
    }
}
