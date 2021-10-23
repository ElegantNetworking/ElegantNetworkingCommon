package hohserg.elegant.networking.utils;

import java.util.function.Consumer;

public class ChannelValidator {
    public static String validateChannel(String channel, Consumer<String> warn) {
        String actualChannel = channel.substring(0, Math.min(channel.length(), 20));
        if (channel.length() > 20)
            warn.accept("Channel name must be no longer that 20. Found: " + channel + ". Used: " + actualChannel);
        return actualChannel;
    }
}
