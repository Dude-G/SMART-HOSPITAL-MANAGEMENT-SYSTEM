// ─────────────────────────────────────────────────
//  Enums — shared constants used across the system
// ─────────────────────────────────────────────────

enum Role {
    ADMIN, DOCTOR, NURSE, RECEPTIONIST
}

enum Gender {
    MALE, FEMALE, OTHER;

    public static Gender parse(String s) {
        return switch (s.trim().toUpperCase()) {
            case "M", "MALE"   -> MALE;
            case "F", "FEMALE" -> FEMALE;
            default            -> OTHER;
        };
    }
}

enum AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
}

enum TestStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum BedStatus {
    AVAILABLE, OCCUPIED, MAINTENANCE
}

enum PaymentStatus {
    UNPAID, PARTIAL, PAID
}
