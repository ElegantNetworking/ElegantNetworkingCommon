package hohserg.elegant.networking.impl;

public class Config {
    int packetSizeLimit;
    BackgroundPacketSystem backgroundPacketSystem;

    public int getPacketSizeLimit() {
        return packetSizeLimit;
    }

    public BackgroundPacketSystem getBackgroundPacketSystem() {
        return backgroundPacketSystem;
    }

    public enum BackgroundPacketSystem {
        CCLImpl,
        ForgeImpl
    }
}
