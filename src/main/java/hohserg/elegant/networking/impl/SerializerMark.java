package hohserg.elegant.networking.impl;

import hohserg.elegant.networking.api.IByteBufSerializable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializerMark {
    Class<? extends IByteBufSerializable> packetClass();
}
