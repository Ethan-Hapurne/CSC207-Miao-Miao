package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.LoggedInState;
import interface_adapter.change_password.LoggedInViewModel;
import interface_adapter.dashboard.DashboardController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies LoggedInView navigation and Dashboard current-user handoff. */
class LoggedInViewNavTest {

    static class CapturingVM extends ViewManagerModel {
        String lastPushed;
        @Override public void pushView(String viewName) { lastPushed = viewName; }
    }
    static class CapturingDashboardController extends DashboardController {
        String lastUser;
        public CapturingDashboardController() { super(null, new ViewManagerModel()); }
        @Override public void setCurrentUser(String username) { lastUser = username; }
    }

    @BeforeAll
    static void headless() { System.setProperty("java.awt.headless", "true"); }

    @Test
    void dashboard_setsCurrentUser_thenPushesDashboard() {
        CapturingVM nav = new CapturingVM();
        LoggedInViewModel vm = new LoggedInViewModel();
        LoggedInState st = vm.getState();
        st.setUsername("zoe"); st.setAdmin(false);
        vm.setState(st);

        LoggedInView view = new LoggedInView(vm, nav);
        CapturingDashboardController d = new CapturingDashboardController();
        view.setDashboardController(d);

        JButton btn = findButton(view, "Dashboard");
        btn.doClick();

        assertEquals("zoe", d.lastUser, "DashboardController should receive current user first");
        assertEquals("dashboard", nav.lastPushed, "Then navigate to dashboard");
    }

    @Test
    void account_pushesAccount() {
        CapturingVM nav = new CapturingVM();
        LoggedInView v = new LoggedInView(new LoggedInViewModel(), nav);
        findButton(v, "Account").doClick();
        assertEquals("account", nav.lastPushed);
    }

    @Test
    void dms_pushesDms() {
        CapturingVM nav = new CapturingVM();
        LoggedInView v = new LoggedInView(new LoggedInViewModel(), nav);
        findButton(v, "DMs").doClick();
        assertEquals("dms", nav.lastPushed);
    }

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
}

