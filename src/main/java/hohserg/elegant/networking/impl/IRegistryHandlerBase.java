package hohserg.elegant.networking.impl;

import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static hohserg.elegant.networking.Refs.reportUrlPlea;

public interface IRegistryHandlerBase {

    default <A> void serializeSingleton(Class<A> type, A value, ByteBuf acc) {
        Integer id = getMapper(type).valueToId.apply(value);
        if (id == -1)
            throw new IllegalArgumentException("Attempt to serialize unregistered " + type.getSimpleName() + ": " + value);
        acc.writeInt(id);
    }

    default <A> A unserializeSingleton(Class<A> type, ByteBuf acc) {
        A r = getMapper(type).idToValue.apply(acc.readInt());
        if (r == null)
            throw new IllegalArgumentException("Illegal id of " + type.getSimpleName() + " from ByteBuf");
        return r;
    }

    static Map<Class, IdMapper> mappers = new HashMap<>();

    default  <A> void register(Class<A> type, Function<A, Integer> valueToId, Function<Integer, A> idToValue) {
        mappers.put(type, new IdMapper<>(valueToId, idToValue));
    }

    static <A> IdMapper<A> getMapper(Class<A> type) {
        IdMapper idMapper = mappers.get(type);
        if (idMapper == null)
            throw new IllegalArgumentException("Attempt to get id mapper for unregistered type " + type.getSimpleName() + ". " + reportUrlPlea);
        return idMapper;
    }

    @Value
    class IdMapper<A> {
        Function<A, Integer> valueToId;
        Function<Integer, A> idToValue;
    }
}
