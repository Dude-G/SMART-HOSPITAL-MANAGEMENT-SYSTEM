import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient — full patient model with all required fields.
 */
public class Patient {

    // ── Core Identity ──────────────────────────────────────────
    private final String patientId;
    private String name;
    private int    age;
    private Gender gender;
    private String phone;
    private String address;
    private String emergencyContact;   // "Name | Relationship | Phone"
    private String countryCode;
    private final LocalDate registrationDate;

    // ── Medical Profile ────────────────────────────────────────
    private final List<String>      medicalConditions = new ArrayList<>();
    private final List<String>      allergies         = new ArrayList<>();
    private final List<Visit>       visitHistory      = new ArrayList<>();
    private final List<String>      testResults       = new ArrayList<>();
    private final List<Prescription> prescriptions    = new ArrayList<>();

    // ── Assignment ────────────────────────────────────────────
    private String assignedDoctorId;
    private String wardBedId;          // null if out-patient

    public Patient(String patientId, String name, int age, Gender gender,
                   String phone, String address, String emergencyContact,
                   String countryCode) {
        this.patientId        = patientId;
        this.name             = name;
        this.age              = age;
        this.gender           = gender;
        this.phone            = phone;
        this.address          = address;
        this.emergencyContact = emergencyContact;
        this.countryCode      = countryCode;
        this.registrationDate = LocalDate.now();
    }

    // ── Getters / Setters ─────────────────────────────────────
    public String    getPatientId()        { return patientId; }
    public String    getName()             { return name; }
    public int       getAge()              { return age; }
    public Gender    getGender()           { return gender; }
    public String    getPhone()            { return phone; }
    public String    getAddress()          { return address; }
    public String    getEmergencyContact() { return emergencyContact; }
    public String    getCountryCode()      { return countryCode; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String    getAssignedDoctorId() { return assignedDoctorId; }
    public String    getWardBedId()        { return wardBedId; }

    public void setName(String n)                  { name = n; }
    public void setAge(int a)                      { age = a; }
    public void setGender(Gender g)                { gender = g; }
    public void setPhone(String p)                 { phone = p; }
    public void setAddress(String a)               { address = a; }
    public void setEmergencyContact(String ec)     { emergencyContact = ec; }
    public void setAssignedDoctorId(String id)     { assignedDoctorId = id; }
    public void setWardBedId(String bedId)         { wardBedId = bedId; }

    public List<String>       getMedicalConditions() { return medicalConditions; }
    public List<String>       getAllergies()          { return allergies; }
    public List<Visit>        getVisitHistory()       { return visitHistory; }
    public List<String>       getTestResults()        { return testResults; }
    public List<Prescription> getPrescriptions()      { return prescriptions; }

    public void addCondition(String c)        { medicalConditions.add(c); }
    public void addAllergy(String a)          { allergies.add(a); }
    public void addVisit(Visit v)             { visitHistory.add(v); }
    public void addTestResult(String r)       { testResults.add(r); }
    public void addPrescription(Prescription p) { prescriptions.add(p); }

    // ── Display ───────────────────────────────────────────────
    public void printSummary() {
        System.out.println("  ┌──────────────────────────────────────────────────┐");
        System.out.printf ("  │  ID      : %-37s│%n", patientId);
        System.out.printf ("  │  Name    : %-37s│%n", name);
        System.out.printf ("  │  Age     : %-37s│%n", age + "  |  Gender: " + gender);
        System.out.printf ("  │  Phone   : %-37s│%n", phone);
        System.out.printf ("  │  Address : %-37s│%n", address);
        System.out.printf ("  │  Emerg.  : %-37s│%n", emergencyContact);
        System.out.printf ("  │  Country : %-37s│%n", PatientIdGenerator.countryName(countryCode));
        System.out.printf ("  │  Reg.    : %-37s│%n", registrationDate);
        System.out.printf ("  │  Doctor  : %-37s│%n", assignedDoctorId == null ? "Unassigned" : assignedDoctorId);
        System.out.printf ("  │  Bed     : %-37s│%n", wardBedId == null ? "Outpatient" : wardBedId);
        System.out.println("  └──────────────────────────────────────────────────┘");
    }

    public void printFullRecord() {
        printSummary();
        System.out.println("  Medical Conditions : " + (medicalConditions.isEmpty() ? "None" : String.join(", ", medicalConditions)));
        System.out.println("  Allergies          : " + (allergies.isEmpty()         ? "None" : String.join(", ", allergies)));
        System.out.println("  Total Visits       : " + visitHistory.size());
        System.out.println("  Prescriptions      : " + prescriptions.size());
        System.out.println("  Test Results       : " + testResults.size());
        if (!visitHistory.isEmpty()) {
            System.out.println("  ── Visit History ──────────────────────────────────");
            for (Visit v : visitHistory) System.out.println("    " + v);
        }
        if (!prescriptions.isEmpty()) {
            System.out.println("  ── Prescriptions ──────────────────────────────────");
            for (Prescription p : prescriptions) System.out.println("    " + p);
        }
        if (!testResults.isEmpty()) {
            System.out.println("  ── Test Results ────────────────────────────────────");
            for (String r : testResults) System.out.println("    • " + r);
        }
    }
}
