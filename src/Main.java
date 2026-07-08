import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║         SMART HOSPITAL MANAGEMENT SYSTEM  — Main Entry          ║
 * ║         Aptech Java OOP Assignment  (Extended)                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Modules:
 *   1.  Patient Registration & Management
 *   2.  Patient Records (history, conditions, allergies)
 *   3.  Appointment Management
 *   4.  Doctor Management
 *   5.  Billing & Payments
 *   6.  Pharmacy Management
 *   7.  Laboratory Management
 *   8.  Ward / Bed Management
 *   9.  Staff Management
 *   10. Reports & Analytics
 *   11. User Authentication
 */
public class Main {

    static final Scanner sc = new Scanner(System.in);

    // ── Service singletons ──────────────────────────────────────
    static final AuthManager        auth     = AuthManager.getInstance();
    static final PatientService     patients = PatientService.getInstance();
    static final DoctorService      doctors  = DoctorService.getInstance();
    static final AppointmentService appts    = AppointmentService.getInstance();
    static final PharmacyService    pharmacy = PharmacyService.getInstance();
    static final LabService         lab      = LabService.getInstance();
    static final WardService        wards    = WardService.getInstance();
    static final BillingService     billing  = BillingService.getInstance();
    static final StaffService       staff    = StaffService.getInstance();
    static final ReportService      reports  = ReportService.getInstance();

    // ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        banner();
        loginLoop();

        boolean running = true;
        while (running) {
            mainMenu();
            int choice = readInt("  Choice: ");
            switch (choice) {
                case 1  -> modulePatientRegistration();
                case 2  -> modulePatientRecords();
                case 3  -> moduleAppointments();
                case 4  -> moduleDoctors();
                case 5  -> moduleBilling();
                case 6  -> modulePharmacy();
                case 7  -> moduleLab();
                case 8  -> moduleWard();
                case 9  -> moduleStaff();
                case 10 -> moduleReports();
                case 11 -> moduleAuth();
                case 0  -> running = quit();
                default -> warn("Invalid choice. Select 0–11.");
            }
        }
        sc.close();
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 11 — Authentication (must be called first)
    // ════════════════════════════════════════════════════════════
    static void loginLoop() {
        System.out.println("\n  Please log in to continue.\n");
        while (!auth.isLoggedIn()) {
            System.out.print("  Username : "); String u = sc.nextLine().trim();
            System.out.print("  Password : "); String p = sc.nextLine().trim();
            try {
                User user = auth.login(u, p);
                ok("Logged in as " + user.getFullName() + " [" + user.getRole() + "]");
            } catch (AuthenticationException e) {
                warn(e.getMessage());
            }
        }
    }

    static void moduleAuth() {
        sectionHeader("USER AUTHENTICATION & MANAGEMENT");
        System.out.println("  1. Logout (switch user)");
        System.out.println("  2. Change my password");
        System.out.println("  3. View all users  [ADMIN]");
        System.out.println("  4. Add new user    [ADMIN]");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> { auth.logout(); ok("Logged out."); loginLoop(); }
            case 2 -> changePassword();
            case 3 -> { tryAdmin(); auth.printUsers(); }
            case 4 -> { tryAdmin(); addUser(); }
        }
    }

    static void changePassword() {
        System.out.print("  Current password : "); String old  = sc.nextLine().trim();
        System.out.print("  New password     : "); String nw   = sc.nextLine().trim();
        System.out.print("  Confirm password : "); String conf = sc.nextLine().trim();
        if (!nw.equals(conf)) { warn("Passwords do not match."); return; }
        if (!auth.getCurrentUser().checkPassword(old)) { warn("Current password is wrong."); return; }
        auth.getCurrentUser().changePassword(nw);
        ok("Password changed.");
    }

    static void addUser() {
        System.out.print("  Username : "); String u = sc.nextLine().trim();
        System.out.print("  Password : "); String p = sc.nextLine().trim();
        System.out.print("  Full Name: "); String n = sc.nextLine().trim();
        System.out.println("  Role (1=ADMIN 2=DOCTOR 3=NURSE 4=RECEPTIONIST): ");
        int r = readInt("  Choice: ");
        Role role = switch (r) {
            case 1 -> Role.ADMIN;
            case 2 -> Role.DOCTOR;
            case 3 -> Role.NURSE;
            default -> Role.RECEPTIONIST;
        };
        auth.addUser(new User(u, p, role, n));
        ok("User created: " + u + " [" + role + "]");
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 1 — Patient Registration
    // ════════════════════════════════════════════════════════════
    static void modulePatientRegistration() {
        sectionHeader("PATIENT REGISTRATION");
        System.out.println("  1. Register new patient");
        System.out.println("  2. Search patient");
        System.out.println("  3. View all patients");
        System.out.println("  4. Validate patient ID format");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> registerPatient();
            case 2 -> searchPatient();
            case 3 -> patients.printAll();
            case 4 -> validateId();
        }
    }

    static void registerPatient() {
        System.out.print("  Full Name          : "); String name = sc.nextLine().trim();
        int age = readInt("  Age                : ");
        System.out.print("  Gender (M/F/Other) : "); Gender g = Gender.parse(sc.nextLine().trim());
        System.out.print("  Phone              : "); String phone = sc.nextLine().trim();
        System.out.print("  Address            : "); String addr  = sc.nextLine().trim();
        System.out.print("  Emergency Contact  : "); String ec    = sc.nextLine().trim();
        PatientIdGenerator.printCountryCodes();
        System.out.print("  Country Code       : "); String cc = sc.nextLine().trim().toUpperCase();
        try {
            Patient p = patients.register(name, age, g, phone, addr, ec, cc);
            ok("Patient registered! ID: " + p.getPatientId());
            p.printSummary();
            // Auto-create bill
            billing.createBill(p.getPatientId());
        } catch (Exception e) { warn(e.getMessage()); }
    }

    static void searchPatient() {
        System.out.print("  Search (name/ID/phone): "); String kw = sc.nextLine().trim();
        List<Patient> results = patients.search(kw);
        if (results.isEmpty()) warn("No matches found.");
        else results.forEach(Patient::printSummary);
    }

    static void validateId() {
        System.out.print("  Enter patient ID: "); String id = sc.nextLine().trim().toUpperCase();
        try { PatientIdGenerator.validate(id); ok("ID \"" + id + "\" is valid."); }
        catch (InvalidPatientIdException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 2 — Patient Records
    // ════════════════════════════════════════════════════════════
    static void modulePatientRecords() {
        sectionHeader("PATIENT RECORDS");
        System.out.print("  Patient ID: "); String id = sc.nextLine().trim().toUpperCase();
        try {
            Patient p = patients.find(id);
            System.out.println("\n  1. View full record");
            System.out.println("  2. Add medical condition");
            System.out.println("  3. Add allergy");
            System.out.println("  4. Add visit note");
            System.out.println("  5. View lab results for this patient");
            System.out.println("  6. View prescriptions");
            System.out.println("  0. Back");
            int c = readInt("  Choice: ");
            switch (c) {
                case 1 -> p.printFullRecord();
                case 2 -> { System.out.print("  Condition: "); p.addCondition(sc.nextLine().trim()); ok("Added."); }
                case 3 -> { System.out.print("  Allergy  : "); p.addAllergy(sc.nextLine().trim());   ok("Added."); }
                case 4 -> {
                    System.out.print("  Doctor ID : "); String did = sc.nextLine().trim();
                    System.out.print("  Notes     : "); String notes = sc.nextLine().trim();
                    p.addVisit(new Visit(did, notes)); ok("Visit recorded.");
                }
                case 5 -> lab.forPatient(id).forEach(t -> System.out.println("  • " + t));
                case 6 -> p.getPrescriptions().forEach(pr -> System.out.println("  • " + pr));
            }
        } catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 3 — Appointment Management
    // ════════════════════════════════════════════════════════════
    static void moduleAppointments() {
        sectionHeader("APPOINTMENT MANAGEMENT");
        System.out.println("  1. Book appointment");
        System.out.println("  2. Reschedule appointment");
        System.out.println("  3. Cancel appointment");
        System.out.println("  4. Mark appointment complete");
        System.out.println("  5. View all appointments");
        System.out.println("  6. View doctor schedule");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> bookAppointment();
            case 2 -> rescheduleAppointment();
            case 3 -> cancelAppointment();
            case 4 -> completeAppointment();
            case 5 -> appts.printAll();
            case 6 -> viewDoctorSchedule();
        }
    }

    static void bookAppointment() {
        System.out.print("  Patient ID   : "); String pid = sc.nextLine().trim().toUpperCase();
        System.out.print("  Doctor ID    : "); String did = sc.nextLine().trim().toUpperCase();
        System.out.print("  Date & Time (yyyy-MM-dd HH:mm): "); String dtStr = sc.nextLine().trim();
        System.out.print("  Reason       : "); String reason = sc.nextLine().trim();
        try {
            LocalDateTime dt = LocalDateTime.parse(dtStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Appointment a = appts.book(pid, did, dt, reason);
            // Auto-assign patient to doctor
            Doctor doc = doctors.find(did);
            doc.assignPatient(pid);
            ok("Appointment booked: " + a.getApptId());
        } catch (DateTimeParseException e) { warn("Invalid date format. Use: yyyy-MM-dd HH:mm"); }
        catch (RecordNotFoundException e)  { warn(e.getMessage()); }
    }

    static void rescheduleAppointment() {
        System.out.print("  Appointment ID : "); String id = sc.nextLine().trim().toUpperCase();
        System.out.print("  New Date & Time (yyyy-MM-dd HH:mm): "); String dtStr = sc.nextLine().trim();
        try {
            LocalDateTime dt = LocalDateTime.parse(dtStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            appts.reschedule(id, dt);
            ok("Appointment " + id + " rescheduled.");
        } catch (DateTimeParseException e) { warn("Invalid date format."); }
        catch (RecordNotFoundException e)  { warn(e.getMessage()); }
    }

    static void cancelAppointment() {
        System.out.print("  Appointment ID: "); String id = sc.nextLine().trim().toUpperCase();
        try { appts.cancel(id); ok("Appointment " + id + " cancelled."); }
        catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void completeAppointment() {
        System.out.print("  Appointment ID: "); String id = sc.nextLine().trim().toUpperCase();
        try { appts.complete(id); ok("Appointment " + id + " marked complete."); }
        catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void viewDoctorSchedule() {
        System.out.print("  Doctor ID: "); String did = sc.nextLine().trim().toUpperCase();
        try { doctors.printSchedule(did, appts.all()); }
        catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 4 — Doctor Management
    // ════════════════════════════════════════════════════════════
    static void moduleDoctors() {
        sectionHeader("DOCTOR MANAGEMENT");
        System.out.println("  1. View all doctors");
        System.out.println("  2. Add new doctor  [ADMIN]");
        System.out.println("  3. View assigned patients for a doctor");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> doctors.printAll();
            case 2 -> { tryAdmin(); addDoctor(); }
            case 3 -> viewDoctorPatients();
        }
    }

    static void addDoctor() {
        System.out.print("  Full Name       : "); String name = sc.nextLine().trim();
        System.out.print("  Specialization  : "); String spec = sc.nextLine().trim();
        System.out.print("  Phone           : "); String ph   = sc.nextLine().trim();
        System.out.print("  Email           : "); String em   = sc.nextLine().trim();
        System.out.print("  Working Hours   : "); String wh   = sc.nextLine().trim();
        Doctor d = doctors.addDoctor(name, spec, ph, em, wh);
        ok("Doctor added: " + d.getDoctorId());
    }

    static void viewDoctorPatients() {
        System.out.print("  Doctor ID: "); String did = sc.nextLine().trim().toUpperCase();
        try {
            Doctor d = doctors.find(did);
            System.out.println("  Patients assigned to " + d.getName() + ":");
            if (d.getAssignedPatientIds().isEmpty()) { System.out.println("  None."); return; }
            d.getAssignedPatientIds().forEach(pid -> {
                try { patients.find(pid).printSummary(); }
                catch (RecordNotFoundException ignored) { System.out.println("  • " + pid + " (record not found)"); }
            });
        } catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 5 — Billing & Payments
    // ════════════════════════════════════════════════════════════
    static void moduleBilling() {
        sectionHeader("BILLING AND PAYMENTS");
        System.out.println("  1. View / print patient bill");
        System.out.println("  2. Add charge to bill");
        System.out.println("  3. Record payment");
        System.out.println("  4. Set insurance info");
        System.out.println("  5. View all bills");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> printPatientBill();
            case 2 -> addChargeToBill();
            case 3 -> recordPayment();
            case 4 -> setInsurance();
            case 5 -> billing.printAll();
        }
    }

    static void printPatientBill() {
        System.out.print("  Bill ID (or Patient ID to find): "); String id = sc.nextLine().trim().toUpperCase();
        try { billing.find(id).printReceipt(); }
        catch (RecordNotFoundException e) {
            // Try finding by patient
            Bill b = billing.findOrCreate(id);
            b.printReceipt();
        }
    }

    static void addChargeToBill() {
        System.out.print("  Patient ID  : "); String pid  = sc.nextLine().trim().toUpperCase();
        System.out.print("  Description : "); String desc = sc.nextLine().trim();
        double amount = readDouble("  Amount ($)  : ");
        Bill b = billing.findOrCreate(pid);
        b.addCharge(desc, amount);
        ok("Charge added. New total: $" + String.format("%.2f", b.getTotal()));
    }

    static void recordPayment() {
        System.out.print("  Bill ID    : "); String bid = sc.nextLine().trim().toUpperCase();
        double amount = readDouble("  Amount ($) : ");
        try { billing.recordPayment(bid, amount); ok("Payment of $" + String.format("%.2f", amount) + " recorded."); }
        catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void setInsurance() {
        System.out.print("  Patient ID      : "); String pid  = sc.nextLine().trim().toUpperCase();
        System.out.print("  Insurance Info  : "); String info = sc.nextLine().trim();
        billing.findOrCreate(pid).setInsurance(info);
        ok("Insurance info updated.");
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 6 — Pharmacy
    // ════════════════════════════════════════════════════════════
    static void modulePharmacy() {
        sectionHeader("PHARMACY MANAGEMENT");
        System.out.println("  1. View medicine inventory");
        System.out.println("  2. Add new medicine       [ADMIN]");
        System.out.println("  3. Sell / dispense medicine");
        System.out.println("  4. Dispense prescription");
        System.out.println("  5. Restock medicine       [ADMIN]");
        System.out.println("  6. Low stock alerts");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> pharmacy.printInventory();
            case 2 -> { tryAdmin(); addMedicine(); }
            case 3 -> sellMedicine();
            case 4 -> dispensePrescription();
            case 5 -> { tryAdmin(); restockMedicine(); }
            case 6 -> pharmacy.printLowStockAlerts();
        }
    }

    static void addMedicine() {
        System.out.print("  Name        : "); String name = sc.nextLine().trim();
        System.out.print("  Category    : "); String cat  = sc.nextLine().trim();
        int    qty   = readInt("  Quantity    : ");
        int    rl    = readInt("  Reorder Lv  : ");
        double price = readDouble("  Unit Price  : ");
        Medicine m = pharmacy.addMedicine(name, cat, qty, rl, price);
        ok("Medicine added: " + m.getMedicineId());
    }

    static void sellMedicine() {
        pharmacy.printInventory();
        System.out.print("  Medicine ID : "); String mid = sc.nextLine().trim().toUpperCase();
        int qty = readInt("  Quantity    : ");
        System.out.print("  Patient ID  : "); String pid = sc.nextLine().trim().toUpperCase();
        try {
            double cost = pharmacy.sell(mid, qty);
            billing.findOrCreate(pid).addCharge("Pharmacy: " + mid + " x" + qty, cost);
            ok(String.format("Sold. Cost: $%.2f — added to patient bill.", cost));
        } catch (Exception e) { warn(e.getMessage()); }
    }

    static void dispensePrescription() {
        System.out.print("  Patient ID : "); String pid = sc.nextLine().trim().toUpperCase();
        try {
            Patient p = patients.find(pid);
            if (p.getPrescriptions().isEmpty()) { System.out.println("  No prescriptions for this patient."); return; }
            System.out.println("  Prescriptions:");
            p.getPrescriptions().forEach(pr -> System.out.println("  • " + pr));
            System.out.print("  Rx ID to dispense: "); String rxId = sc.nextLine().trim().toUpperCase();
            p.getPrescriptions().stream()
                .filter(pr -> pr.getRxId().equals(rxId))
                .findFirst()
                .ifPresentOrElse(pr -> { pr.setDispensed(true); ok("Prescription " + rxId + " dispensed."); },
                                 () -> warn("Prescription not found: " + rxId));
        } catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void restockMedicine() {
        pharmacy.printInventory();
        System.out.print("  Medicine ID : "); String mid = sc.nextLine().trim().toUpperCase();
        int qty = readInt("  Add Quantity: ");
        try {
            Medicine m = pharmacy.find(mid);
            m.setQuantity(m.getQuantityInStock() + qty);
            ok("Restocked. New qty: " + m.getQuantityInStock());
        } catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 7 — Laboratory
    // ════════════════════════════════════════════════════════════
    static void moduleLab() {
        sectionHeader("LABORATORY MANAGEMENT");
        System.out.println("  1. Request lab test");
        System.out.println("  2. Enter test result");
        System.out.println("  3. View all tests");
        System.out.println("  4. Print test report");
        System.out.println("  5. View tests for a patient");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> requestLabTest();
            case 2 -> enterLabResult();
            case 3 -> lab.printAll();
            case 4 -> printLabReport();
            case 5 -> viewPatientTests();
        }
    }

    static void requestLabTest() {
        System.out.print("  Patient ID  : "); String pid  = sc.nextLine().trim().toUpperCase();
        System.out.print("  Test Name   : "); String test = sc.nextLine().trim();
        System.out.print("  Doctor ID   : "); String did  = sc.nextLine().trim().toUpperCase();
        LabTest t = lab.requestTest(pid, test, did);
        billing.findOrCreate(pid).addCharge("Lab: " + test, 25.00);
        ok("Lab test requested: " + t.getTestId() + ". $25 added to bill.");
    }

    static void enterLabResult() {
        System.out.print("  Test ID  : "); String id     = sc.nextLine().trim().toUpperCase();
        System.out.print("  Result   : "); String result = sc.nextLine().trim();
        try {
            lab.enterResult(id, result);
            // Also store in patient record
            LabTest t = lab.find(id);
            patients.find(t.getPatientId()).addTestResult(t.getTestName() + ": " + result);
            ok("Result entered for " + id);
        } catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void printLabReport() {
        System.out.print("  Test ID: "); String id = sc.nextLine().trim().toUpperCase();
        try { lab.printReport(id); }
        catch (RecordNotFoundException e) { warn(e.getMessage()); }
    }

    static void viewPatientTests() {
        System.out.print("  Patient ID: "); String pid = sc.nextLine().trim().toUpperCase();
        lab.forPatient(pid).forEach(t -> System.out.println("  • " + t));
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 8 — Ward / Bed Management
    // ════════════════════════════════════════════════════════════
    static void moduleWard() {
        sectionHeader("WARD / BED MANAGEMENT");
        System.out.println("  1. View all beds");
        System.out.println("  2. Admit patient to bed");
        System.out.println("  3. Discharge patient");
        System.out.println("  4. Transfer patient");
        System.out.println("  5. Find available bed in a ward");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> wards.printBeds();
            case 2 -> admitToBed();
            case 3 -> dischargeFromBed();
            case 4 -> transferPatient();
            case 5 -> findAvailableBed();
        }
    }

    static void admitToBed() {
        wards.printBeds();
        System.out.print("  Bed ID     : "); String bid = sc.nextLine().trim().toUpperCase();
        System.out.print("  Patient ID : "); String pid = sc.nextLine().trim().toUpperCase();
        try {
            wards.admit(bid, pid);
            patients.find(pid).setWardBedId(bid);
            billing.findOrCreate(pid).addCharge("Ward admission: " + bid, 100.00);
            ok("Patient " + pid + " admitted to bed " + bid + ". $100 added to bill.");
        } catch (Exception e) { warn(e.getMessage()); }
    }

    static void dischargeFromBed() {
        System.out.print("  Bed ID: "); String bid = sc.nextLine().trim().toUpperCase();
        try {
            Bed b = wards.find(bid);
            String pid = b.getOccupantId();
            wards.discharge(bid);
            if (pid != null) patients.find(pid).setWardBedId(null);
            ok("Bed " + bid + " discharged.");
        } catch (Exception e) { warn(e.getMessage()); }
    }

    static void transferPatient() {
        System.out.print("  From Bed ID : "); String from = sc.nextLine().trim().toUpperCase();
        System.out.print("  To Bed ID   : "); String to   = sc.nextLine().trim().toUpperCase();
        try {
            String pid = wards.find(from).getOccupantId();
            wards.transfer(from, to);
            if (pid != null) patients.find(pid).setWardBedId(to);
            ok("Patient transferred from " + from + " to " + to);
        } catch (Exception e) { warn(e.getMessage()); }
    }

    static void findAvailableBed() {
        System.out.print("  Ward name: "); String w = sc.nextLine().trim();
        try { Bed b = wards.findAvailableIn(w); ok("Available bed: " + b.getBedId() + " in " + b.getWard()); }
        catch (BedUnavailableException e) { warn(e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 9 — Staff Management
    // ════════════════════════════════════════════════════════════
    static void moduleStaff() {
        sectionHeader("STAFF MANAGEMENT");
        System.out.println("  1. View all staff");
        System.out.println("  2. Add staff member  [ADMIN]");
        System.out.println("  3. View by role");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> staff.printAll();
            case 2 -> { tryAdmin(); addStaff(); }
            case 3 -> viewStaffByRole();
        }
    }

    static void addStaff() {
        System.out.print("  Name  : "); String n = sc.nextLine().trim();
        System.out.print("  Phone : "); String p = sc.nextLine().trim();
        System.out.print("  Shift : "); String s = sc.nextLine().trim();
        System.out.println("  Role (3=NURSE, 4=RECEPTIONIST): ");
        int r = readInt("  Choice: ");
        Role role = (r == 3) ? Role.NURSE : Role.RECEPTIONIST;
        Staff newStaff = staff.addStaff(n, role, p, s);
        ok("Staff added: " + newStaff.getStaffId());
    }

    static void viewStaffByRole() {
        System.out.println("  1=ADMIN  2=DOCTOR  3=NURSE  4=RECEPTIONIST");
        int r = readInt("  Choice: ");
        Role role = switch (r) {
            case 1 -> Role.ADMIN;
            case 2 -> Role.DOCTOR;
            case 3 -> Role.NURSE;
            default -> Role.RECEPTIONIST;
        };
        List<Staff> list = staff.byRole(role);
        if (list.isEmpty()) warn("No staff with role " + role);
        else list.forEach(Staff::printInfo);
    }

    // ════════════════════════════════════════════════════════════
    //  MODULE 10 — Reports & Analytics
    // ════════════════════════════════════════════════════════════
    static void moduleReports() {
        sectionHeader("REPORTS AND ANALYTICS");
        System.out.println("  1. Daily patients count");
        System.out.println("  2. Revenue report");
        System.out.println("  3. Doctor performance");
        System.out.println("  4. Medicine usage / stock");
        System.out.println("  5. Ward / bed status");
        System.out.println("  0. Back");
        int c = readInt("  Choice: ");
        switch (c) {
            case 1 -> reports.dailyPatientsReport();
            case 2 -> reports.revenueReport();
            case 3 -> reports.doctorPerformanceReport();
            case 4 -> reports.medicineUsageReport();
            case 5 -> reports.wardReport();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════
    static void banner() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║        SMART HOSPITAL MANAGEMENT SYSTEM  v2.0               ║");
        System.out.println("║        Aptech Java OOP Assignment — Full Edition             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    static void mainMenu() {
        System.out.println("\n╔══════════════════════ MAIN MENU ══════════════════════╗");
        System.out.printf ("║  Logged in: %-20s Role: %-12s ║%n",
            auth.getCurrentUser().getFullName(), auth.getCurrentUser().getRole());
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║  1.  Patient Registration      7.  Lab Management     ║");
        System.out.println("║  2.  Patient Records           8.  Ward Management    ║");
        System.out.println("║  3.  Appointments              9.  Staff Management   ║");
        System.out.println("║  4.  Doctor Management         10. Reports            ║");
        System.out.println("║  5.  Billing & Payments        11. Auth & Users       ║");
        System.out.println("║  6.  Pharmacy                  0.  Exit               ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    static void sectionHeader(String title) {
        System.out.println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 52 - title.length())) + "╗");
        System.out.println();
    }

    static void ok(String msg)   { System.out.println("\n  ✔  " + msg + "\n"); }
    static void warn(String msg) { System.out.println("\n  ⚠  " + msg + "\n"); }

    static boolean quit() {
        System.out.println("\n  Thank you for using the Smart Hospital Management System. Goodbye!\n");
        return false;
    }

    static void tryAdmin() {
        try { auth.requireRole(Role.ADMIN); }
        catch (AuthenticationException e) { warn(e.getMessage()); throw new RuntimeException("Access denied"); }
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Please enter a number."); }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid number."); }
        }
    }
}
