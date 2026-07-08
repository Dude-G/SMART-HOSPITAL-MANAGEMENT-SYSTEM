import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PatientIdGenerator — generates IDs in format: XXXX-CC
 * Supports 13 countries.
 */
public class PatientIdGenerator {

    public static final Map<String, String> COUNTRY_NAMES = new LinkedHashMap<>();
    static {
        COUNTRY_NAMES.put("NG", "Nigeria");
        COUNTRY_NAMES.put("US", "United States");
        COUNTRY_NAMES.put("GB", "United Kingdom");
        COUNTRY_NAMES.put("GH", "Ghana");
        COUNTRY_NAMES.put("KE", "Kenya");
        COUNTRY_NAMES.put("ZA", "South Africa");
        COUNTRY_NAMES.put("IN", "India");
        COUNTRY_NAMES.put("CA", "Canada");
        COUNTRY_NAMES.put("AU", "Australia");
        COUNTRY_NAMES.put("DE", "Germany");
        COUNTRY_NAMES.put("FR", "France");
        COUNTRY_NAMES.put("BR", "Brazil");
        COUNTRY_NAMES.put("JP", "Japan");
    }

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static String generate(String countryCode) throws InvalidPatientIdException {
        String code = countryCode == null ? "" : countryCode.trim().toUpperCase();
        if (!COUNTRY_NAMES.containsKey(code))
            throw new InvalidPatientIdException(
                "Unsupported country code: \"" + code + "\". Supported: " + COUNTRY_NAMES.keySet());
        int n = counter.incrementAndGet();
        if (n > 9999) throw new InvalidPatientIdException("Patient counter exceeded 9999. Contact admin.");
        return String.format("%04d-%s", n, code);
    }

    public static void validate(String id) throws InvalidPatientIdException {
        if (id == null || !id.matches("\\d{4}-[A-Z]{2}"))
            throw new InvalidPatientIdException("ID \"" + id + "\" must be format XXXX-CC (e.g. 0001-NG).");
        String code = id.substring(5);
        if (!COUNTRY_NAMES.containsKey(code))
            throw new InvalidPatientIdException("Country code \"" + code + "\" is not supported.");
    }

    public static String countryName(String code) {
        return COUNTRY_NAMES.getOrDefault(code.toUpperCase(), "Unknown");
    }

    public static void printCountryCodes() {
        System.out.println("\n  Supported Country Codes:");
        int i = 0;
        for (Map.Entry<String, String> e : COUNTRY_NAMES.entrySet()) {
            System.out.printf("  %-3s %-18s", e.getKey(), e.getValue());
            if (++i % 3 == 0) System.out.println();
        }
        System.out.println();
    }
}
