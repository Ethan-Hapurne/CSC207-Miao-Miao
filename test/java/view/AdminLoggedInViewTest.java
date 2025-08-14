package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.adminloggedIn.AdminLoggedInState;
import interface_adapter.adminloggedIn.AdminLoggedInViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AdminLoggedInView}.
 *
 * Notes:
 * - No Mockito is used (works fine on JDK 24).
 * - We use a tiny fake ViewManagerModel to observe navigation.
 * - We also cover the DMs button, both when DMsView is null and when set.
 * - Comments are in English to satisfy style rules.
 */
public class AdminLoggedInViewTest {

    /** A minimal test ViewManagerModel that records navigation calls. */
    private static class TestViewManagerModel extends ViewManagerModel {
        String lastPushed;
        int popCount;

        @Override
        public void pushView(final String viewName) {
            this.lastPushed = viewName;
        }

        @Override
        public void popViewOrClose() {
            popCount++;
        }
    }

    /**
     * A tiny concrete DMsView used only to capture the username passed from
     * AdminLoggedInView before navigating to the DMs page.
     */
    private static class TestDMsView extends DMsView {
        String capturedUsername;

        TestDMsView(final ViewManagerModel vmm, final interface_adapter.dms.DMsViewModel vm) {
            super(vmm, vm);
        }

        @Override
        public void setCurrentUsername(final String username) {
            // Do not call super to keep it simple; just capture the value.
            this.capturedUsername = username;
        }
    }

    private AdminLoggedInViewModel vm;
    private TestViewManagerModel vmm;
    private AdminLoggedInView view;

    @BeforeEach
    void setUp() {
        vm = new AdminLoggedInViewModel();
        vmm = new TestViewManagerModel();
        view = new AdminLoggedInView(vm, vmm);
    }

    // -------------------- helpers --------------------

    private static JButton findButton(final Component root, final String text) {
        if (root instanceof JButton && text.equals(((JButton) root).getText())) {
            return (JButton) root;
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                final JButton found = findButton(c, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static JLabel findLabel(final Component root, final String text) {
        if (root instanceof JLabel && text.equals(((JLabel) root).getText())) {
            return (JLabel) root;
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                final JLabel found = findLabel(c, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // -------------------- tests --------------------

    @Test
    void getViewName_returnsAdminLoggedIn() {
        assertEquals("admin logged in", view.getViewName());
    }

    @Test
    void propertyChange_state_updatesUsernameLabel() {
        final AdminLoggedInState state = new AdminLoggedInState();
        state.setUsername("adminUser");

        // Fire the property change directly.
        view.propertyChange(new PropertyChangeEvent(this, "state", null, state));

        // The username label is rendered in the welcome card; find it by text.
        final JLabel label = findLabel(view, "adminUser");
        assertNotNull(label, "Username label should be updated to 'adminUser'.");
    }

    @Test
    void dashboardButton_pushesDashboard() {
        final JButton btn = findButton(view, "Dashboard");
        assertNotNull(btn);
        btn.doClick();
        assertEquals("dashboard", vmm.lastPushed);
    }

    @Test
    void accountButton_pushesAccount() {
        final JButton btn = findButton(view, "Account");
        assertNotNull(btn);
        btn.doClick();
        assertEquals("account", vmm.lastPushed);
    }

    @Test
    void adminDashboardButton_pushesAdmin() {
        final JButton btn = findButton(view, "AdminDashboard");
        assertNotNull(btn);
        btn.doClick();
        assertEquals("admin", vmm.lastPushed);
    }

    @Test
    void deleteUsersButton_pushesDeleteUsers() {
        final JButton btn = findButton(view, "Delete Users");
        assertNotNull(btn);
        btn.doClick();
        assertEquals("delete users", vmm.lastPushed);
    }

    @Test
    void dmsButton_withoutDMsView_stillNavigates() {
        // Do not set a DMsView; branch should skip setCurrentUsername but still navigate.
        final AdminLoggedInState state = new AdminLoggedInState();
        state.setUsername("alice");
        view.propertyChange(new PropertyChangeEvent(this, "state", null, state));

        final JButton btn = findButton(view, "DMs");
        assertNotNull(btn);
        btn.doClick();
        assertEquals("dms", vmm.lastPushed);
    }

    @Test
    void dmsButton_withDMsView_setsUsername_thenNavigates() {
        // Prepare state/username first.
        final AdminLoggedInState state = new AdminLoggedInState();
        state.setUsername("bob");
        view.propertyChange(new PropertyChangeEvent(this, "state", null, state));

        // Attach a test DMsView to capture username.
        final TestDMsView testDMs =
                new TestDMsView(vmm, new interface_adapter.dms.DMsViewModel());
        view.setDMsView(testDMs);

        final JButton btn = findButton(view, "DMs");
        assertNotNull(btn);
        btn.doClick();

        assertEquals("bob", testDMs.capturedUsername,
                "DMsView.setCurrentUsername should be called with current username.");
        assertEquals("dms", vmm.lastPushed, "Navigation should still go to 'dms'.");
    }
}
