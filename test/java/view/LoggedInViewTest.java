package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.LoggedInState;
import interface_adapter.change_password.LoggedInViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-coverage tests for LoggedInView without Mockito/agents (JDK 24 friendly).
 * We verify:
 *  - constructor wiring
 *  - reaction to "state" and "password" PropertyChange events
 *  - action listeners of Dashboard / Account / DMs / Fuzzy Search buttons
 *  - view routing via a test double of ViewManagerModel
 *
 * All comments are in English as requested.
 */
public class LoggedInViewTest {

    private LoggedInViewModel loggedInViewModel;
    private TestViewManagerModel viewManagerModel; // test double that records last pushed view

    @BeforeEach
    void setUp() {
        // Replace with your real constructors if needed.
        loggedInViewModel = new LoggedInViewModel();
        viewManagerModel = new TestViewManagerModel();
    }

    @Test
    void ctor_buildsPanel() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);
        assertNotNull(view);
        assertEquals("logged in", view.getViewName());
        assertTrue(view.getComponentCount() > 0, "View should have child components");
    }

    @Test
    void propertyChange_state_updatesUsernameLabel() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);

        LoggedInState state = new LoggedInState();
        state.setUsername("alice");
        view.propertyChange(new PropertyChangeEvent(this, "state", null, state));

        assertTrue(containsLabelWithText(view, "alice"),
                "Username label should display the updated username");
    }

    @Test
    void propertyChange_password_branch_is_reachable() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);

        LoggedInState state = new LoggedInState();
        state.setUsername("bob");
        // This goes through the else-if branch: evt.getPropertyName().equals("password")
        view.propertyChange(new PropertyChangeEvent(this, "password", null, state));

        // No visible change expected for password branch; we just assert that it does not crash.
        assertNotNull(view);
    }

    @Test
    void clickingAccount_routesToAccount() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);
        JButton btn = findButtonByText(view, "Account");
        assertNotNull(btn, "Account button should exist");

        btn.getActionListeners()[0].actionPerformed(new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, "click"));
        assertEquals("account", viewManagerModel.lastPushedView, "Account button should push 'account'");
    }

    @Test
    void clickingFuzzySearch_routesToFuzzySearch() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);
        JButton btn = findButtonByText(view, "Fuzzy Search");
        assertNotNull(btn, "Fuzzy Search button should exist");

        btn.getActionListeners()[0].actionPerformed(new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, "click"));
        assertEquals("fuzzy search", viewManagerModel.lastPushedView, "Fuzzy Search button should push 'fuzzy search'");
    }

    @Test
    void clickingDMs_routesToDms_evenWithoutDmsView() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);
        JButton btn = findButtonByText(view, "DMs");
        assertNotNull(btn, "DMs button should exist");

        btn.getActionListeners()[0].actionPerformed(new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, "click"));
        assertEquals("dms", viewManagerModel.lastPushedView, "DMs button should push 'dms'");
    }

    @Test
    void clickingDashboard_routesToDashboard() {
        LoggedInView view = new LoggedInView(loggedInViewModel, viewManagerModel);
        JButton btn = findButtonByText(view, "Dashboard");
        assertNotNull(btn, "Dashboard button should exist");

        // Set current username before clicking to exercise the code path that reads it.
        LoggedInState state = new LoggedInState();
        state.setUsername("carol");
        view.propertyChange(new PropertyChangeEvent(this, "state", null, state));

        btn.getActionListeners()[0].actionPerformed(new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, "click"));
        assertEquals("dashboard", viewManagerModel.lastPushedView, "Dashboard button should push 'dashboard'");
    }

    // ---------- helpers ----------

    /**
     * A tiny test double to observe which view was requested.
     * We extend the real ViewManagerModel to avoid any compile issues in the production code path.
     */
    private static class TestViewManagerModel extends ViewManagerModel {
        String lastPushedView;

        @Override
        public void pushView(String viewName) {
            this.lastPushedView = viewName;
            super.pushView(viewName); // call through in case there is additional behavior
        }
    }

    /** Depth-first search for a JButton by its text. */
    private static JButton findButtonByText(Component root, String text) {
        if (root instanceof JButton) {
            if (text.equals(((JButton) root).getText())) return (JButton) root;
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                JButton found = findButtonByText(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Recursively check if any JLabel in the tree has the given text. */
    private static boolean containsLabelWithText(Component root, String text) {
        if (root instanceof JLabel) {
            if (text.equals(((JLabel) root).getText())) return true;
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                if (containsLabelWithText(child, text)) return true;
            }
        }
        return false;
    }
}
