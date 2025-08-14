package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI tests for {@link LoginView}.
 *
 * Notes:
 * - No Mockito is used (works on any JDK). We use tiny fakes/subclasses to capture calls.
 * - Tests run Swing creation on the EDT to avoid UI thread issues.
 */
public class LoginViewTest {

    private LoginView view;
    private LoginViewModel loginViewModel;
    private CapturingViewManagerModel viewManagerModel;
    private CapturingLoginController loginController;

    @BeforeEach
    void setUp() throws Exception {
        loginViewModel = new LoginViewModel();
        viewManagerModel = new CapturingViewManagerModel();
        loginController = new CapturingLoginController();

        // Build the Swing UI on the EDT
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            view = new LoginView(loginViewModel, viewManagerModel);
            view.setLoginController(loginController);
            latch.countDown();
        });
        // Wait until UI is created
        latch.await();
    }

    // ---------- Helpers ----------

    /** Recursively collect all components of the given type under a container. */
    private static <T> List<T> findAll(Container root, Class<T> type) {
        List<T> acc = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                acc.add(type.cast(c));
            }
            if (c instanceof Container) {
                acc.addAll(findAll((Container) c, type));
            }
        }
        return acc;
    }

    /** Find a JButton by its text. Throws if not found. */
    private static JButton findButtonByText(Container root, String text) {
        for (JButton b : findAll(root, JButton.class)) {
            if (text.equals(b.getText())) return b;
        }
        fail("Button with text '" + text + "' not found");
        return null;
    }

    /** Find the username JTextField (not the JPasswordField). */
    private static JTextField findUsernameField(Container root) {
        for (JTextField tf : findAll(root, JTextField.class)) {
            if (!(tf instanceof JPasswordField)) {
                return tf;
            }
        }
        fail("Username JTextField not found");
        return null;
    }

    /** Find the password JPasswordField. */
    private static JPasswordField findPasswordField(Container root) {
        for (JPasswordField pf : findAll(root, JPasswordField.class)) {
            return pf;
        }
        fail("Password JPasswordField not found");
        return null;
    }

    // ---------- Tests ----------

    @Test
    void getViewName_is_log_in() {
        assertEquals("log in", view.getViewName());
    }

    @Test
    void typing_updates_LoginViewModel_state() throws Exception {
        JTextField usernameField = findUsernameField(view);
        JPasswordField passwordField = findPasswordField(view);

        // Simulate user typing
        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText("alice");
            passwordField.setText("s3cret");
        });

        LoginState state = loginViewModel.getState();
        assertEquals("alice", state.getUsername(), "Username in state should reflect text field");
        assertEquals("s3cret", state.getPassword(), "Password in state should reflect password field");
    }

    @Test
    void clicking_login_calls_controller_with_state_values() throws Exception {
        // Prepare model state
        LoginState s = loginViewModel.getState();
        s.setUsername("bob");
        s.setPassword("pw123");
        s.setAdmin(true);
        loginViewModel.setState(s); // keep model consistent

        JButton logIn = findButtonByText(view, "Log In");

        SwingUtilities.invokeAndWait(logIn::doClick);

        assertEquals("bob", loginController.capturedUsername);
        assertEquals("pw123", loginController.capturedPassword);
        assertTrue(loginController.capturedAdmin);
    }

    @Test
    void clicking_cancel_pushes_sign_up_view() throws Exception {
        JButton cancel = findButtonByText(view, "Cancel");

        SwingUtilities.invokeAndWait(cancel::doClick);

        assertEquals("sign up", viewManagerModel.lastPushedView);
    }

    @Test
    void propertyChange_populates_fields_and_error_label() throws Exception {
        // Prepare a new state to feed via propertyChange
        LoginState newState = new LoginState();
        newState.setUsername("charlie");
        newState.setPassword("letmein");
        newState.setLoginError("Invalid credentials");

        // Fire the PropertyChange directly (equivalent to ViewModel notifying)
        SwingUtilities.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(loginViewModel, "state", null, newState)));

        // Verify text fields reflect state
        JTextField usernameField = findUsernameField(view);
        JPasswordField passwordField = findPasswordField(view);
        assertEquals("charlie", usernameField.getText());
        assertEquals("letmein", new String(passwordField.getPassword()));

        // Verify error label updated (search by label text)
        boolean foundError = false;
        for (JLabel lbl : findAll(view, JLabel.class)) {
            if ("Invalid credentials".equals(lbl.getText())) {
                foundError = true;
                break;
            }
        }
        assertTrue(foundError, "An error JLabel with the expected text should be present");
    }

    // ---------- Tiny fakes ----------

    /** Captures calls to pushView without needing any external framework. */
    private static class CapturingViewManagerModel extends ViewManagerModel {
        String lastPushedView;
        @Override
        public void pushView(String viewName) {
            lastPushedView = viewName;
            super.pushView(viewName);
        }
    }

    /** Subclass of LoginController overriding execute to capture arguments. */
    private static class CapturingLoginController extends LoginController {
        String capturedUsername;
        String capturedPassword;
        boolean capturedAdmin;

        CapturingLoginController() {
            super(null); // super's dependency is unused because we override execute
        }

        @Override
        public void execute(String username, String password, boolean admin) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.capturedAdmin = admin;
        }
    }
}
