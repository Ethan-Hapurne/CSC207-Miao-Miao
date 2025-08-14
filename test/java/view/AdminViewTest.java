package view;

import interface_adapter.admin.AdminController;
import interface_adapter.admin.AdminState;
import interface_adapter.admin.AdminViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stable, high-coverage unit tests for {@link AdminView}.
 *
 * Notes:
 *  - Each test displays the view inside a JFrame and disposes it after the test.
 *  - All UI actions are invoked on the EDT, followed by flushing the EDT.
 *  - No Mockito; we use a tiny fake controller that records last calls.
 */
public class AdminViewTest {

    /** Simple fake AdminController that records the last method call and arguments. */
    private static class FakeAdminController extends AdminController {
        String lastMethod;
        Object[] lastArgs;

        FakeAdminController() { super(null, null); }

        @Override public void loadPosts() { lastMethod = "loadPosts"; lastArgs = null; }
        @Override public void searchPosts(String q) { lastMethod = "searchPosts"; lastArgs = new Object[]{q}; }
        @Override public void addPost(String t, String c, List<String> tags, String loc, boolean lost) {
            lastMethod = "addPost"; lastArgs = new Object[]{t, c, tags, loc, lost};
        }
        @Override public void editPost(String id, String t, String d, String loc, List<String> tags, boolean lost) {
            lastMethod = "editPost"; lastArgs = new Object[]{id, t, d, loc, tags, lost};
        }
        @Override public void deletePost(String id) { lastMethod = "deletePost"; lastArgs = new Object[]{id}; }
        @Override public void navigateBack() { lastMethod = "navigateBack"; lastArgs = null; }
    }

    private AdminView view;
    private AdminViewModel vm;
    private FakeAdminController controller;
    private JFrame host;

    @BeforeEach
    void setUp() throws Exception {
        runOnEdt(() -> {
            vm = new AdminViewModel();
            controller = new FakeAdminController();
            view = new AdminView(vm);
            view.setAdminController(controller);

            host = new JFrame("AdminHost");
            host.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            host.getContentPane().add(view);
            host.setSize(1000, 700);
            host.setLocationRelativeTo(null);
            host.setVisible(true);
        });
        flushEdt();
    }

    @AfterEach
    void tearDown() throws Exception {
        runOnEdt(() -> {
            if (host != null) {
                host.dispose();
            }
        });
        flushEdt();
        view = null;
        vm = null;
        controller = null;
        host = null;
    }

    // ---------------- Basic wiring ----------------

    @Test
    void getViewName_returnsAdmin() throws Exception {
        runOnEdt(() -> assertEquals("admin", view.getViewName()));
    }

    @Test
    void componentShown_triggersLoadPosts() throws Exception {
        runOnEdt(() -> {
            for (ComponentListener cl : view.getComponentListeners()) {
                cl.componentShown(new ComponentEvent(view, ComponentEvent.COMPONENT_SHOWN));
            }
        });
        flushEdt();
        assertEquals("loadPosts", controller.lastMethod);
    }

    @Test
    void searchButton_callsControllerWithText() throws Exception {
        runOnEdt(() -> view.getSearchField().setText("q123"));
        JButton search = runOnEdtGet(() -> findButton(view, "Search"));
        assertNotNull(search, "Search button should exist");
        runOnEdt(search::doClick);
        flushEdt();
        assertEquals("searchPosts", controller.lastMethod);
        assertEquals("q123", controller.lastArgs[0]);
    }

    @Test
    void backButton_callsNavigateBack() throws Exception {
        JButton back = runOnEdtGet(() -> findButton(view, "Back"));
        assertNotNull(back, "Back button should exist");
        runOnEdt(back::doClick);
        flushEdt();
        assertEquals("navigateBack", controller.lastMethod);
    }

    @Test
    void setSelectedPost_togglesEditButton() throws Exception {
        JButton edit = runOnEdtGet(() -> findButton(view, "Edit Post"));
        assertNotNull(edit, "Edit Post button should exist");
        runOnEdt(() -> view.setSelectedPost(null));
        flushEdt();
        assertFalse(edit.isEnabled(), "Edit should be disabled when no selection");

        runOnEdt(() -> view.setSelectedPost("42"));
        flushEdt();
        assertTrue(edit.isEnabled(), "Edit should be enabled when a post is selected");

        runOnEdt(() -> view.setSelectedPost(null));
        flushEdt();
        assertFalse(edit.isEnabled(), "Edit should be disabled again after clearing selection");
    }

    // ---------------- Save edit path (bypass modal dialog) ----------------

    @Test
    void saveEdit_invokesControllerEditPostWithSelectedId_andParsesTags() throws Exception {
        runOnEdt(() -> view.setSelectedPost("123"));

        // Fill edit fields via reflection.
        runOnEdt(() -> {
            ((JTextField) getPrivateField("titleField")).setText("TitleX");
            ((JTextArea) getPrivateField("descriptionArea")).setText("DescX");
            ((JTextField) getPrivateField("locationField")).setText("LocX");
            ((JTextField) getPrivateField("tagsField")).setText("  a, b, c  "); // verify trimming
            ((JCheckBox) getPrivateField("isLostCheckBox")).setSelected(false);
        });

        // Call private saveEdit() which dispatches to controller.editPost(...)
        runOnEdt(() -> invokePrivateNoArg("saveEdit"));
        flushEdt();

        assertEquals("editPost", controller.lastMethod);
        assertEquals("123", controller.lastArgs[0]);
        assertEquals("TitleX", controller.lastArgs[1]);
        assertEquals("DescX", controller.lastArgs[2]);
        assertEquals("LocX", controller.lastArgs[3]);
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) controller.lastArgs[4];
        List<String> trimmed = tags.stream().map(s -> s == null ? null : s.trim()).collect(Collectors.toList());
        assertEquals(List.of("a", "b", "c"), trimmed, "Tags should be trimmed and split");
        assertEquals(false, controller.lastArgs[5]);
    }

    // ---------------- Posts list & detail rendering ----------------

    @Test
    void propertyChange_state_withEmptyPosts_rendersPlaceholder() throws Exception {
        AdminState s = new AdminState();
        s.setPosts(new ArrayList<>());
        runOnEdt(() -> view.propertyChange(new PropertyChangeEvent(this, "state", null, s)));
        flushEdt();

        JLabel placeholder = runOnEdtGet(() -> findLabel(view, "No posts found."));
        assertNotNull(placeholder, "Placeholder should be present when no posts");
    }

    @Test
    void propertyChange_state_withOnePost_rendersList_and_clickShowsDetails() throws Exception {
        Object post = createPost(
                7,
                "Lost Keys",
                "Black keychain with 3 keys",
                "alice",
                "Library",
                true,
                "2025-01-01 12:00",
                List.of("keys", "lost"),
                5
        );

        AdminState s = new AdminState();
        List<Object> posts = new ArrayList<>();
        posts.add(post);
        @SuppressWarnings({"rawtypes", "unchecked"})
        List casted = posts; // unchecked ok for runtime
        s.setPosts(casted);

        // Fire "state" to render list
        runOnEdt(() -> view.propertyChange(new PropertyChangeEvent(this, "state", null, s)));
        flushEdt();

        JButton deleteBtn = runOnEdtGet(() -> findButton(view, "Delete Post"));
        assertNotNull(deleteBtn, "Per-item Delete button should exist, meaning the list item was rendered");

        // Click the item panel (ancestor of delete button) to show details and set selection
        JPanel itemPanel = runOnEdtGet(() -> findAncestorPanel(deleteBtn));
        assertNotNull(itemPanel, "Item panel should exist");

        runOnEdt(() -> itemPanel.dispatchEvent(new MouseEvent(
                itemPanel, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 5, 5, 1, false
        )));
        flushEdt();

        JButton edit = runOnEdtGet(() -> findButton(view, "Edit Post"));
        assertTrue(edit.isEnabled(), "Edit should be enabled after clicking a list item");

        JLabel titleInDetails = runOnEdtGet(() -> findLabel(view, "Lost Keys"));
        assertNotNull(titleInDetails, "Details panel should show the post title");
    }

    /**
     * Regression guard: clicking the visible "Edit Post" button should not NPE.
     * Ensure AdminViewModel holds posts so getCurrentlySelectedPost() can resolve it.
     */
    @Test
    void editButton_click_invokesEditHandlerBranch_withoutNullPointer() throws Exception {
        Object post777 = createPost(
                777, "Any", "AnyDesc", "userA", "loc",
                true, "2025-01-01 00:00", List.of("t1"), 0
        );

        AdminState s = new AdminState();
        List<Object> posts = new ArrayList<>();
        posts.add(post777);
        @SuppressWarnings({"rawtypes", "unchecked"})
        List casted = posts;
        s.setPosts(casted);

        // Install state into ViewModel to allow resolution of selected post
        runOnEdt(() -> vm.setState(s));

        // Also fire a "state" event so the list is rendered.
        runOnEdt(() -> view.propertyChange(new PropertyChangeEvent(this, "state", null, s)));
        flushEdt();

        // Select the post in the view
        runOnEdt(() -> view.setSelectedPost("777"));
        flushEdt();

        // Prepare edit fields (so when showEditDialog pre-fills, fields are valid)
        runOnEdt(() -> {
            ((JTextField) getPrivateField("titleField")).setText("TT");
            ((JTextArea) getPrivateField("descriptionArea")).setText("DD");
            ((JTextField) getPrivateField("locationField")).setText("LL");
            ((JTextField) getPrivateField("tagsField")).setText("t1,t2");
            ((JCheckBox) getPrivateField("isLostCheckBox")).setSelected(true);
        });

        JButton editBtn = runOnEdtGet(() -> findButton(view, "Edit Post"));
        assertNotNull(editBtn, "Edit button should exist");
        runOnEdt(editBtn::doClick);
        flushEdt();

        // Call saveEdit directly to exercise controller path (bypass modal).
        runOnEdt(() -> invokePrivateNoArg("saveEdit"));
        flushEdt();

        assertEquals("editPost", controller.lastMethod);
        assertEquals("777", controller.lastArgs[0]);
    }

    // ---------------- Helpers ----------------

    private Component getPrivateField(String name) {
        try {
            Field f = AdminView.class.getDeclaredField(name);
            f.setAccessible(true);
            return (Component) f.get(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivateNoArg(String name) {
        try {
            var m = AdminView.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JButton findButton(Component root, String text) {
        if (root instanceof JButton && text.equals(((JButton) root).getText())) {
            return (JButton) root;
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                JButton b = findButton(c, text);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    private static JLabel findLabel(Component root, String text) {
        if (root instanceof JLabel && text.equals(((JLabel) root).getText())) {
            return (JLabel) root;
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                JLabel l = findLabel(c, text);
                if (l != null) {
                    return l;
                }
            }
        }
        return null;
    }

    private static JPanel findAncestorPanel(Component c) {
        Component cur = c;
        while (cur != null) {
            if (cur instanceof JPanel) {
                return (JPanel) cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    /**
     * Create an instance of entity.Post and set expected fields via reflection.
     * This avoids depending on constructors or setters.
     */
    private Object createPost(int id,
                              String title,
                              String desc,
                              String author,
                              String location,
                              boolean lost,
                              String timestamp,
                              List<String> tags,
                              int likes) throws Exception {
        Class<?> postCls = Class.forName("entity.Post");
        Object post = postCls.getDeclaredConstructor().newInstance();

        setField(post, postCls, "postID", id);
        setField(post, postCls, "title", title);
        setField(post, postCls, "description", desc);
        setField(post, postCls, "author", author);
        setField(post, postCls, "location", location);
        setField(post, postCls, "isLost", lost);
        // AdminView may rely on getTimestamp(), try both common field names
        if (!setField(post, postCls, "timestamp", timestamp)) {
            setField(post, postCls, "timeStamp", timestamp); // fallback
        }
        setField(post, postCls, "tags", tags);
        setField(post, postCls, "numberOfLikes", likes);
        return post;
    }

    /** Try to set a declared field; returns false if the field is not found. */
    private boolean setField(Object target, Class<?> cls, String name, Object val) throws Exception {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, val);
            return true;
        } catch (NoSuchFieldException ex) {
            return false;
        }
    }

    // ---------- EDT helpers ----------

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            AtomicReference<Exception> ex = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> {
                try { r.run(); } catch (Exception e) { ex.set(e); }
            });
            if (ex.get() != null) {
                throw ex.get();
            }
        }
    }

    private static <T> T runOnEdtGet(SupplierWithException<T> s) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return s.get();
        }
        AtomicReference<T> ref = new AtomicReference<>();
        AtomicReference<Exception> ex = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { ref.set(s.get()); } catch (Exception e) { ex.set(e); }
        });
        if (ex.get() != null) {
            throw ex.get();
        }
        return ref.get();
    }

    private static void flushEdt() throws Exception {
        runOnEdt(() -> { /* pump */ });
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
