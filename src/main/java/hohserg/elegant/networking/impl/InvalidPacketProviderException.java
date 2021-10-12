package hohserg.elegant.networking.impl;

public class InvalidPacketProviderException extends RuntimeException {
    public InvalidPacketProviderException(IPacketProvider packetProvider, String msg) {
        super(packetProvider.getClass().getName() + ": " + msg);
    }
}
