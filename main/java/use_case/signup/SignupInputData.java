package use_case.signup;

/**
 * The Input Data for the Signup Use Case.
 */
public class SignupInputData {

    private final String username;
    private final String password;
    private final String repeatPassword;
    private final boolean admin;
    // might have to handle the previous user wh doesnt have this state

    public SignupInputData(String username, String password, String repeatPassword, boolean admin) {
        this.username = username;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.admin = admin;
    }

    // Backward-compatible constructor (defaults admin to false)
    public SignupInputData(String username, String password, String repeatPassword) {
        this(username, password, repeatPassword, false);
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

    public String getRepeatPassword() {
        return repeatPassword;
    }
}
