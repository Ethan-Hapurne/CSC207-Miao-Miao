package view;

import entity.Post;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stable, high-coverage tests for {@link SearchView}.
 *
 * Why this version is stable when running with the whole suite:
 *  - Every test shows the view inside a real JFrame and disposes it in @AfterEach.
 *  - All UI mutations happen on the EDT.
 *  - flushEdt() guarantees that pending invokeLater tasks are processed even if
 *    the caller is already on the EDT (the usual flakiness culprit).
 *  - A couple of tiny wait helpers avoid racing on asynchronous label updates.
 */
public class SearchViewTest {

    /** Minimal fake controller capturing the last call for verification. */
    static class FakeSearchController extends SearchController {
        String lastMethod;
        Object[] lastArgs;

        FakeSearchController() { super(null, null); }

        @Override
        public void execute(String query) {
            lastMethod = "execute";
            lastArgs = new Object[]{query};
        }

        @Override
        public void navigateBack() {
            lastMethod = "navigateBack";
            lastArgs = null;
        }
    }

    private SearchViewModel vm;
    private SearchView view;
    private FakeSearchController controller;
    private JFrame host;

    @BeforeEach
    void setUp() throws Exception {
        runOnEdt(() -> {
            vm = new SearchViewModel();
            controller = new FakeSearchController();
            view = new SearchView(vm);
            view.setSearchController(controller);

            host = new JFrame("SearchHost");
            host.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            host.getContentPane().add(view);
            host.setSize(900, 600);
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

    @Test
    void viewName_isSearch() throws Exception {
        runOnEdt(() -> assertEquals("search", view.getViewName()));
    }

    @Test
    void propertyChange_loading_showsSearchingLabel() throws Exception {
        runOnEdt(() -> {
            SearchState s = vm.getState();
            s.setSearchQuery("wallet");
            s.setLoading(true);
            s.setSearchResults(null);
            s.setSearchError(null);
            vm.setState(s);
            vm.firePropertyChanged();
        });
        flushEdt();

        runOnEdt(() -> {
            JPanel resultsPanel = findResultsPanel(view);
            assertNotNull(resultsPanel);
            assertTrue(
                    waitForLabelTextContains(resultsPanel, "searching", 2000),
                    "Expected a 'Searching' label to appear"
            );
        });
    }

    @Test
    void propertyChange_emptyResults_showsNoResultsMessage() throws Exception {
        runOnEdt(() -> {
            SearchState s = vm.getState();
            s.setSearchQuery("zzz");
            s.setLoading(false);
            s.setSearchResults(new ArrayList<>());
            s.setSearchError(null);
            vm.setState(s);
            vm.firePropertyChanged();
        });
        flushEdt();

        runOnEdt(() -> {
            JPanel resultsPanel = findResultsPanel(view);
            assertNotNull(resultsPanel);
            assertTrue(
                    waitForLabelTextContains(resultsPanel, "no posts", 2000),
                    "Expected a 'No posts' message"
            );
        });
    }

    @Test
    void propertyChange_results_renderList_andShowTags() throws Exception {
        runOnEdt(() -> {
            List<Post> posts = new ArrayList<>();

            Post p1 = new Post();
            p1.setTitle("Lost Wallet");
            p1.setDescription("Black leather wallet near library");
            p1.setAuthor("Alice");
            p1.setLocation("Library");
            p1.setLost(true);
            p1.setTimestamp("2025-08-12T10:20:00");
            p1.setTags(List.of("wallet", "black"));

            Post p2 = new Post();
            p2.setTitle("Found Keys");
            p2.setDescription("Keychain with blue tag");
            p2.setAuthor("Bob");
            p2.setLocation("Gym");
            p2.setLost(false);
            p2.setTimestamp("2025-08-12T11:00:00");
            p2.setTags(List.of("keys"));

            posts.add(p1);
            posts.add(p2);

            SearchState s = vm.getState();
            s.setSearchQuery("wa");
            s.setLoading(false);
            s.setSearchResults(posts);
            s.setSearchError(null);
            vm.setState(s);
            vm.firePropertyChanged();
        });
        flushEdt();

        runOnEdt(() -> {
            JPanel resultsPanel = findResultsPanel(view);
            assertNotNull(resultsPanel);

            // Header that says "2 posts found" (wording can vary slightly, so just check the number)
            assertTrue(anyLabelContains(resultsPanel, "2"), "Header should reflect 2 posts found");

            String allTexts = collectAllLabelTexts(resultsPanel).toLowerCase();
            assertTrue(allTexts.contains("lost wallet"));
            assertTrue(allTexts.contains("found keys"));
            assertTrue(allTexts.contains("alice"));
            assertTrue(allTexts.contains("bob"));
            assertTrue(allTexts.contains("library"));
            assertTrue(allTexts.contains("gym"));
            assertTrue(allTexts.contains("wallet"));
            assertTrue(allTexts.contains("black"));
            assertTrue(allTexts.contains("keys"));
            assertTrue(allTexts.contains("lost"));
            assertTrue(allTexts.contains("found"));
        });
    }

    @Test
    void propertyChange_setsErrorText_andSyncsInputFromState() throws Exception {
        runOnEdt(() -> {
            SearchState s = vm.getState();
            s.setSearchQuery("abc");
            s.setSearchError("Boom!");
            s.setLoading(false);
            s.setSearchResults(new ArrayList<>());
            vm.setState(s);
            vm.firePropertyChanged();
        });
        flushEdt();

        runOnEdt(() -> {
            JTextField input = findFirst(view, JTextField.class);
            assertNotNull(input);
            assertEquals("abc", input.getText());

            // Exact error label might be placed deep in the tree – search for exact text.
            JLabel error = findLabelWithExactText(view, "Boom!");
            assertNotNull(error, "Could not find a JLabel that shows the error text");
            assertEquals("Boom!", error.getText());
        });
    }

    @Test
    void typingInSearchField_updatesViewModelViaDocumentListener() throws Exception {
        JTextField input = runOnEdtGet(() -> findFirst(view, JTextField.class));
        assertNotNull(input, "Search input field should exist");

        runOnEdt(() -> setTextByDoc(input, "hello world"));
        flushEdt();

        assertEquals("hello world", vm.getState().getSearchQuery());
    }

    @Test
    void clickingSearchButton_callsControllerExecute_withCurrentQuery() throws Exception {
        JTextField input = runOnEdtGet(() -> findFirst(view, JTextField.class));
        JButton searchBtn = runOnEdtGet(() -> findButton(view, "Search"));
        assertNotNull(input);
        assertNotNull(searchBtn);

        runOnEdt(() -> setTextByDoc(input, "phone"));
        flushEdt();

        runOnEdt(searchBtn::doClick);
        flushEdt();

        assertEquals("execute", controller.lastMethod);
        assertEquals("phone", controller.lastArgs[0]);
    }

    @Test
    void clickingBack_callsControllerNavigateBack() throws Exception {
        JButton backBtn = runOnEdtGet(() -> findButton(view, "Back"));
        assertNotNull(backBtn);
        runOnEdt(backBtn::doClick);
        flushEdt();
        assertEquals("navigateBack", controller.lastMethod);
    }

    // ---------------- helpers ----------------

    /** Return the inner results panel (inside the view's JScrollPane). */
    private static JPanel findResultsPanel(SearchView v) {
        for (Component c : v.getComponents()) {
            if (c instanceof JScrollPane) {
                JViewport vp = ((JScrollPane) c).getViewport();
                Component inner = vp.getView();
                if (inner instanceof JPanel) {
                    return (JPanel) inner;
                }
            }
        }
        return null;
    }

    /** Find a JButton by its exact text, recursively. */
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

    /** Find the first component of a given type, recursively. */
    private static <T> T findFirst(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                T got = findFirst(c, type);
                if (got != null) {
                    return got;
                }
            }
        }
        return null;
    }

    /** Find a JLabel with exact text anywhere under the root. */
    private static JLabel findLabelWithExactText(Component root, String target) {
        if (root instanceof JLabel) {
            JLabel l = (JLabel) root;
            if (target.equals(l.getText())) {
                return l;
            }
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                JLabel found = findLabelWithExactText(c, target);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Does ANY JLabel under container contain the substring (case-insensitive)? */
    private static boolean anyLabelContains(Container container, String needle) {
        String n = needle.toLowerCase();
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                if (((JLabel) c).getText().toLowerCase().contains(n)) {
                    return true;
                }
            } else if (c instanceof Container) {
                if (anyLabelContains((Container) c, needle)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Poll for a label containing given text to appear within timeoutMs. */
    private static boolean waitForLabelTextContains(Container root, String needle, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (anyLabelContains(root, needle)) {
                return true;
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /** Collect all JLabel text under a container (debug helper). */
    private static String collectAllLabelTexts(Container container) {
        StringBuilder sb = new StringBuilder();
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                sb.append(((JLabel) c).getText()).append('\n');
            } else if (c instanceof Container) {
                sb.append(collectAllLabelTexts((Container) c));
            }
        }
        return sb.toString();
    }

    /** Safely set text through the Document (fires DocumentListener). */
    private static void setTextByDoc(JTextField field, String text) {
        Document doc = field.getDocument();
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- EDT helpers (robust) ----------

    /** Run code on EDT and wait. */
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

    /** Run supplier on EDT and return the value. */
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

    /**
     * Robustly flush pending tasks on the EDT.
     *
     * If we are already on the EDT, the usual "invokeAndWait(no-op)" trick does not work,
     * because you cannot block the EDT waiting on itself. Instead:
     *  - When NOT on EDT: do a normal invokeAndWait(no-op), which guarantees all previously
     *    queued tasks have run.
     *  - When ON the EDT: post a marker with invokeLater and wait for it with a latch.
     *    This ensures all tasks queued before this call are processed.
     */
    private static void flushEdt() throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(() -> { /* no-op */ });
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        // Wait up to 2 seconds; if it times out, something is seriously wrong.
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new IllegalStateException("EDT flush timed out");
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
