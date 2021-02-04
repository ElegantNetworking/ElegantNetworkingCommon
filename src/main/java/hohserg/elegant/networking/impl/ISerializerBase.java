package hohserg.elegant.networking.impl;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface ISerializerBase<Packet> {
    void serialize(Packet value, ByteBuf acc);

    Packet unserialize(ByteBuf buf);

    default void serialize_Boolean_Generic(boolean value, ByteBuf acc) {
        acc.writeBoolean(value);
    }

    default void serialize_Byte_Generic(byte value, ByteBuf acc) {
        acc.writeByte(value);
    }

    default void serialize_Short_Generic(short value, ByteBuf acc) {
        acc.writeShort(value);
    }

    default void serialize_Int_Generic(int value, ByteBuf acc) {
        acc.writeInt(value);
    }

    default void serialize_Long_Generic(long value, ByteBuf acc) {
        acc.writeLong(value);
    }

    default void serialize_Char_Generic(char value, ByteBuf acc) {
        acc.writeChar(value);
    }

    default void serialize_Float_Generic(float value, ByteBuf acc) {
        acc.writeFloat(value);
    }

    default void serialize_Double_Generic(double value, ByteBuf acc) {
        acc.writeDouble(value);
    }

    default void serialize_String_Generic(String value, ByteBuf acc) {
        byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);
        acc.writeInt(utf8Bytes.length);
        acc.writeBytes(utf8Bytes);
    }

    default void serialize_UUID_Generic(UUID value, ByteBuf acc) {
        serialize_String_Generic(value.toString(), acc);
    }


    default boolean unserialize_Boolean_Generic(ByteBuf buf) {
        return buf.readBoolean();
    }

    default byte unserialize_Byte_Generic(ByteBuf buf) {
        return buf.readByte();
    }

    default short unserialize_Short_Generic(ByteBuf buf) {
        return buf.readShort();
    }

    default int unserialize_Int_Generic(ByteBuf buf) {
        return buf.readInt();
    }

    default long unserialize_Long_Generic(ByteBuf buf) {
        return buf.readLong();
    }

    default char unserialize_Char_Generic(ByteBuf buf) {
        return buf.readChar();
    }

    default float unserialize_Float_Generic(ByteBuf buf) {
        return buf.readFloat();
    }

    default double unserialize_Double_Generic(ByteBuf buf) {
        return buf.readDouble();
    }

    default String unserialize_String_Generic(ByteBuf buf) {
        int len = buf.readInt();
        String str = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + len);
        return str;
    }

    default UUID unserialize_UUID_Generic(ByteBuf buf) {
        return UUID.fromString(unserialize_String_Generic(buf));
    }
}