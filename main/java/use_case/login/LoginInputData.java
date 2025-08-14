package use_case.login;

/**
 * The Input Data for the Login Use Case.
 */
public class LoginInputData {

    private final String username;
    private final String password;
    private final boolean admin;

    public LoginInputData(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    // Backward-compatible constructor (defaults admin to false)
    public LoginInputData(String username, String password) {
        this(username, password, false);
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    boolean getAdmin() {
        return admin;
    }

}
