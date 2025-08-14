package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.delete_user.DeleteUserController;
import interface_adapter.delete_user.DeleteUserState;
import interface_adapter.delete_user.DeleteUserViewModel;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-coverage tests for {@link DeleteUserView}.
 *
 * Coverage plan:
 *  - Build the view on the EDT and verify the initial layout and placeholder text.
 *  - Fire COMPONENT_SHOWN to exercise the anonymous ComponentListener that calls controller.loadUsers().
 *  - Drive the UI via DeleteUserViewModel#setState(...) to cover the propertyChange branch that
 *    repopulates the users list (both non-empty and empty cases).
 *  - Click the Back button and assert that ViewManagerModel receives "admin logged in".
 *  - Validate getViewName().
 *
 * Not covered (and why):
 *  - Paths that show modal dialogs (JOptionPane) for delete confirmation and error/success toasts:
 *    these require a windowing environment and are flaky in headless CI. We keep error/success empty
 *    and do not click the Delete button. The core view logic is still thoroughly covered.
 */
public class DeleteUserViewTest {

    /**
     * Lightweight fake controller that records method calls without touching real interactors.
     * We extend the real controller to keep production types, but override methods to be no-ops.
     */
    private static final class FakeDeleteUserController extends DeleteUserController {
        int loadUsersCalls = 0;
        int executeCalls = 0;
        String lastDeleted = null;

        FakeDeleteUserController() {
            super(null); // real ctor requires a boundary; pass null but we override loadUsers/execute
        }

        @Override
        public void loadUsers() {
            loadUsersCalls++;
        }

        @Override
        public void execute(final String username) {
            executeCalls++;
            lastDeleted = username;
        }
    }

    // ---------------------------------------------------------------------
    // JUnit lifecycle: force headless and ensure Swing is flushed on EDT
    // ---------------------------------------------------------------------

    @BeforeAll
    static void forceHeadless() {
        // Keep tests stable in CI: avoid any real dialogs / native windows.
        System.setProperty("java.awt.headless", "true");
    }

    /** Helper: run a runnable on EDT and wait. */
    private static void onEDT(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Constructor renders title and empty placeholder; componentShown triggers loadUsers()")
    void constructorAndComponentShown() throws Exception {
        final DeleteUserViewModel vm = new DeleteUserViewModel();
        final FakeDeleteUserController controller = new FakeDeleteUserController();
        final ViewManagerModel nav = new ViewManagerModel();

        final DeleteUserView[] holder = new DeleteUserView[1];

        onEDT(() -> {
            DeleteUserView view = new DeleteUserView(vm, controller, nav);
            holder[0] = view;
        });

        final DeleteUserView view = holder[0];
        assertNotNull(view, "View must be constructed.");

        // Title label is NORTH component.
        JLabel title = (JLabel) view.getComponent(0);
        assertEquals("All Users", title.getText(), "Title should be 'All Users'.");

        // Users scroll pane is CENTER, placeholder "No users found" should be present initially.
        JPanel usersPanel = usersPanel(view);
        assertNotNull(findLabel(usersPanel, "No users found"), "Empty placeholder should render.");

        // Fire a synthetic COMPONENT_SHOWN event -> controller.loadUsers() should be called exactly once.
        onEDT(() -> view.dispatchEvent(new ComponentEvent(view, ComponentEvent.COMPONENT_SHOWN)));
        assertEquals(1, controller.loadUsersCalls, "loadUsers should be called when the view becomes visible.");

        // getViewName() sanity check.
        assertEquals("delete users", view.getViewName());
    }

    @Test
    @DisplayName("propertyChange with non-empty list renders user rows")
    void populateUsersNonEmpty() throws Exception {
        final DeleteUserViewModel vm = new DeleteUserViewModel();
        final FakeDeleteUserController controller = new FakeDeleteUserController();
        final ViewManagerModel nav = new ViewManagerModel();

        final DeleteUserView[] holder = new DeleteUserView[1];
        onEDT(() -> holder[0] = new DeleteUserView(vm, controller, nav));
        final DeleteUserView view = holder[0];

        // Build a state that contains users.
        final List<String> users = Arrays.asList("alice", "bob", "carol");
        final DeleteUserState st = new DeleteUserState();
        st.setUsersList(users);
        st.setError("");
        st.setSuccessMessage("");

        // Setting the state will fire propertyChange via the ViewModel.
        onEDT(() -> vm.setState(st));

        // Verify that rows are rendered: for each user there is a JPanel row with a "Delete" JButton.
        JPanel usersPanel = usersPanel(view);
        int rowCount = countUserRows(usersPanel);
        assertEquals(users.size(), rowCount, "Each user should render exactly one row.");

        // Back button is SOUTH component: clicking it should push 'admin logged in' to nav model.
        JButton back = (JButton) view.getComponent(2);
        onEDT(back::doClick);
        assertEquals("admin logged in", nav.getState(), "Back should push 'admin logged in'.");
    }

    @Test
    @DisplayName("propertyChange with empty list shows placeholder again")
    void populateUsersEmpty() throws Exception {
        final DeleteUserViewModel vm = new DeleteUserViewModel();
        final FakeDeleteUserController controller = new FakeDeleteUserController();
        final ViewManagerModel nav = new ViewManagerModel();

        final DeleteUserView[] holder = new DeleteUserView[1];
        onEDT(() -> holder[0] = new DeleteUserView(vm, controller, nav));
        final DeleteUserView view = holder[0];

        // Push an empty list state.
        final DeleteUserState st = new DeleteUserState();
        st.setUsersList(Collections.emptyList());
        st.setError("");
        st.setSuccessMessage("");

        onEDT(() -> vm.setState(st));

        JPanel usersPanel = usersPanel(view);
        assertNotNull(findLabel(usersPanel, "No users found"),
                "Placeholder 'No users found' should be present when list is empty.");
    }

    // ---------------------------------------------------------------------
    // Helpers to navigate the component tree of DeleteUserView
    // ---------------------------------------------------------------------

    /** Extract the users panel from CENTER scroll pane. */
    private static JPanel usersPanel(DeleteUserView view) {
        // Layout: NORTH Title (index 0), CENTER Scroll (index 1), SOUTH Back (index 2)
        JScrollPane scroll = (JScrollPane) view.getComponent(1);
        JViewport vp = scroll.getViewport();
        return (JPanel) vp.getView();
    }

    /** Count rows (direct children panels) that contain a "Delete" button. */
    private static int countUserRows(JPanel usersPanel) {
        int cnt = 0;
        for (Component c : usersPanel.getComponents()) {
            if (c instanceof JPanel) {
                if (findButton((JPanel) c, "Delete") != null) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    /** Find a JLabel with exact text under a container (DFS). */
    private static JLabel findLabel(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if (text.equals(l.getText())) {
                    return l;
                }
            }
            if (c instanceof Container) {
                JLabel inner = findLabel((Container) c, text);
                if (inner != null) return inner;
            }
        }
        return null;
    }

    /** Find a JButton with exact text under a container (DFS). */
    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (text.equals(b.getText())) {
                    return b;
                }
            }
            if (c instanceof Container) {
                JButton inner = findButton((Container) c, text);
                if (inner != null) return inner;
            }
        }
        return null;
    }
}
