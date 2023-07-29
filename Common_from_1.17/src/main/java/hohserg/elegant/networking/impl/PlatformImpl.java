package hohserg.elegant.networking.impl;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;

public class PlatformImpl implements Platform {
    @Override
    public boolean isServerSide() {
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    @Override
    public boolean isClientSide() {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }
}
