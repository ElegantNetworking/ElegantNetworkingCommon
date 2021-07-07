package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.IByteBufSerializable;

public interface IPacketProvider {
    Class<? extends IByteBufSerializable> getPacketClass();

    String modid();
}
