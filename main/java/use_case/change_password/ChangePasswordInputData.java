package use_case.change_password;

/**
 * The input data for the Change Password Use Case.
 */
public class ChangePasswordInputData {

    private final String password;
    private final String username;
    private final boolean admin;

    public ChangePasswordInputData(String password, String username, boolean admin) {
        this.password = password;
        this.username = username;
        this.admin = admin;
    }

    // Backward-compatible constructor (defaults admin to false)
    public ChangePasswordInputData(String password, String username) {
        this(password, username, false);
    }

    String getPassword() {
        return password;
    }

    String getUsername() {
        return username;
    }

    boolean getAdmin() {
        return admin;
    }

}
