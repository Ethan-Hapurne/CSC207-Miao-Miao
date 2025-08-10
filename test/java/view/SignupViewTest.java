package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SignupView.
 * We avoid JOptionPane branches to keep tests headless and deterministic.
 */
class SignupViewTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private SignupViewModel vm;
    private ViewManagerModel nav;
    private SignupController controller;
    private SignupState state;
    private SignupView view;

    @BeforeEach
    void setup() throws Exception {
        vm = mock(SignupViewModel.class);
        nav = mock(ViewManagerModel.class);
        controller = mock(SignupController.class);

        state = new SignupState(); // assume no-arg constructor
        when(vm.getState()).thenReturn(state);

        SwingUtilities.invokeAndWait(() -> {
            view = new SignupView(vm, nav);
            view.setSignupController(controller);
        });
    }

    // helpers
    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && ((JButton) c).getText().equals(text)) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }
    private JTextField findFirstPlainTextField(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField && !(c instanceof JPasswordField)) return (JTextField) c;
            if (c instanceof Container) {
                JTextField f = findFirstPlainTextField((Container) c);
                if (f != null) return f;
            }
        }
        return null;
    }

    @Test
    void backToLogin_callsControllerSwitch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton btn = findButton(view, "Back to Login");
            assertNotNull(btn);
            btn.doClick();
        });
        verify(controller, times(1)).switchToLoginView();
    }

    @Test
    void cancel_popsViewOrClose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton btn = findButton(view, "Cancel");
            assertNotNull(btn);
            btn.doClick();
        });
        verify(nav, times(1)).popViewOrClose();
    }

    @Test
    void typingUsername_updatesViewModelState() throws Exception {
        ArgumentCaptor<SignupState> captor = ArgumentCaptor.forClass(SignupState.class);
        SwingUtilities.invokeAndWait(() -> {
            JTextField tf = findFirstPlainTextField(view);
            assertNotNull(tf);
            tf.setText("newuser");
        });
        verify(vm, atLeastOnce()).setState(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
    }
}
