package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.LoggedInState;
import interface_adapter.change_password.LoggedInViewModel;
import interface_adapter.change_username.ChangeUsernameController;
import interface_adapter.logout.LogoutController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies AccountView wiring: back navigation and controller parameters. */
class AccountViewActionsTest {

    /** Captures navigation calls. */
    static class CapturingVM extends ViewManagerModel {
        int popOrCloseCount = 0;
        @Override public void popViewOrClose() { popOrCloseCount++; }
    }

    @BeforeAll
    static void headless() { System.setProperty("java.awt.headless", "true"); }

    @Test
    void back_callsPopOrClose() {
        CapturingVM nav = new CapturingVM();
        AccountView view = new AccountView(nav);

        JButton back = findButton(view, "Back");
        assertNotNull(back);
        back.doClick();

        assertEquals(1, nav.popOrCloseCount);
    }

    @Test
    void changeUsername_sendsOldAndNew() {
        CapturingVM nav = new CapturingVM();
        AccountView view = new AccountView(nav);

        // LoggedInViewModel with current user in state
        LoggedInViewModel loggedVM = new LoggedInViewModel();
        LoggedInState st = loggedVM.getState();
        st.setUsername("oldUser");
        st.setAdmin(false);
        loggedVM.setState(st);
        view.setLoggedInViewModel(loggedVM);

        final String[] capture = new String[2];
        ChangeUsernameController cuc = new ChangeUsernameController(null) {
            @Override public void execute(String oldUsername, String newUsername) {
                capture[0] = oldUsername; capture[1] = newUsername;
            }
        };
        view.setChangeUsernameController(cuc);

        // Fill new username text field and click button
        setSiblingTextFieldAfterLabel(view, "New Username:", "newUser");
        JButton btn = findButton(view, "Change Username");
        assertNotNull(btn);
        btn.doClick();

        assertEquals("oldUser", capture[0]);
        assertEquals("newUser", capture[1]);
    }

    @Test
    void changePassword_sendsUserPasswordAndAdmin() {
        CapturingVM nav = new CapturingVM();
        AccountView view = new AccountView(nav);

        LoggedInViewModel loggedVM = new LoggedInViewModel();
        LoggedInState st = loggedVM.getState();
        st.setUsername("alice");
        st.setAdmin(true);
        loggedVM.setState(st);
        view.setLoggedInViewModel(loggedVM);

        final String[] cap = new String[3]; // user, pass, admin flag
        ChangePasswordController cpc = new ChangePasswordController(null) {
            @Override public void execute(String password, String username, boolean admin) {
                // NOTE: controller signature is (password, username, admin)
                cap[0] = username; cap[1] = password; cap[2] = String.valueOf(admin);
            }
        };
        view.setChangePasswordController(cpc);

        setSiblingTextFieldAfterLabel(view, "New Password:", "S3cret!");
        JButton btn = findButton(view, "Change Password");
        btn.doClick();

        assertEquals("alice", cap[0]);
        assertEquals("S3cret!", cap[1]);
        assertEquals("true", cap[2]);
    }

    @Test
    void logout_sendsUsername() {
        CapturingVM nav = new CapturingVM();
        AccountView view = new AccountView(nav);

        LoggedInViewModel loggedVM = new LoggedInViewModel();
        LoggedInState st = loggedVM.getState();
        st.setUsername("bob");
        st.setAdmin(false);
        loggedVM.setState(st);
        view.setLoggedInViewModel(loggedVM);

        final String[] cap = new String[1];
        LogoutController lc = new LogoutController(null) {
            @Override public void execute(String username) { cap[0] = username; }
        };
        view.setLogoutController(lc);

        JButton btn = findButton(view, "Log Out");
        assertNotNull(btn);
        btn.doClick();

        assertEquals("bob", cap[0]);
    }

    // ---------- helpers ----------
    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }
    /** Find a JLabel with labelText and set the text of the immediate next JTextField in the same parent panel. */
    private static void setSiblingTextFieldAfterLabel(Container root, String labelText, String value) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel) {
                Component[] a = ((JPanel) c).getComponents();
                for (int i = 0; i < a.length - 1; i++) {
                    if (a[i] instanceof JLabel && labelText.equals(((JLabel)a[i]).getText())) {
                        if (a[i+1] instanceof JTextField) ((JTextField)a[i+1]).setText(value);
                    }
                }
            }
            if (c instanceof Container) setSiblingTextFieldAfterLabel((Container) c, labelText, value);
        }
    }
}
