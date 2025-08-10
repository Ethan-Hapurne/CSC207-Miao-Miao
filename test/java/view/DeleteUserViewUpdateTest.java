package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.delete_user.DeleteUserState;
import interface_adapter.delete_user.DeleteUserViewModel;
import interface_adapter.delete_user.DeleteUserController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Extra imports for the interactor stub
import use_case.deleteUser.DeleteUserInputBoundary;
import use_case.deleteUser.DeleteUserInputData;

/**
 * Verifies DeleteUserView reacts to state updates and back navigation.
 * Uses a no-op interactor (anonymous class) instead of a lambda,
 * because DeleteUserInputBoundary has two abstract methods.
 */
class DeleteUserViewUpdateTest {

    /** Captures the last pushed view name instead of actually navigating. */
    static class CapturingVM extends ViewManagerModel {
        String lastPushed;
        @Override public void pushView(String viewName) { lastPushed = viewName; }
    }

    /** Creates a DeleteUserController wired to a no-op interactor. */
    private static DeleteUserController makeNoopController() {
        DeleteUserInputBoundary interactor = new DeleteUserInputBoundary() {
            @Override public void execute(DeleteUserInputData data) { /* no-op */ }
            @Override public void loadUsers() { /* no-op */ }
        };
        return new DeleteUserController(interactor);
    }

    @BeforeAll
    static void headless() { System.setProperty("java.awt.headless", "true"); }

    @Test
    void propertyChange_updatesUserListPanels() throws Exception {
        DeleteUserViewModel vm = new DeleteUserViewModel();
        CapturingVM nav = new CapturingVM();
        DeleteUserController controller = makeNoopController();
        DeleteUserView view = new DeleteUserView(vm, controller, nav);

        // Build state with 3 users and empty messages (to avoid JOptionPane popups)
        DeleteUserState st = new DeleteUserState();
        st.setUsersList(List.of("alice", "bob", "carol"));
        st.setError("");
        st.setSuccessMessage("");

        // Fire the state change on EDT so Swing can build the children safely
        SwingUtilities.invokeAndWait(() -> vm.setState(st));

        int deletes = countButtons(view, "Delete");
        assertEquals(3, deletes, "Should render 3 user rows with Delete buttons");
    }

    @Test
    void back_pushesAdminLoggedIn() throws Exception {
        DeleteUserViewModel vm = new DeleteUserViewModel();
        CapturingVM nav = new CapturingVM();
        DeleteUserController controller = makeNoopController();
        DeleteUserView view = new DeleteUserView(vm, controller, nav);

        JButton back = findButton(view, "Back");
        assertNotNull(back, "Back button should exist");

        // Click on EDT to avoid race conditions
        SwingUtilities.invokeAndWait(back::doClick);

        assertEquals("admin logged in", nav.lastPushed,
                "Back should navigate to 'admin logged in'");
    }

    // -------- helpers --------

    /** Recursively counts JButton instances whose text equals the given label. */
    private static int countButtons(Container root, String text) {
        int cnt = 0;
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) cnt++;
            if (c instanceof Container) cnt += countButtons((Container) c, text);
        }
        return cnt;
    }

    /** Recursively finds the first JButton whose text equals the given label. */
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
