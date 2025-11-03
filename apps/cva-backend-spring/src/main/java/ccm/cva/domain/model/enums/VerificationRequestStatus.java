package ccm.cva.domain.model.enums;

public enum VerificationRequestStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED;
    }
}
