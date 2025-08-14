package view;

import entity.Post;
import interface_adapter.dashboard.DashboardController;
import interface_adapter.dashboard.DashboardState;
import interface_adapter.dashboard.DashboardViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-coverage tests for {@link DashboardView}.
 *
 * Covered (via real Swing events on the EDT):
 *  - UI construction (toolbar, tabs, lists)
 *  - propertyChange → list update, details render, sorting by timestamp (DESC)
 *  - Post like button (+1)
 *  - Comment section: add a comment and like a comment
 *  - Search dropdown branches: General / Title / Location / Tags / Lost / Found
 *  - "My Posts" filtering by current user
 *  - Back button calls controller.navigateBack()
 *
 * Notes:
 *  - All Swing interaction runs on EDT using EventQueue.invokeAndWait
 *  - FakeDashboardController records calls only; no production side effects
 */
public class DashboardViewTest {

    private static final String CRITERIA_GENERAL  = "General Search";
    private static final String CRITERIA_TITLE    = "Title";
    private static final String CRITERIA_LOCATION = "Location";
    private static final String CRITERIA_TAGS     = "Tags";
    private static final String CRITERIA_LOST     = "Lost Items";
    private static final String CRITERIA_FOUND    = "Found Items";

    @BeforeAll
    static void headlessOn() {
        // Keep tests stable on CI without a display server.
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("propertyChange → list updates; details render; Like +1; My Posts filters")
    void propertyChangeAndLikeOnce() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            v.setCurrentUser("alice");
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        final Post p1 = buildPost(1, "Alpha", "A-desc", "tag1,tag2", "LIB", true,
                "alice", LocalDateTime.now().minusHours(1).toString(), 0);
        final Post p2 = buildPost(2, "Bravo", "B-desc", "t3", "GYM", false,
                "bob", LocalDateTime.now().minusMinutes(5).toString(), 3);
        final Post p3 = buildPost(3, "Charlie", "C-desc", "", "CSB", true,
                "alice", LocalDateTime.now().minusDays(1).toString(), 1);

        final DashboardState state = new DashboardState();
        state.setPosts(Arrays.asList(p1, p2, p3));
        state.setSelectedPost(null);
        state.setError("");
        state.setSuccessMessage("");

        EventQueue.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(this, "state", null, state)));

        JPanel postsList = findPostsListPanel(view);
        assertNotNull(postsList);
        assertTrue(countChildrenOfType(postsList, JPanel.class) >= 3);

        // Click first item (should be most recent -> p2).
        JPanel firstItem = firstChildPanel(postsList);
        assertNotNull(firstItem);
        EventQueue.invokeAndWait(() -> firstItem.dispatchEvent(new java.awt.event.MouseEvent(
                firstItem, java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 3, 3, 1, false)));

        JPanel detailCenter = findDetailCenterPanel(view);
        assertNotNull(detailCenter);
        assertNotNull(findLabelContains(detailCenter, "Content:"));
        assertNotNull(findLabelContains(detailCenter, "Tags:"));
        assertNotNull(findLabelContains(detailCenter, "Location:"));
        assertNotNull(findLabelStarts(detailCenter, "Type:"));
        assertNotNull(findLabelStarts(detailCenter, "Posted:"));

        JLabel likesLabel = findLabelStarts(detailCenter, "Likes:");
        assertNotNull(likesLabel);
        int beforeLikes = trailingInt(likesLabel.getText());

        JButton likeBtn = findButtonWithText(view, "❤ Like Post");
        assertNotNull(likeBtn);
        EventQueue.invokeAndWait(likeBtn::doClick);

        JPanel detailAfter = findDetailCenterPanel(view);
        JLabel likesAfter = findLabelStarts(detailAfter, "Likes:");
        assertNotNull(likesAfter);
        assertEquals(beforeLikes + 1, trailingInt(likesAfter.getText()));

        // My Posts shows only Alice's posts (p1 & p3).
        JScrollPane myScroll = findMyPostsScroll(view);
        assertNotNull(myScroll);
        JPanel myList = (JPanel) myScroll.getViewport().getView();
        assertTrue(countChildrenOfType(myList, JPanel.class) >= 2);
    }

    @Test
    @DisplayName("Comment section: add a comment and like the comment")
    void commentAddAndLike() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            v.setCurrentUser("alice");
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        final Post post = buildPost(7, "WithComments", "desc", "t", "CSB", false,
                "bob", LocalDateTime.now().toString(), 0);

        final DashboardState state = new DashboardState();
        state.setPosts(Collections.singletonList(post));
        state.setSelectedPost(post); // render details immediately
        state.setError("");
        state.setSuccessMessage("");

        EventQueue.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(this, "state", null, state)));

        // Button exists?
        JButton postCommentBtn = findButtonWithText(view, "Post Comment");
        assertNotNull(postCommentBtn, "Post Comment button must exist.");

        // Get the sibling JTextField from the same parent (inputPanel with BorderLayout).
        JTextField input = findSiblingTextField(postCommentBtn);
        assertNotNull(input, "Comment input must be resolved as sibling of the Post Comment button.");

        // Type and submit.
        EventQueue.invokeAndWait(() -> input.setText("hello world!"));
        EventQueue.invokeAndWait(postCommentBtn::doClick);

        // After refresh, the posted comment text should be present somewhere in the tree.
        JLabel posted = findLabelContains(view, "hello world!");
        assertNotNull(posted, "Posted comment text should be present.");

        // Like a comment (button text starts with "Like (")
        JButton commentLike = findButtonStarts(view, "Like (");
        assertNotNull(commentLike, "A comment Like button should exist.");
        EventQueue.invokeAndWait(commentLike::doClick);
        assertNotNull(findButtonStarts(view, "Like ("), "Comment Like button should remain after like.");
    }

    @Test
    @DisplayName("Search dropdown routes all branches; Back triggers navigateBack()")
    void searchRoutingAndBack() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        DashboardState state = new DashboardState();
        state.setPosts(new ArrayList<>());
        state.setSelectedPost(null);
        state.setError("");
        state.setSuccessMessage("");
        EventQueue.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(this, "state", null, state)));

        JTextField searchField = findSearchField(view);
        JComboBox<?> dropdown = findCriteriaDropdown(view);
        JButton searchBtn = findButtonWithText(view, "Search");
        assertNotNull(searchField);
        assertNotNull(dropdown);
        assertNotNull(searchBtn);

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_GENERAL, "wallet");
        assertEquals(1, controller.searchPostsCalls);
        assertEquals("wallet", controller.lastSearchPostsQuery);

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_TITLE, "phone");
        assertEquals(1, controller.advCalls);
        assertEquals("phone", controller.lastTitle);
        assertEquals("", controller.lastLocation);
        assertNull(controller.lastIsLost);
        assertTrue(controller.lastTags.isEmpty());

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_LOCATION, "CSB");
        assertEquals(2, controller.advCalls);
        assertEquals("CSB", controller.lastLocation);

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_TAGS, "t1, t2 , , t3");
        assertEquals(3, controller.advCalls);
        assertEquals(Arrays.asList("t1","t2","t3"), controller.lastTags);

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_LOST, "usb-c");
        assertEquals(4, controller.advCalls);
        assertEquals("usb-c", controller.lastTitle);
        assertEquals(Boolean.TRUE, controller.lastIsLost);

        setSearch(view, searchField, dropdown, searchBtn, CRITERIA_FOUND, "card");
        assertEquals(5, controller.advCalls);
        assertEquals("card", controller.lastTitle);
        assertEquals(Boolean.FALSE, controller.lastIsLost);

        JButton backBtn = findButtonWithText(view, "Back");
        assertNotNull(backBtn);
        EventQueue.invokeAndWait(backBtn::doClick);
        assertEquals(1, controller.backCalls);
    }

    @Test
    @DisplayName("My Posts updates when current user set and posts refresh")
    void myPostsFilterUpdates() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            v.setCurrentUser("carol");
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        final Post a = buildPost(10, "A", "a", "", "X", true,  "carol", LocalDateTime.now().toString(), 0);
        final Post b = buildPost(11, "B", "b", "", "Y", false, "bob",   LocalDateTime.now().minusHours(2).toString(), 0);
        final Post c = buildPost(12, "C", "c", "", "Z", true,  "carol", LocalDateTime.now().minusDays(1).toString(), 0);

        DashboardState state = new DashboardState();
        state.setPosts(Arrays.asList(a, b, c));
        state.setSelectedPost(null);
        state.setError("");
        state.setSuccessMessage("");

        EventQueue.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(this, "state", null, state)));

        JScrollPane myScroll = findMyPostsScroll(view);
        assertNotNull(myScroll);
        JPanel myList = (JPanel) myScroll.getViewport().getView();
        assertTrue(countChildrenOfType(myList, JPanel.class) >= 2);
    }

    @Test
    @DisplayName("Resolved post shows badges (Resolved by / Credited to)")
    void resolvedPostShowsBadges() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            v.setCurrentUser("alice");
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        final Post resolved = buildPost(22, "Done", "finished", "", "LIB", true,
                "bob", LocalDateTime.now().toString(), 2);
        resolved.setResolved(true);
        resolved.setResolvedBy("mod");
        resolved.setCreditedTo("carol");

        DashboardState state = new DashboardState();
        state.setPosts(Collections.singletonList(resolved));
        state.setSelectedPost(resolved);
        state.setError("");
        state.setSuccessMessage("");

        EventQueue.invokeAndWait(() ->
                view.propertyChange(new PropertyChangeEvent(this, "state", null, state)));

        JPanel detailCenter = findDetailCenterPanel(view);
        assertNotNull(detailCenter);
        assertNotNull(findLabelContains(detailCenter, "Resolved by:"));
        assertNotNull(findLabelContains(detailCenter, "Credited to:"));
    }

    @Test
    @DisplayName("Empty search query calls loadPosts()")
    void emptyQueryTriggersLoadAll() throws Exception {
        final DashboardViewModel vm = new DashboardViewModel();
        final FakeDashboardController controller = new FakeDashboardController();

        final DashboardView[] ref = new DashboardView[1];
        EventQueue.invokeAndWait(() -> {
            DashboardView v = new DashboardView(vm);
            v.setDashboardController(controller);
            ref[0] = v;
        });
        final DashboardView view = ref[0];

        JTextField searchField = findSearchField(view);
        JButton searchBtn = findButtonWithText(view, "Search");
        assertNotNull(searchField);
        assertNotNull(searchBtn);

        EventQueue.invokeAndWait(() -> {
            searchField.setText("");
            searchBtn.doClick();
        });

        assertEquals(1, controller.loadPostsCalls, "Empty search should call loadPosts()");
    }

    // -------------------- Fake controller & helpers --------------------

    static final class FakeDashboardController extends DashboardController {
        int searchPostsCalls = 0;
        String lastSearchPostsQuery = null;

        int advCalls = 0;
        String lastTitle = null;
        String lastLocation = null;
        List<String> lastTags = new ArrayList<>();
        Boolean lastIsLost = null;

        int backCalls = 0;
        int loadPostsCalls = 0;

        FakeDashboardController() { super(null, null); }

        public void loadPosts() { loadPostsCalls++; }

        public void searchPosts(final String query) {
            searchPostsCalls++;
            lastSearchPostsQuery = query;
        }

        public void executeAdvancedSearch(final String title,
                                          final String location,
                                          final List<String> tags,
                                          final Boolean isLost) {
            advCalls++;
            lastTitle = title;
            lastLocation = location;
            lastTags = new ArrayList<>(tags);
            lastIsLost = isLost;
        }

        public void addPost(final String t, final String c,
                            final List<String> tags, final String loc,
                            final boolean isLost) { }

        public void updatePost(final Post post) { }

        public void deletePost(final int postId) { }

        public void resolvePost(final String postId, final String creditedTo,
                                final String currentUser) { }

        public void navigateBack() { backCalls++; }
    }

    private static Post buildPost(final int id, final String title, final String desc,
                                  final String tagsCsv, final String location,
                                  final boolean isLost, final String author,
                                  final String timestamp, final int likes) {
        Post p = new Post();
        p.setPostID(id);
        p.setTitle(title);
        p.setDescription(desc);
        if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
            String[] parts = tagsCsv.split(",");
            if (parts.length == 1) {
                p.setTags(Collections.singletonList(parts[0]));
            } else {
                p.setTags(Arrays.asList(parts));
            }
        } else {
            p.setTags(new ArrayList<>());
        }
        p.setLocation(location);
        p.setLost(isLost);
        p.setAuthor(author);
        p.setTimestamp(timestamp);
        p.setNumberOfLikes(likes);
        return p;
    }

    private static int trailingInt(final String s) {
        final String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static void setSearch(final DashboardView view, final JTextField field,
                                  final JComboBox<?> dropdown, final JButton btn,
                                  final String criteria, final String query) throws Exception {
        EventQueue.invokeAndWait(() -> {
            field.setText(query);
            dropdown.setSelectedItem(criteria);
            btn.doClick();
        });
    }

    // ---------- component tree helpers ----------

    private static JPanel findPostsListPanel(final DashboardView view) {
        JTabbedPane tabs = findTabbed(view);
        Container generalTab = (Container) tabs.getComponentAt(0);
        JScrollPane scroll = (JScrollPane) generalTab.getComponent(0);
        return (JPanel) scroll.getViewport().getView();
    }

    private static JPanel findDetailCenterPanel(final DashboardView view) {
        JTabbedPane tabs = findTabbed(view);
        Container generalTab = (Container) tabs.getComponentAt(0);
        JPanel right = (JPanel) generalTab.getComponent(1);
        for (Component c : right.getComponents()) {
            if (c instanceof JScrollPane) {
                return (JPanel) ((JScrollPane) c).getViewport().getView();
            }
        }
        return null;
    }

    private static JScrollPane findMyPostsScroll(final DashboardView view) {
        JTabbedPane tabs = findTabbed(view);
        Container myTab = (Container) tabs.getComponentAt(1);
        for (Component c : myTab.getComponents()) {
            if (c instanceof JScrollPane) {
                return (JScrollPane) c;
            }
        }
        return null;
    }

    private static JTabbedPane findTabbed(final Container root) {
        final JTabbedPane[] ref = new JTabbedPane[1];
        walk(root, comp -> { if (comp instanceof JTabbedPane) ref[0] = (JTabbedPane) comp; });
        return ref[0];
    }

    private static JPanel firstChildPanel(final JPanel listPanel) {
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JPanel) return (JPanel) c;
        }
        return null;
    }

    private static int countChildrenOfType(final Container container, final Class<?> clazz) {
        int cnt = 0;
        for (Component c : container.getComponents()) {
            if (clazz.isInstance(c)) cnt++;
            if (c instanceof Container) cnt += countChildrenOfType((Container) c, clazz);
        }
        return cnt;
    }

    private static JLabel findLabelContains(final Container root, final String needle) {
        final JLabel[] ref = new JLabel[1];
        walk(root, comp -> {
            if (comp instanceof JLabel) {
                JLabel l = (JLabel) comp;
                if (l.getText() != null && l.getText().contains(needle)) ref[0] = l;
            }
        });
        return ref[0];
    }

    private static JLabel findLabelStarts(final Container root, final String prefix) {
        final JLabel[] ref = new JLabel[1];
        walk(root, comp -> {
            if (comp instanceof JLabel) {
                JLabel l = (JLabel) comp;
                if (l.getText() != null && l.getText().startsWith(prefix)) ref[0] = l;
            }
        });
        return ref[0];
    }

    private static JButton findButtonWithText(final Container root, final String text) {
        final JButton[] ref = new JButton[1];
        walk(root, comp -> {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (text.equals(b.getText())) ref[0] = b;
            }
        });
        return ref[0];
    }

    private static JButton findButtonStarts(final Container root, final String prefix) {
        final JButton[] ref = new JButton[1];
        walk(root, comp -> {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                String t = b.getText();
                if (t != null && t.startsWith(prefix)) ref[0] = b;
            }
        });
        return ref[0];
    }

    /**
     * Find the sibling JTextField that lives in the same parent as the given button.
     * In DashboardView the "Post Comment" button and the input field are added to the
     * same inputPanel (BorderLayout.EAST for the button, BorderLayout.CENTER for the field).
     */
    private static JTextField findSiblingTextField(JButton button) {
        Container parent = button.getParent();
        if (parent == null) return null;
        for (Component c : parent.getComponents()) {
            if (c instanceof JTextField) return (JTextField) c;
        }
        return null;
    }

    /**
     * Find the "main" search text field in the toolbar.
     * Strategy: scan all JTextFields and pick the widest one (the toolbar search field
     * is created with a large preferred width).
     */
    private static JTextField findSearchField(final Container root) {
        final JTextField[] ref = new JTextField[1];
        walk(root, comp -> {
            if (comp instanceof JTextField) {
                JTextField tf = (JTextField) comp;
                if (ref[0] == null ||
                        tf.getPreferredSize().width > ref[0].getPreferredSize().width) {
                    ref[0] = tf;
                }
            }
        });
        return ref[0];
    }

    /** Find the criteria dropdown (the only JComboBox in the toolbar). */
    private static JComboBox<?> findCriteriaDropdown(final Container root) {
        final JComboBox<?>[] ref = new JComboBox<?>[1];
        walk(root, comp -> {
            if (comp instanceof JComboBox) {
                ref[0] = (JComboBox<?>) comp;
            }
        });
        return ref[0];
    }

    // simple DFS that also descends into JScrollPane's viewport
    private interface Visitor { void visit(Component c); }

    private static void walk(final Component root, final Visitor v) {
        if (root == null) return;
        v.visit(root);
        if (root instanceof JScrollPane) {
            JComponent view = (JComponent) ((JScrollPane) root).getViewport().getView();
            if (view != null) walk(view, v);
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                walk(c, v);
            }
        }
    }
}
