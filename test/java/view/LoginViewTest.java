package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoginView.
 * We avoid real windows and run in headless mode; all collaborators are mocked.
 */
class LoginViewTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private LoginViewModel loginVM;
    private ViewManagerModel nav;
    private LoginController controller;
    private LoginState state;
    private LoginView view;

    @BeforeEach
    void setup() throws Exception {
        loginVM = mock(LoginViewModel.class);
        nav = mock(ViewManagerModel.class);
        controller = mock(LoginController.class);

        // Assume LoginState has a no-arg constructor. If not, replace with a builder/real constructor.
        state = new LoginState();
        state.setUsername("alice");
        state.setPassword("pw123");
        state.setAdmin(false);
        when(loginVM.getState()).thenReturn(state);

        SwingUtilities.invokeAndWait(() -> {
            view = new LoginView(loginVM, nav);
            view.setLoginController(controller);
        });
    }

    // ------- helper queries over component tree -------
    private JButton findButtonByText(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && ((JButton) c).getText().equals(text)) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButtonByText((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }
    private JTextField findFirstUsernameField(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField && !(c instanceof JPasswordField)) return (JTextField) c;
            if (c instanceof Container) {
                JTextField f = findFirstUsernameField((Container) c);
                if (f != null) return f;
            }
        }
        return null;
    }
    private JPasswordField findPasswordField(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPasswordField) return (JPasswordField) c;
            if (c instanceof Container) {
                JPasswordField f = findPasswordField((Container) c);
                if (f != null) return f;
            }
        }
        return null;
    }
    // --------------------------------------------------

    @Test
    void clickSignIn_callsControllerWithCurrentState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton signIn = findButtonByText(view, "Sign In");
            assertNotNull(signIn);
            signIn.doClick();
        });
        verify(controller, times(1)).execute("alice", "pw123", false);
    }

    @Test
    void clickCancel_pushesToSignup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton cancel = findButtonByText(view, "Cancel");
            assertNotNull(cancel);
            cancel.doClick();
        });
        verify(nav, times(1)).pushView("sign up");
    }

    @Test
    void propertyChange_setsFieldsFromState() throws Exception {
        LoginState newState = new LoginState();
        newState.setUsername("bob");
        newState.setPassword("secret");
        newState.setAdmin(false);

        SwingUtilities.invokeAndWait(() -> {
            view.propertyChange(new java.beans.PropertyChangeEvent(this, "state", state, newState));
            JTextField user = findFirstUsernameField(view);
            JPasswordField pass = findPasswordField(view);
            assertEquals("bob", user.getText());
            assertEquals("secret", new String(pass.getPassword()));
        });
    }

    @Test
    void typingUsername_updatesViewModelState() throws Exception {
        ArgumentCaptor<LoginState> captor = ArgumentCaptor.forClass(LoginState.class);
        SwingUtilities.invokeAndWait(() -> {
            JTextField user = findFirstUsernameField(view);
            user.setText("charlie"); // triggers DocumentListener
        });
        verify(loginVM, atLeastOnce()).setState(captor.capture());
        assertEquals("charlie", captor.getValue().getUsername());
    }
}
