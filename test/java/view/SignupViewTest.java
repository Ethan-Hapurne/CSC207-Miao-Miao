package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SignupView}.
 *
 * Notes:
 * - We do NOT use Mockito (ByteBuddy issues on JDK 24).
 * - We avoid clicking the "Sign Up" / "Back to Login" buttons because they
 *   require a real SignupController. Instead, we focus on view-model binding,
 *   admin code visibility toggling, and navigation via the Cancel button.
 * - We run UI creation on the EDT to be Swing-safe.
 */
public class SignupViewTest {

    private static final String ADMIN_CHECKBOX_TEXT = "Sign up as Admin";
    private static final String ADMIN_CODE_LABEL = "Admin Code";
    private static final String BTN_SIGN_UP = "Sign Up";
    private static final String BTN_BACK_TO_LOGIN = "Back to Login";
    private static final String BTN_CANCEL = "Cancel";

    /** A simple fake ViewManagerModel to observe navigation calls. */
    private static class FakeViewManagerModel extends ViewManagerModel {
        int popOrCloseCount = 0;
        @Override
        public void popViewOrClose() {
            popOrCloseCount++;
        }
    }

    private SignupViewModel viewModel;
    private FakeViewManagerModel viewManager;
    private SignupView view;

    @BeforeAll
    static void headless() {
        // Prevent accidental window popups during tests.
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws Exception {
        viewModel = new SignupViewModel();
        viewManager = new FakeViewManagerModel();

        // Create the view on the EDT.
        SwingUtilities.invokeAndWait(() -> {
            view = new SignupView(viewModel, viewManager);
        });
    }

    @Test
    void viewName_isSignUp() {
        assertEquals("sign up", view.getViewName(), "View name should be 'sign up'.");
    }

    @Test
    void typingUsernamePasswordRepeat_updatesViewModelState() throws Exception {
        // Find the three text/password fields by walking the component tree.
        final JTextField username = findTextFieldLabeled(view, "Username");
        final JPasswordField password = (JPasswordField) findTextFieldLabeled(view, "Password");
        final JPasswordField repeat = (JPasswordField) findTextFieldLabeled(view, "Repeat Password");

        assertNotNull(username, "Username field should exist.");
        assertNotNull(password, "Password field should exist.");
        assertNotNull(repeat, "Repeat Password field should exist.");

        // Type text on EDT so DocumentListeners fire.
        SwingUtilities.invokeAndWait(() -> {
            username.setText("alice");
            password.setText("P@ssw0rd!");
            repeat.setText("P@ssw0rd!");
        });

        final SignupState state = viewModel.getState();
        assertEquals("alice", state.getUsername(), "Username should sync to view model.");
        assertEquals("P@ssw0rd!", state.getPassword(), "Password should sync to view model.");
        assertEquals("P@ssw0rd!", state.getRepeatPassword(), "Repeat password should sync to view model.");
    }

    @Test
    void toggleAdminCheckbox_showsAndHidesAdminCodeField() throws Exception {
        final JCheckBox adminCheckbox = findCheckBoxByText(view, ADMIN_CHECKBOX_TEXT);
        assertNotNull(adminCheckbox, "Admin checkbox should exist.");

        // The panel for "Admin Code" is initially hidden.
        JPanel adminCodePanel = findInputPanelByLabel(view, ADMIN_CODE_LABEL);
        assertNotNull(adminCodePanel, "Admin code panel should be present in component tree.");
        assertFalse(adminCodePanel.isVisible(), "Admin code panel should be hidden initially.");

        // Toggle ON -> should become visible.
        SwingUtilities.invokeAndWait(() -> adminCheckbox.setSelected(true));
        // Fire its listeners to simulate user click.
        SwingUtilities.invokeAndWait(() -> {
            for (ActionListener l : adminCheckbox.getActionListeners()) {
                l.actionPerformed(null);
            }
        });
        assertTrue(adminCodePanel.isVisible(), "Admin code panel should be visible when admin is selected.");

        // Toggle OFF -> should hide again.
        SwingUtilities.invokeAndWait(() -> adminCheckbox.setSelected(false));
        SwingUtilities.invokeAndWait(() -> {
            for (ActionListener l : adminCheckbox.getActionListeners()) {
                l.actionPerformed(null);
            }
        });
        assertFalse(adminCodePanel.isVisible(), "Admin code panel should be hidden when admin is deselected.");
    }

    @Test
    void cancelButton_triggersViewManagerPopOrClose() throws Exception {
        final JButton cancel = findButtonByText(view, BTN_CANCEL);
        assertNotNull(cancel, "Cancel button should exist.");
        assertTrue(cancel.getActionListeners().length > 0, "Cancel button should have an ActionListener.");

        // Click on EDT.
        SwingUtilities.invokeAndWait(cancel::doClick);

        assertEquals(1, viewManager.popOrCloseCount,
                "Cancel should delegate to ViewManagerModel#popViewOrClose()");
    }

    @Test
    void buttons_arePresent_andHaveListeners() {
        final JButton signUp = findButtonByText(view, BTN_SIGN_UP);
        final JButton backToLogin = findButtonByText(view, BTN_BACK_TO_LOGIN);
        final JButton cancel = findButtonByText(view, BTN_CANCEL);

        assertNotNull(signUp, "'Sign Up' button should exist.");
        assertNotNull(backToLogin, "'Back to Login' button should exist.");
        assertNotNull(cancel, "'Cancel' button should exist.");

        assertTrue(signUp.getActionListeners().length > 0, "'Sign Up' should have listeners attached.");
        assertTrue(backToLogin.getActionListeners().length > 0, "'Back to Login' should have listeners attached.");
        assertTrue(cancel.getActionListeners().length > 0, "'Cancel' should have listeners attached.");
    }

    /* --------------------------- helper methods --------------------------- */

    /** Breadth-first traversal to find a JButton by its exact text. */
    private static JButton findButtonByText(Container root, String text) {
        Deque<Component> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Component c = q.removeFirst();
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (text.equals(b.getText())) return b;
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) q.addLast(child);
            }
        }
        return null;
    }

    /** Find a JCheckBox by its text. */
    private static JCheckBox findCheckBoxByText(Container root, String text) {
        Deque<Component> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Component c = q.removeFirst();
            if (c instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) c;
                if (text.equals(box.getText())) return box;
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) q.addLast(child);
            }
        }
        return null;
    }

    /**
     * Return the panel created by createInputPanel(labelText, field) whose first component
     * is a JLabel with the given text. This is the container whose visibility gets toggled.
     */
    private static JPanel findInputPanelByLabel(Container root, String labelText) {
        Deque<Component> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Component c = q.removeFirst();
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                Component[] children = p.getComponents();
                if (children.length >= 1 && children[0] instanceof JLabel) {
                    JLabel lbl = (JLabel) children[0];
                    if (labelText.equals(lbl.getText())) return p;
                }
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) q.addLast(child);
            }
        }
        return null;
    }

    /**
     * Finds the JTextField (or JPasswordField) that belongs to the input panel for a label.
     * The panel layout adds [label, inputField] in order.
     */
    private static JTextField findTextFieldLabeled(Container root, String labelText) {
        JPanel panel = findInputPanelByLabel(root, labelText);
        if (panel == null) return null;
        // Expect at least [JLabel, JTextField]
        for (Component child : panel.getComponents()) {
            if (child instanceof JTextField) {
                return (JTextField) child;
            }
        }
        return null;
    }
}
