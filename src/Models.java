import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────
//  Visit
// ─────────────────────────────────────────────────────────────
class Visit {
    private final LocalDate date;
    private final String    doctorId;
    private final String    notes;

    public Visit(String doctorId, String notes) {
        this.date     = LocalDate.now();
        this.doctorId = doctorId;
        this.notes    = notes;
    }

    public LocalDate getDate()     { return date; }
    public String    getDoctorId() { return doctorId; }
    public String    getNotes()    { return notes; }

    @Override
    public String toString() {
        return date + " | Doctor: " + doctorId + " | " + notes;
    }
}

// ─────────────────────────────────────────────────────────────
//  Prescription
// ─────────────────────────────────────────────────────────────
class Prescription {
    private static int seq = 1;
    private final String     rxId;
    private final String     patientId;
    private final String     doctorId;
    private final String     medicine;
    private final String     dosage;
    private final String     duration;
    private final LocalDate  date;
    private       boolean    dispensed;

    public Prescription(String patientId, String doctorId,
                        String medicine, String dosage, String duration) {
        this.rxId      = "RX" + String.format("%04d", seq++);
        this.patientId = patientId;
        this.doctorId  = doctorId;
        this.medicine  = medicine;
        this.dosage    = dosage;
        this.duration  = duration;
        this.date      = LocalDate.now();
        this.dispensed = false;
    }

    public String  getRxId()      { return rxId; }
    public String  getPatientId() { return patientId; }
    public String  getDoctorId()  { return doctorId; }
    public String  getMedicine()  { return medicine; }
    public boolean isDispensed()  { return dispensed; }
    public void    setDispensed(boolean d) { dispensed = d; }

    @Override
    public String toString() {
        return rxId + " | " + medicine + " " + dosage + " for " + duration
             + " | Dr:" + doctorId + " | " + (dispensed ? "✔Dispensed" : "Pending");
    }
}

// ─────────────────────────────────────────────────────────────
//  Appointment
// ─────────────────────────────────────────────────────────────
class Appointment {
    private static int seq = 1;
    private final String            apptId;
    private final String            patientId;
    private final String            doctorId;
    private       LocalDateTime     dateTime;
    private       String            reason;
    private       AppointmentStatus status;

    public Appointment(String patientId, String doctorId,
                       LocalDateTime dateTime, String reason) {
        this.apptId    = "APT" + String.format("%04d", seq++);
        this.patientId = patientId;
        this.doctorId  = doctorId;
        this.dateTime  = dateTime;
        this.reason    = reason;
        this.status    = AppointmentStatus.SCHEDULED;
    }

    public String            getApptId()    { return apptId; }
    public String            getPatientId() { return patientId; }
    public String            getDoctorId()  { return doctorId; }
    public LocalDateTime     getDateTime()  { return dateTime; }
    public AppointmentStatus getStatus()    { return status; }
    public String            getReason()    { return reason; }

    public void setDateTime(LocalDateTime dt) { dateTime = dt; }
    public void setStatus(AppointmentStatus s) { status = s; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return apptId + " | Patient:" + patientId + " | Dr:" + doctorId
             + " | " + dateTime.format(fmt) + " | " + status + " | " + reason;
    }
}

// ─────────────────────────────────────────────────────────────
//  Doctor
// ─────────────────────────────────────────────────────────────
class Doctor {
    private final String       doctorId;
    private       String       name;
    private       String       specialization;
    private       String       phone;
    private       String       email;
    private       String       workingHours;   // e.g. "Mon-Fri 08:00-16:00"
    private final List<String> assignedPatientIds = new ArrayList<>();

    public Doctor(String doctorId, String name, String specialization,
                  String phone, String email, String workingHours) {
        this.doctorId      = doctorId;
        this.name          = name;
        this.specialization = specialization;
        this.phone         = phone;
        this.email         = email;
        this.workingHours  = workingHours;
    }

    public String       getDoctorId()        { return doctorId; }
    public String       getName()            { return name; }
    public String       getSpecialization()  { return specialization; }
    public String       getPhone()           { return phone; }
    public String       getWorkingHours()    { return workingHours; }
    public List<String> getAssignedPatientIds() { return assignedPatientIds; }

    public void setName(String n)            { name = n; }
    public void setSpecialization(String s)  { specialization = s; }
    public void setPhone(String p)           { phone = p; }
    public void setEmail(String e)           { email = e; }
    public void setWorkingHours(String h)    { workingHours = h; }

    public void assignPatient(String id)     { if (!assignedPatientIds.contains(id)) assignedPatientIds.add(id); }

    public void printInfo() {
        System.out.printf("  %-8s %-22s %-20s %-15s %-20s%n",
            doctorId, name, specialization, phone, workingHours);
    }
}

// ─────────────────────────────────────────────────────────────
//  Staff  (Nurse / Receptionist / Admin)
// ─────────────────────────────────────────────────────────────
class Staff {
    private static int seq = 1;
    private final String staffId;
    private       String name;
    private       Role   role;
    private       String phone;
    private       String shift;   // e.g. "Morning 07:00-15:00"

    public Staff(String name, Role role, String phone, String shift) {
        this.staffId = "STF" + String.format("%04d", seq++);
        this.name    = name;
        this.role    = role;
        this.phone   = phone;
        this.shift   = shift;
    }

    public String getStaffId() { return staffId; }
    public String getName()    { return name; }
    public Role   getRole()    { return role; }
    public String getPhone()   { return phone; }
    public String getShift()   { return shift; }

    public void setName(String n)  { name = n; }
    public void setPhone(String p) { phone = p; }
    public void setShift(String s) { shift = s; }

    public void printInfo() {
        System.out.printf("  %-8s %-22s %-15s %-15s %-22s%n",
            staffId, name, role, phone, shift);
    }
}

// ─────────────────────────────────────────────────────────────
//  Medicine
// ─────────────────────────────────────────────────────────────
class Medicine {
    private final String medicineId;
    private       String name;
    private       String category;
    private       int    quantityInStock;
    private       int    reorderLevel;    // alert if stock falls below this
    private       double unitPrice;

    public Medicine(String medicineId, String name, String category,
                    int qty, int reorderLevel, double unitPrice) {
        this.medicineId    = medicineId;
        this.name          = name;
        this.category      = category;
        this.quantityInStock = qty;
        this.reorderLevel  = reorderLevel;
        this.unitPrice     = unitPrice;
    }

    public String getMedicineId()      { return medicineId; }
    public String getName()            { return name; }
    public String getCategory()        { return category; }
    public int    getQuantityInStock()  { return quantityInStock; }
    public int    getReorderLevel()    { return reorderLevel; }
    public double getUnitPrice()       { return unitPrice; }

    public void setQuantity(int q)     { quantityInStock = q; }
    public void setUnitPrice(double p) { unitPrice = p; }

    public void deduct(int qty) throws InsufficientStockException {
        if (qty > quantityInStock)
            throw new InsufficientStockException(
                "Insufficient stock for " + name + ". Available: " + quantityInStock + ", Requested: " + qty);
        quantityInStock -= qty;
    }

    public boolean isLowStock() { return quantityInStock <= reorderLevel; }

    public void printInfo() {
        String alert = isLowStock() ? " ⚠ LOW" : "";
        System.out.printf("  %-8s %-22s %-15s %6d  %5d  $%8.2f%s%n",
            medicineId, name, category, quantityInStock, reorderLevel, unitPrice, alert);
    }
}

// ─────────────────────────────────────────────────────────────
//  LabTest
// ─────────────────────────────────────────────────────────────
class LabTest {
    private static int seq = 1;
    private final String     testId;
    private final String     patientId;
    private final String     testName;
    private final String     requestedBy;  // doctorId
    private final LocalDate  requestDate;
    private       TestStatus status;
    private       String     result;
    private       LocalDate  completedDate;

    public LabTest(String patientId, String testName, String requestedBy) {
        this.testId       = "LAB" + String.format("%04d", seq++);
        this.patientId    = patientId;
        this.testName     = testName;
        this.requestedBy  = requestedBy;
        this.requestDate  = LocalDate.now();
        this.status       = TestStatus.PENDING;
        this.result       = "";
    }

    public String     getTestId()      { return testId; }
    public String     getPatientId()   { return patientId; }
    public String     getTestName()    { return testName; }
    public TestStatus getStatus()      { return status; }
    public String     getResult()      { return result; }

    public void setStatus(TestStatus s)    { status = s; }
    public void setResult(String r) {
        result        = r;
        status        = TestStatus.COMPLETED;
        completedDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return testId + " | " + testName + " | Patient:" + patientId
             + " | Dr:" + requestedBy + " | " + requestDate + " | " + status
             + (result.isEmpty() ? "" : " | Result: " + result);
    }
}

// ─────────────────────────────────────────────────────────────
//  Bed
// ─────────────────────────────────────────────────────────────
class Bed {
    private final String    bedId;
    private final String    ward;
    private       BedStatus status;
    private       String    occupantPatientId;

    public Bed(String bedId, String ward) {
        this.bedId  = bedId;
        this.ward   = ward;
        this.status = BedStatus.AVAILABLE;
    }

    public String    getBedId()           { return bedId; }
    public String    getWard()            { return ward; }
    public BedStatus getStatus()          { return status; }
    public String    getOccupantId()      { return occupantPatientId; }

    public void admit(String patientId) throws BedUnavailableException {
        if (status != BedStatus.AVAILABLE)
            throw new BedUnavailableException("Bed " + bedId + " is not available (" + status + ").");
        occupantPatientId = patientId;
        status = BedStatus.OCCUPIED;
    }

    public void discharge() {
        occupantPatientId = null;
        status = BedStatus.AVAILABLE;
    }

    public void setMaintenance(boolean on) {
        status = on ? BedStatus.MAINTENANCE : BedStatus.AVAILABLE;
    }

    public void printInfo() {
        System.out.printf("  %-8s %-15s %-12s %s%n",
            bedId, ward, status,
            occupantPatientId == null ? "-" : "Patient: " + occupantPatientId);
    }
}

// ─────────────────────────────────────────────────────────────
//  Bill
// ─────────────────────────────────────────────────────────────
class Bill {
    private static int seq = 1;
    private final String        billId;
    private final String        patientId;
    private final List<String>  lineItems    = new ArrayList<>();
    private final List<Double>  amounts      = new ArrayList<>();
    private       double        totalAmount;
    private       double        paid;
    private       String        insuranceInfo;
    private       PaymentStatus paymentStatus;
    private final LocalDate     billDate;

    public Bill(String patientId) {
        this.billId        = "BILL" + String.format("%04d", seq++);
        this.patientId     = patientId;
        this.totalAmount   = 0.0;
        this.paid          = 0.0;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.billDate      = LocalDate.now();
    }

    public void addCharge(String description, double amount) {
        lineItems.add(description);
        amounts.add(amount);
        totalAmount += amount;
        refreshStatus();
    }

    public void recordPayment(double amount) {
        paid += amount;
        refreshStatus();
    }

    private void refreshStatus() {
        if (paid <= 0)              paymentStatus = PaymentStatus.UNPAID;
        else if (paid < totalAmount) paymentStatus = PaymentStatus.PARTIAL;
        else                         paymentStatus = PaymentStatus.PAID;
    }

    public String        getBillId()       { return billId; }
    public String        getPatientId()    { return patientId; }
    public double        getTotal()        { return totalAmount; }
    public double        getPaid()         { return paid; }
    public double        getOutstanding()  { return Math.max(0, totalAmount - paid); }
    public PaymentStatus getStatus()       { return paymentStatus; }
    public void          setInsurance(String info) { insuranceInfo = info; }

    public void printReceipt() {
        System.out.println("\n  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║          HOSPITAL MANAGEMENT SYSTEM          ║");
        System.out.println("  ║               PAYMENT RECEIPT                ║");
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  Bill ID   : %-31s║%n", billId);
        System.out.printf ("  ║  Patient   : %-31s║%n", patientId);
        System.out.printf ("  ║  Date      : %-31s║%n", billDate);
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  %-30s %11s ║%n", "Description", "Amount");
        System.out.println("  ║  ─────────────────────────────────────────  ║");
        for (int i = 0; i < lineItems.size(); i++)
            System.out.printf("  ║  %-30s  $%9.2f ║%n", lineItems.get(i), amounts.get(i));
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  %-30s  $%9.2f ║%n", "TOTAL",       totalAmount);
        System.out.printf ("  ║  %-30s  $%9.2f ║%n", "PAID",        paid);
        System.out.printf ("  ║  %-30s  $%9.2f ║%n", "OUTSTANDING", getOutstanding());
        System.out.printf ("  ║  %-30s  %-10s ║%n",  "STATUS",      paymentStatus);
        if (insuranceInfo != null)
            System.out.printf("  ║  Insurance : %-31s║%n", insuranceInfo);
        System.out.println("  ╚══════════════════════════════════════════════╝");
    }
}
