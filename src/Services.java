import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ═══════════════════════════════════════════════════════════════
//  PatientService
// ═══════════════════════════════════════════════════════════════
class PatientService {
    private static PatientService instance;
    private final Map<String, Patient> patients = new LinkedHashMap<>();

    private PatientService() {}
    public static PatientService getInstance() {
        if (instance == null) instance = new PatientService();
        return instance;
    }

    public Patient register(String name, int age, Gender gender,
                            String phone, String address, String emergencyContact,
                            String countryCode)
            throws InvalidPatientIdException, InvalidAgeException, DuplicateRecordException {
        if (age < 0 || age > 150) throw new InvalidAgeException("Age must be 0–150. Got: " + age);
        String id = PatientIdGenerator.generate(countryCode);
        Patient p = new Patient(id, name, age, gender, phone, address, emergencyContact, countryCode);
        patients.put(id, p);
        return p;
    }

    public Patient find(String id) throws RecordNotFoundException {
        Patient p = patients.get(id.toUpperCase());
        if (p == null) throw new RecordNotFoundException("Patient not found: " + id);
        return p;
    }

    public Collection<Patient> all()   { return patients.values(); }
    public int                 count() { return patients.size(); }

    public List<Patient> search(String keyword) {
        String kw = keyword.toLowerCase();
        return patients.values().stream()
            .filter(p -> p.getName().toLowerCase().contains(kw)
                      || p.getPatientId().toLowerCase().contains(kw)
                      || p.getPhone().contains(kw))
            .collect(Collectors.toList());
    }

    public void printAll() {
        if (patients.isEmpty()) { System.out.println("  No patients registered."); return; }
        System.out.println("  ┌──────────┬──────────────────────┬─────┬────────┬─────────────────┐");
        System.out.printf ("  │ %-8s │ %-20s │ %-3s │ %-6s │ %-15s │%n",
            "ID","Name","Age","Gender","Phone");
        System.out.println("  ├──────────┼──────────────────────┼─────┼────────┼─────────────────┤");
        for (Patient p : patients.values())
            System.out.printf("  │ %-8s │ %-20s │ %-3d │ %-6s │ %-15s │%n",
                p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getPhone());
        System.out.println("  └──────────┴──────────────────────┴─────┴────────┴─────────────────┘");
    }
}

// ═══════════════════════════════════════════════════════════════
//  DoctorService
// ═══════════════════════════════════════════════════════════════
class DoctorService {
    private static DoctorService instance;
    private static int seq = 1;
    private final Map<String, Doctor> doctors = new LinkedHashMap<>();

    private DoctorService() {
        // Seed two default doctors
        addDoctor("Dr. Chidi Okeke",   "General Medicine",  "08012345678","chidi@hospital.ng","Mon-Fri 08:00-16:00");
        addDoctor("Dr. Fatima Hassan", "Cardiology",        "08023456789","fatima@hospital.ng","Mon-Fri 09:00-17:00");
        addDoctor("Dr. Yuki Tanaka",   "Pediatrics",        "08034567890","yuki@hospital.ng","Tue-Sat 08:00-15:00");
    }
    public static DoctorService getInstance() {
        if (instance == null) instance = new DoctorService();
        return instance;
    }

    public Doctor addDoctor(String name, String specialization, String phone,
                            String email, String workingHours) {
        String id  = "DOC" + String.format("%04d", seq++);
        Doctor doc = new Doctor(id, name, specialization, phone, email, workingHours);
        doctors.put(id, doc);
        return doc;
    }

    public Doctor find(String id) throws RecordNotFoundException {
        Doctor d = doctors.get(id.toUpperCase());
        if (d == null) throw new RecordNotFoundException("Doctor not found: " + id);
        return d;
    }

    public Collection<Doctor> all() { return doctors.values(); }

    public void printAll() {
        if (doctors.isEmpty()) { System.out.println("  No doctors on record."); return; }
        System.out.printf("  %-8s %-22s %-20s %-15s %-22s%n",
            "ID","Name","Specialization","Phone","Working Hours");
        System.out.println("  " + "─".repeat(90));
        doctors.values().forEach(Doctor::printInfo);
    }

    public void printSchedule(String doctorId, List<Appointment> appts) throws RecordNotFoundException {
        Doctor d = find(doctorId);
        System.out.println("  Schedule for " + d.getName() + " [" + d.getSpecialization() + "]");
        System.out.println("  Hours: " + d.getWorkingHours());
        List<Appointment> mine = appts.stream()
            .filter(a -> a.getDoctorId().equals(doctorId) && a.getStatus() == AppointmentStatus.SCHEDULED)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
        if (mine.isEmpty()) System.out.println("  No upcoming appointments.");
        else mine.forEach(a -> System.out.println("  • " + a));
    }
}

// ═══════════════════════════════════════════════════════════════
//  AppointmentService
// ═══════════════════════════════════════════════════════════════
class AppointmentService {
    private static AppointmentService instance;
    private final List<Appointment> appointments = new ArrayList<>();

    private AppointmentService() {}
    public static AppointmentService getInstance() {
        if (instance == null) instance = new AppointmentService();
        return instance;
    }

    public Appointment book(String patientId, String doctorId,
                            LocalDateTime dateTime, String reason) {
        Appointment a = new Appointment(patientId, doctorId, dateTime, reason);
        appointments.add(a);
        return a;
    }

    public Appointment find(String apptId) throws RecordNotFoundException {
        return appointments.stream()
            .filter(a -> a.getApptId().equals(apptId))
            .findFirst()
            .orElseThrow(() -> new RecordNotFoundException("Appointment not found: " + apptId));
    }

    public void reschedule(String apptId, LocalDateTime newDT) throws RecordNotFoundException {
        Appointment a = find(apptId);
        a.setDateTime(newDT);
        a.setStatus(AppointmentStatus.RESCHEDULED);
    }

    public void cancel(String apptId) throws RecordNotFoundException {
        find(apptId).setStatus(AppointmentStatus.CANCELLED);
    }

    public void complete(String apptId) throws RecordNotFoundException {
        find(apptId).setStatus(AppointmentStatus.COMPLETED);
    }

    public List<Appointment> all()                     { return appointments; }
    public List<Appointment> forPatient(String pid)    { return appointments.stream().filter(a->a.getPatientId().equals(pid)).collect(Collectors.toList()); }
    public List<Appointment> forDoctor(String did)     { return appointments.stream().filter(a->a.getDoctorId().equals(did)).collect(Collectors.toList()); }
    public List<Appointment> scheduled()               { return appointments.stream().filter(a->a.getStatus()==AppointmentStatus.SCHEDULED).collect(Collectors.toList()); }

    public void printAll() {
        if (appointments.isEmpty()) { System.out.println("  No appointments."); return; }
        appointments.forEach(a -> System.out.println("  • " + a));
    }
}

// ═══════════════════════════════════════════════════════════════
//  PharmacyService
// ═══════════════════════════════════════════════════════════════
class PharmacyService {
    private static PharmacyService instance;
    private static int seq = 1;
    private final Map<String, Medicine> inventory = new LinkedHashMap<>();

    private PharmacyService() {
        addMedicine("Paracetamol 500mg", "Analgesic",    500, 50,  0.20);
        addMedicine("Amoxicillin 250mg", "Antibiotic",   200, 30,  0.80);
        addMedicine("Metformin 500mg",   "Antidiabetic", 150, 20,  0.60);
        addMedicine("Amlodipine 5mg",    "Antihyperten.",100, 15,  1.20);
        addMedicine("Ibuprofen 400mg",   "Analgesic",    300, 40,  0.35);
        addMedicine("Coartem 80mg",      "Antimalarial", 100, 25,  2.50);
    }
    public static PharmacyService getInstance() {
        if (instance == null) instance = new PharmacyService();
        return instance;
    }

    public Medicine addMedicine(String name, String category,
                                int qty, int reorder, double price) {
        String id  = "MED" + String.format("%04d", seq++);
        Medicine m = new Medicine(id, name, category, qty, reorder, price);
        inventory.put(id, m);
        return m;
    }

    public Medicine find(String id) throws RecordNotFoundException {
        Medicine m = inventory.get(id.toUpperCase());
        if (m == null) throw new RecordNotFoundException("Medicine not found: " + id);
        return m;
    }

    public double sell(String medId, int qty) throws RecordNotFoundException, InsufficientStockException {
        Medicine m = find(medId);
        m.deduct(qty);
        return m.getUnitPrice() * qty;
    }

    public void printInventory() {
        System.out.printf("  %-8s %-22s %-15s %6s  %5s  %-10s%n",
            "ID","Name","Category","Stock","Reorder","Unit Price");
        System.out.println("  " + "─".repeat(80));
        inventory.values().forEach(Medicine::printInfo);
    }

    public void printLowStockAlerts() {
        List<Medicine> low = inventory.values().stream().filter(Medicine::isLowStock).collect(Collectors.toList());
        if (low.isEmpty()) { System.out.println("  ✔ All stock levels are OK."); return; }
        System.out.println("  ⚠  LOW STOCK ALERT:");
        low.forEach(m -> System.out.printf("  %-22s — Stock: %d (Reorder at: %d)%n",
            m.getName(), m.getQuantityInStock(), m.getReorderLevel()));
    }

    public Collection<Medicine> all() { return inventory.values(); }
}

// ═══════════════════════════════════════════════════════════════
//  LabService
// ═══════════════════════════════════════════════════════════════
class LabService {
    private static LabService instance;
    private final List<LabTest> tests = new ArrayList<>();

    private LabService() {}
    public static LabService getInstance() {
        if (instance == null) instance = new LabService();
        return instance;
    }

    public LabTest requestTest(String patientId, String testName, String doctorId) {
        LabTest t = new LabTest(patientId, testName, doctorId);
        tests.add(t);
        return t;
    }

    public LabTest find(String testId) throws RecordNotFoundException {
        return tests.stream().filter(t -> t.getTestId().equals(testId))
            .findFirst()
            .orElseThrow(() -> new RecordNotFoundException("Lab test not found: " + testId));
    }

    public void enterResult(String testId, String result) throws RecordNotFoundException {
        find(testId).setResult(result);
    }

    public List<LabTest> forPatient(String pid) {
        return tests.stream().filter(t -> t.getPatientId().equals(pid)).collect(Collectors.toList());
    }

    public void printAll() {
        if (tests.isEmpty()) { System.out.println("  No lab tests on record."); return; }
        tests.forEach(t -> System.out.println("  • " + t));
    }

    public void printReport(String testId) throws RecordNotFoundException {
        LabTest t = find(testId);
        System.out.println("  ┌──────────────────────────────────────────────┐");
        System.out.printf ("  │  Lab Test Report                             │%n");
        System.out.printf ("  │  Test ID   : %-31s│%n", t.getTestId());
        System.out.printf ("  │  Patient   : %-31s│%n", t.getPatientId());
        System.out.printf ("  │  Test Name : %-31s│%n", t.getTestName());
        System.out.printf ("  │  Status    : %-31s│%n", t.getStatus());
        System.out.printf ("  │  Result    : %-31s│%n",
            t.getResult().isEmpty() ? "Awaiting" : t.getResult());
        System.out.println("  └──────────────────────────────────────────────┘");
    }
}

// ═══════════════════════════════════════════════════════════════
//  WardService
// ═══════════════════════════════════════════════════════════════
class WardService {
    private static WardService instance;
    private final Map<String, Bed> beds = new LinkedHashMap<>();

    private WardService() {
        // Seed some beds across two wards
        String[] wards = {"General Ward", "ICU", "Pediatric Ward", "Maternity Ward"};
        int[] counts   = {10, 4, 6, 4};
        char prefix    = 'A';
        for (int w = 0; w < wards.length; w++) {
            for (int b = 1; b <= counts[w]; b++) {
                String id = prefix + String.format("%02d", b);
                beds.put(id, new Bed(id, wards[w]));
            }
            prefix++;
        }
    }
    public static WardService getInstance() {
        if (instance == null) instance = new WardService();
        return instance;
    }

    public Bed find(String bedId) throws RecordNotFoundException {
        Bed b = beds.get(bedId.toUpperCase());
        if (b == null) throw new RecordNotFoundException("Bed not found: " + bedId);
        return b;
    }

    public Bed findAvailableIn(String ward) throws BedUnavailableException {
        return beds.values().stream()
            .filter(b -> b.getWard().equalsIgnoreCase(ward) && b.getStatus() == BedStatus.AVAILABLE)
            .findFirst()
            .orElseThrow(() -> new BedUnavailableException("No available beds in ward: " + ward));
    }

    public void admit(String bedId, String patientId)
            throws RecordNotFoundException, BedUnavailableException {
        find(bedId).admit(patientId);
    }

    public void discharge(String bedId) throws RecordNotFoundException {
        find(bedId).discharge();
    }

    public void transfer(String fromBedId, String toBedId)
            throws RecordNotFoundException, BedUnavailableException {
        Bed from = find(fromBedId);
        Bed to   = find(toBedId);
        String pid = from.getOccupantId();
        if (pid == null) throw new RecordNotFoundException("Bed " + fromBedId + " is not occupied.");
        to.admit(pid);
        from.discharge();
    }

    public void printBeds() {
        System.out.printf("  %-8s %-18s %-12s %s%n","Bed ID","Ward","Status","Occupant");
        System.out.println("  " + "─".repeat(65));
        beds.values().forEach(Bed::printInfo);
    }

    public long availableCount() {
        return beds.values().stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
    }
}

// ═══════════════════════════════════════════════════════════════
//  BillingService
// ═══════════════════════════════════════════════════════════════
class BillingService {
    private static BillingService instance;
    private final Map<String, Bill> bills = new LinkedHashMap<>();

    private BillingService() {}
    public static BillingService getInstance() {
        if (instance == null) instance = new BillingService();
        return instance;
    }

    public Bill createBill(String patientId) {
        Bill b = new Bill(patientId);
        bills.put(b.getBillId(), b);
        return b;
    }

    public Bill find(String billId) throws RecordNotFoundException {
        Bill b = bills.get(billId);
        if (b == null) throw new RecordNotFoundException("Bill not found: " + billId);
        return b;
    }

    public Bill findOrCreate(String patientId) {
        return bills.values().stream()
            .filter(b -> b.getPatientId().equals(patientId) && b.getStatus() != PaymentStatus.PAID)
            .findFirst()
            .orElseGet(() -> createBill(patientId));
    }

    public void recordPayment(String billId, double amount) throws RecordNotFoundException {
        find(billId).recordPayment(amount);
    }

    public void printAll() {
        if (bills.isEmpty()) { System.out.println("  No bills on record."); return; }
        System.out.printf("  %-10s %-10s %10s %10s %10s %-8s%n",
            "Bill ID","Patient","Total","Paid","Outstanding","Status");
        System.out.println("  " + "─".repeat(65));
        for (Bill b : bills.values())
            System.out.printf("  %-10s %-10s %10.2f %10.2f %10.2f %-8s%n",
                b.getBillId(), b.getPatientId(), b.getTotal(), b.getPaid(), b.getOutstanding(), b.getStatus());
    }

    public double totalRevenue() {
        return bills.values().stream().mapToDouble(Bill::getPaid).sum();
    }

    public double totalOutstanding() {
        return bills.values().stream().mapToDouble(Bill::getOutstanding).sum();
    }
}

// ═══════════════════════════════════════════════════════════════
//  StaffService
// ═══════════════════════════════════════════════════════════════
class StaffService {
    private static StaffService instance;
    private final List<Staff> staffList = new ArrayList<>();

    private StaffService() {
        staffList.add(new Staff("Amara Diallo",   Role.NURSE,         "08044444444","Morning 07:00-15:00"));
        staffList.add(new Staff("Blessing Osei",  Role.RECEPTIONIST,  "08055555555","Morning 08:00-16:00"));
        staffList.add(new Staff("Emeka Nwosu",    Role.NURSE,         "08066666666","Night 22:00-06:00"));
    }
    public static StaffService getInstance() {
        if (instance == null) instance = new StaffService();
        return instance;
    }

    public Staff addStaff(String name, Role role, String phone, String shift) {
        Staff s = new Staff(name, role, phone, shift);
        staffList.add(s);
        return s;
    }

    public List<Staff> all()           { return staffList; }
    public List<Staff> byRole(Role r)  { return staffList.stream().filter(s->s.getRole()==r).collect(Collectors.toList()); }

    public void printAll() {
        System.out.printf("  %-8s %-22s %-15s %-15s %-22s%n","ID","Name","Role","Phone","Shift");
        System.out.println("  " + "─".repeat(85));
        staffList.forEach(Staff::printInfo);
    }
}

// ═══════════════════════════════════════════════════════════════
//  ReportService
// ═══════════════════════════════════════════════════════════════
class ReportService {
    private static ReportService instance;
    private ReportService() {}
    public static ReportService getInstance() {
        if (instance == null) instance = new ReportService();
        return instance;
    }

    public void dailyPatientsReport() {
        PatientService ps = PatientService.getInstance();
        System.out.println("  ── Daily Patients Report ──────────────────────");
        System.out.println("  Total Registered Patients : " + ps.count());
        System.out.println("  Today's Date              : " + LocalDate.now());
        long inWard = ps.all().stream().filter(p -> p.getWardBedId() != null).count();
        System.out.println("  Currently Admitted        : " + inWard);
        System.out.println("  Outpatients               : " + (ps.count() - inWard));
    }

    public void revenueReport() {
        BillingService bs = BillingService.getInstance();
        System.out.println("  ── Revenue Report ──────────────────────────────");
        System.out.printf ("  Total Billed     : $%.2f%n", bs.totalRevenue() + bs.totalOutstanding());
        System.out.printf ("  Total Collected  : $%.2f%n", bs.totalRevenue());
        System.out.printf ("  Total Outstanding: $%.2f%n", bs.totalOutstanding());
    }

    public void doctorPerformanceReport() {
        DoctorService ds = DoctorService.getInstance();
        AppointmentService as = AppointmentService.getInstance();
        System.out.println("  ── Doctor Performance Report ───────────────────");
        for (Doctor d : ds.all()) {
            long completed = as.forDoctor(d.getDoctorId()).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            long assigned  = d.getAssignedPatientIds().size();
            System.out.printf("  %-22s | Completed Appts: %3d | Patients: %3d%n",
                d.getName(), completed, assigned);
        }
    }

    public void medicineUsageReport() {
        PharmacyService ph = PharmacyService.getInstance();
        System.out.println("  ── Medicine Usage / Stock Report ───────────────");
        ph.printInventory();
        System.out.println();
        ph.printLowStockAlerts();
    }

    public void wardReport() {
        WardService ws = WardService.getInstance();
        System.out.println("  ── Ward / Bed Report ───────────────────────────");
        ws.printBeds();
        System.out.println("  Available beds: " + ws.availableCount());
    }
}
