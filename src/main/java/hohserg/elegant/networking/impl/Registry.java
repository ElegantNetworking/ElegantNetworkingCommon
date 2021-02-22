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

    static int getPacketId(String className) {
        return packetIdByPacketClassName.get(className);
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

    static void register(PacketInfo p) {
        ISerializerBase serializer = serializerByPacketClassName.get(p.className);
        if (serializer == null)
            throw new RuntimeException("Serializer for packet " + p.className + " not found");
        channelByPacketClassName.put(p.className, p.channel);
        packetIdByPacketClassName.put(p.className, p.id);
        packetClassNameByChannelId.put(Pair.of(p.channel, p.id), p.className);
    }

    @Value
    static class PacketInfo {
        public String channel;
        public int id;
        public String className;
    }
}
