package ccm.cva.shared.exception;

import java.util.List;

public class DomainValidationException extends RuntimeException {

    private final transient List<String> messages;

    public DomainValidationException(String message, List<String> messages) {
        super(message);
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
