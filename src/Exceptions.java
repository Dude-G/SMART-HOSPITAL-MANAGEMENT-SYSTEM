// ─────────────────────────────────────────────────────────────
//  Custom Exceptions
// ─────────────────────────────────────────────────────────────

class InvalidPatientIdException extends Exception {
    public InvalidPatientIdException(String msg) { super(msg); }
}

class DuplicateRecordException extends Exception {
    public DuplicateRecordException(String msg) { super(msg); }
}

class RecordNotFoundException extends Exception {
    public RecordNotFoundException(String msg) { super(msg); }
}

class AuthenticationException extends Exception {
    public AuthenticationException(String msg) { super(msg); }
}

class InsufficientStockException extends Exception {
    public InsufficientStockException(String msg) { super(msg); }
}

class InvalidAgeException extends Exception {
    public InvalidAgeException(String msg) { super(msg); }
}

class BedUnavailableException extends Exception {
    public BedUnavailableException(String msg) { super(msg); }
}
