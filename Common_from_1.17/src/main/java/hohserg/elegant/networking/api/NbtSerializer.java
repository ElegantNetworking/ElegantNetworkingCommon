package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ISerializerBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;

public class NbtSerializer<A extends IByteBufSerializable> {
    private final ISerializerBase<A> serializer;

    public NbtSerializer(ISerializerBase<A> serializer) {
        this.serializer = serializer;
    }

    public CompoundTag serialize(A value) {
        CompoundTag r = new CompoundTag();
        r.put("content", serializeToByteArray(value));
        return r;
    }

    public A unserialize(CompoundTag nbt) {
        if (nbt.contains("content", 7))
            return unserializeFromByteArray((ByteArrayTag) nbt.get("content"));
        else
            throw new IllegalArgumentException("invalid nbt data " + nbt);
    }

    public ByteArrayTag serializeToByteArray(A value) {
        ByteBuf buffer = Unpooled.buffer();
        serializer.serialize(value, buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new ByteArrayTag(bytes);
    }

    public A unserializeFromByteArray(ByteArrayTag nbt) {
        ByteBuf buffer = Unpooled.buffer(nbt.getAsByteArray().length);
        buffer.writeBytes(nbt.getAsByteArray());
        return serializer.unserialize(buffer);
    }
}
