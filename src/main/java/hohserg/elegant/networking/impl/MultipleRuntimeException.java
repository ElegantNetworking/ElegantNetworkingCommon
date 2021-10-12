package hohserg.elegant.networking.impl;

import java.util.List;

public class MultipleRuntimeException extends RuntimeException {
    public MultipleRuntimeException(String msg, List<Throwable> errors) {
        super(msg + ". Total " + errors.size() + " errors");
        errors.forEach(this::addSuppressed);
    }
}
