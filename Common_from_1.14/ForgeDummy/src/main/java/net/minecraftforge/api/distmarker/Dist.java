//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraftforge.api.distmarker;

public enum Dist {
    CLIENT,
    DEDICATED_SERVER;

    private Dist() {
    }

    public boolean isDedicatedServer() {
        return !this.isClient();
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}
