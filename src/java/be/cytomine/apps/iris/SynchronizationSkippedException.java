package be.cytomine.apps.iris;

/**
 * Custom exception, which is thrown if the synchronization of a domain object with Cytomine
 * is skipped due to any reason.
 */
public class SynchronizationSkippedException extends Exception {

    public static enum REASON {
        HOST_DID_NOT_MATCH,
        SYNC_DISABLED,
        UNDEFINED
    }

    private REASON reason = REASON.UNDEFINED;

    // hide default constructor
    private SynchronizationSkippedException() {
    }

    public SynchronizationSkippedException(REASON reason) {
        super();
        this.reason = reason;
    }

    public SynchronizationSkippedException(String message, REASON reason) {
        super(message);
        this.reason = reason;
    }

    public REASON getReason() {
        return reason;
    }

    @Override
    public String toString() {
        String supStr = super.toString();
        return supStr + " -> Reason " + this.getReason();
    }
}
