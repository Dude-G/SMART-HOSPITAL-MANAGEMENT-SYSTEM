import java.util.*;

// ─────────────────────────────────────────────────────────────
//  User — represents a system user with role-based access
// ─────────────────────────────────────────────────────────────
class User {
    private final String username;
    private String passwordHash;   // simple hash for student demo
    private final Role role;
    private final String fullName;
    private boolean active;

    public User(String username, String password, Role role, String fullName) {
        this.username     = username;
        this.passwordHash = hashPassword(password);
        this.role         = role;
        this.fullName     = fullName;
        this.active       = true;
    }

    /** Very simple hash — adequate for a student OOP project. */
    private String hashPassword(String raw) {
        return Integer.toHexString(Objects.hash(raw, "HMS_SALT_2024"));
    }

    public boolean checkPassword(String raw) {
        return passwordHash.equals(hashPassword(raw));
    }

    public void changePassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    public String  getUsername() { return username; }
    public Role    getRole()     { return role; }
    public String  getFullName() { return fullName; }
    public boolean isActive()    { return active; }
    public void    setActive(boolean a) { this.active = a; }

    @Override
    public String toString() {
        return String.format("  %-15s %-20s %-15s %s",
                username, fullName, role, active ? "Active" : "Inactive");
    }
}

// ─────────────────────────────────────────────────────────────
//  AuthManager — login / session / user management
// ─────────────────────────────────────────────────────────────
public class AuthManager {

    private static AuthManager instance;
    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    private AuthManager() {
        // Seed default accounts
        addUser(new User("admin",        "Admin@123",   Role.ADMIN,        "System Administrator"));
        addUser(new User("dr.chidi",     "Doctor@123",  Role.DOCTOR,       "Dr. Chidi Okeke"));
        addUser(new User("dr.fatima",    "Doctor@123",  Role.DOCTOR,       "Dr. Fatima Al-Hassan"));
        addUser(new User("nurse.amara",  "Nurse@123",   Role.NURSE,        "Nurse Amara Diallo"));
        addUser(new User("receptionist", "Recep@123",   Role.RECEPTIONIST, "Blessing Osei"));
    }

    public static AuthManager getInstance() {
        if (instance == null) instance = new AuthManager();
        return instance;
    }

    public void addUser(User u) { users.put(u.getUsername(), u); }

    /** Attempts login; returns the authenticated User or throws. */
    public User login(String username, String password) throws AuthenticationException {
        User u = users.get(username);
        if (u == null || !u.isActive())
            throw new AuthenticationException("User \"" + username + "\" not found or inactive.");
        if (!u.checkPassword(password))
            throw new AuthenticationException("Incorrect password.");
        currentUser = u;
        return u;
    }

    public void logout() { currentUser = null; }

    public User getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() { return currentUser != null; }

    public void requireRole(Role... roles) throws AuthenticationException {
        if (currentUser == null)
            throw new AuthenticationException("Not logged in.");
        for (Role r : roles) if (currentUser.getRole() == r) return;
        throw new AuthenticationException(
            "Access denied. Required role(s): " + Arrays.toString(roles)
            + ". Your role: " + currentUser.getRole());
    }

    public Collection<User> getAllUsers() { return users.values(); }

    public void printUsers() {
        System.out.println("\n  ┌─────────────────────────────────────────────────────────┐");
        System.out.println("  │                    SYSTEM USERS                         │");
        System.out.println("  ├───────────────┬────────────────────┬───────────┬────────┤");
        System.out.printf("  │ %-13s │ %-18s │ %-9s │ %-6s │%n","Username","Full Name","Role","Status");
        System.out.println("  ├───────────────┼────────────────────┼───────────┼────────┤");
        for (User u : users.values())
            System.out.printf("  │ %-13s │ %-18s │ %-9s │ %-6s │%n",
                u.getUsername(), u.getFullName(), u.getRole(),
                u.isActive() ? "Active" : "Off");
        System.out.println("  └───────────────┴────────────────────┴───────────┴────────┘");
    }
}
