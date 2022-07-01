package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.ElegantPacket;
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

    public static String getChannelForPacket(String className) {
        return channelByPacketClassName.get(className);
    }

    public static List<String> getPacketsForChannel(String channel) {
        return channelByPacketClassName.entrySet().stream().filter(i -> i.getValue().equals(channel)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static int getPacketId(Class<? extends IByteBufSerializable> cl) throws IllegalArgumentException {
        String className = cl.getName();
        Integer exists = packetIdByPacketClassName.get(className);
        if (exists == null)
            throw new IllegalArgumentException("Packet is not registered: " + className + ". " +
                    (cl.getAnnotation(ElegantPacket.class) == null ?
                            "Need to add @ElegantPacket annotation to packet class" :
                            "Check annotation processor availability"));
        else
            return exists;
    }

    public static String getPacketName(String channel, int id) {
        return packetClassNameByChannelId.get(Pair.of(channel, id));
    }

    public static <A extends IByteBufSerializable> ISerializerBase<A> getSerializer(String className) {
        return serializerByPacketClassName.get(className);
    }

    public static <A extends IByteBufSerializable> ISerializerBase<A> getSerializerFor(Class<A> serializable) {
        return getSerializer(serializable.getName());
    }

    static void registerSerializer(Class<? extends IByteBufSerializable> serializable, ISerializerBase serializer) {
        serializerByPacketClassName.put(serializable.getName(), serializer);
    }

    static void register(String channel, int packetId, Class<? extends IByteBufSerializable> packetClass) {
        String packetClassName = packetClass.getName();

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
