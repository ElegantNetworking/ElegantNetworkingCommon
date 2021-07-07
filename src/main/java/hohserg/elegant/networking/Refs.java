package hohserg.elegant.networking;

public class Refs {
    public static final String serviceFolder = "META-INF/services/";

    public static final String ElegantPacket_name = "hohserg.elegant.networking.api.ElegantPacket";
    public static final String ElegantSerializable_name = "hohserg.elegant.networking.api.ElegantSerializable";
    public static final String Mod_name_1_8_plus = "net.minecraftforge.fml.common.Mod";
    public static final String Mod_name_1_7_minus = "cpw.mods.fml.common.Mod";
    public static final String ClientToServerPacket_name = "hohserg.elegant.networking.api.ClientToServerPacket";
    public static final String ServerToClientPacket_name = "hohserg.elegant.networking.api.ServerToClientPacket";
    public static final String IByteBufSerializable_name = "hohserg.elegant.networking.api.IByteBufSerializable";
    public static final String ISerializer_name = "hohserg.elegant.networking.impl.ISerializer";
    public static final String ISerializerBase_name = "hohserg.elegant.networking.impl.ISerializerBase";
    public static final String IPacketProvider_name = "hohserg.elegant.networking.impl.IPacketProvider";
    public static final String SerializerMark_name = "hohserg.elegant.networking.impl.SerializerMark";
    public static final String PacketProviderMark_name = "hohserg.elegant.networking.impl.PacketProviderMark";

    public static final String relocatePrefix = "shadow.";
    public static String reportUrlPlea = "Please, report to https://github.com/ElegantNetworking/ElegantNetworkingAnnotationProcessor/issues";

    private static String getOriginalQualifierName(String maybeShadowedName) {
        if (maybeShadowedName.startsWith(relocatePrefix))
            return maybeShadowedName.substring(relocatePrefix.length());
        else
            return maybeShadowedName;
    }

    public static final String ImmutableList_name = getOriginalQualifierName("com.google.common.collect.ImmutableList");
    public static final String ImmutableSet_name = getOriginalQualifierName("com.google.common.collect.ImmutableSet");
    public static final String ImmutableMap_name = getOriginalQualifierName("com.google.common.collect.ImmutableMap");
    public static final String Pair_name = getOriginalQualifierName("org.apache.commons.lang3.tuple.Pair");


    public static String getServicePath(String interfaceName) {
        return serviceFolder + interfaceName;
    }

    public static final String basePath = "hohserg/elegant/networking/";
    public static final String processorPath = basePath + "annotation/processor/";
    public static final String apiPath = basePath + "api/";
    public static final String testPath = basePath + "test/";
}
