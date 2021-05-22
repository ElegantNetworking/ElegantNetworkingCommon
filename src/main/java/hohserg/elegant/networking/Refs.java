package hohserg.elegant.networking;

public class Refs {
    public static final String serviceFolder = "META-INF/services/";

    public static final String ElegantPacket_name = "hohserg.elegant.networking.api.ElegantPacket";
    public static final String ElegantSerializable_name = "hohserg.elegant.networking.api.ElegantSerializable";
    public static final String ClientToServerPacket_name = "hohserg.elegant.networking.api.ClientToServerPacket";
    public static final String ServerToClientPacket_name = "hohserg.elegant.networking.api.ServerToClientPacket";
    public static final String IByteBufSerializable_name = "hohserg.elegant.networking.api.IByteBufSerializable";
    public static final String ISerializer_name = "hohserg.elegant.networking.impl.ISerializer";
    public static final String SerializerMark_name = "hohserg.elegant.networking.impl.SerializerMark";

    public static String getServicePath(String interfaceName) {
        return serviceFolder + interfaceName;
    }

    public static final String basePath = "hohserg/elegant/networking/";
    public static final String processorPath = basePath + "annotation/processor/";
    public static final String apiPath = basePath + "api/";
    public static final String testPath = basePath + "test/";
}
