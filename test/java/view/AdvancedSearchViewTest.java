package view;

import entity.Post;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AdvancedSearchView}.
 *
 * <p>Design notes:
 * <ul>
 *   <li>No mocking frameworks are used (ByteBuddy / inline mocks are fragile on JDK 24).</li>
 *   <li>We stimulate the view by sending PropertyChangeEvents for "state".</li>
 *   <li>Private UI fields are read with reflection in a safe, localized utility.</li>
 *   <li>Assertions avoid brittle layout checks; we assert presence of key labels/texts.</li>
 * </ul>
 */
final class AdvancedSearchViewTest {

    private AdvancedSearchView view;

    /**
     * Real SearchViewModel is used only to satisfy the constructor;
     * we do not rely on it emitting events in these tests.
     */
    @BeforeEach
    void setUp() {
        SearchViewModel vm = new SearchViewModel();
        view = new AdvancedSearchView(vm);
    }

    @Test
    @DisplayName("getViewName returns 'advanced_search'")
    void getViewName_returnsAdvancedSearch() {
        assertEquals("advanced_search", view.getViewName());
    }

    @Test
    @DisplayName("propertyChange('state') with loading=true renders 'Searching...' placeholder")
    void propertyChange_loading_rendersSearchingPlaceholder() {
        SearchState s = new SearchState();
        s.setLoading(true);
        s.setSearchResults(new ArrayList<>());
        s.setSearchError("");

        // Fire the event directly.
        view.propertyChange(new java.beans.PropertyChangeEvent(this, "state", null, s));

        JPanel results = getPrivate(view, "resultsPanel", JPanel.class);
        assertNotNull(results);

        // Find a JLabel text == "Searching..."
        assertTrue(containsLabelText(results, "Searching..."));
    }

    @Test
    @DisplayName("propertyChange('state') with empty results renders 'No posts found...'")
    void propertyChange_empty_rendersNoResults() {
        SearchState s = new SearchState();
        s.setLoading(false);
        s.setSearchResults(new ArrayList<>());
        s.setSearchError("");

        view.propertyChange(new java.beans.PropertyChangeEvent(this, "state", null, s));

        JPanel results = getPrivate(view, "resultsPanel", JPanel.class);
        assertTrue(containsLabelText(results,
                "No posts found matching your search criteria."));
    }

    @Test
    @DisplayName("propertyChange('state') with results renders header and post content")
    void propertyChange_results_renderedWithHeaderAndPost() {
        List<Post> posts = new ArrayList<>();
        posts.add(makePost(
                1,
                "Lost Wallet",
                "Black leather wallet with a red stripe",
                Collections.singletonList("wallet"),
                true
        ));

        SearchState s = new SearchState();
        s.setLoading(false);
        s.setSearchResults(posts);
        s.setSearchError("");

        view.propertyChange(new java.beans.PropertyChangeEvent(this, "state", null, s));

        JPanel results = getPrivate(view, "resultsPanel", JPanel.class);

        // Header showing the count
        assertTrue(containsLabelText(results, "Advanced Search Results (1 posts found):"));

        // Title or content snippet should appear somewhere in composed components
        assertTrue(containsTextRecursively(results, "Lost Wallet"));
        assertTrue(containsTextRecursively(results, "Black leather wallet"));
        assertTrue(containsTextRecursively(results, "Tags: wallet"));
    }

    // ----------------------- helpers -----------------------

    /**
     * Make a Post using the project entity constructor.
     */
    private static Post makePost(final int id,
                                 final String title,
                                 final String desc,
                                 final List<String> tags,
                                 final boolean isLost) {
        // The Post entity stores timestamp as String internally; pass LocalDateTime here.
        return new Post(
                id,
                title,
                desc,
                tags,
                LocalDateTime.now(),
                "tester",
                "Library",
                null,
                isLost,
                0,
                null
        );
    }

    /**
     * Reflection helper to read a private field from the view.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getPrivate(final Object target, final String fieldName, final Class<T> type) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object v = f.get(target);
            if (v == null) {
                return null;
            }
            if (!type.isInstance(v)) {
                throw new IllegalStateException("Field " + fieldName + " is not of type " + type.getName());
            }
            return (T) v;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to access private field: " + fieldName, e);
        }
    }

    /**
     * Recursively checks whether a container contains a JLabel with exact text.
     */
    private static boolean containsLabelText(final Container root, final String expected) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel) {
                if (expected.equals(((JLabel) c).getText())) {
                    return true;
                }
            } else if (c instanceof Container) {
                if (containsLabelText((Container) c, expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursively checks whether any JLabel contains a given substring.
     */
    private static boolean containsTextRecursively(final Container root, final String needle) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel) {
                String t = ((JLabel) c).getText();
                if (t != null && t.contains(needle)) {
                    return true;
                }
            } else if (c instanceof Container) {
                if (containsTextRecursively((Container) c, needle)) {
                    return true;
                }
            }
        }
        return false;
    }
}
