package view;

import entity.Post;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchView.
 * We verify UI-to-VM binding, controller invocation, and result rendering signals.
 */
class SearchViewTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private SearchViewModel vm;
    private SearchController controller;
    private SearchState state;
    private SearchView view;

    @BeforeEach
    void setup() throws Exception {
        vm = mock(SearchViewModel.class);
        controller = mock(SearchController.class);

        state = new SearchState(); // assume no-arg constructor
        state.setSearchQuery("laptop");
        state.setLoading(false);
        when(vm.getState()).thenReturn(state);

        SwingUtilities.invokeAndWait(() -> {
            view = new SearchView(vm);
            view.setSearchController(controller);
        });
    }

    // helpers
    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && ((JButton) c).getText().equals(text)) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }
    private JCheckBox findCheckBox(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JCheckBox) return (JCheckBox) c;
            if (c instanceof Container) {
                JCheckBox f = findCheckBox((Container) c);
                if (f != null) return f;
            }
        }
        return null;
    }
    private boolean treeContainsLabelText(Component root, String needle) {
        if (root instanceof JLabel) {
            if (((JLabel) root).getText() != null && ((JLabel) root).getText().contains(needle)) return true;
        }
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                if (treeContainsLabelText(c, needle)) return true;
            }
        }
        return false;
    }

    @Test
    void clickSearch_executesWithQueryAndFuzzyFlag() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JCheckBox fuzzy = findCheckBox(view);
            fuzzy.setSelected(true);
            JButton search = findButton(view, "Search");
            assertNotNull(search);
            search.doClick();
        });
        verify(controller, times(1)).execute("laptop", true);
    }

    @Test
    void backButton_callsNavigateBack() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton back = findButton(view, "Back");
            assertNotNull(back);
            back.doClick();
        });
        verify(controller, times(1)).navigateBack();
    }

    @Test
    void propertyChange_loadingAndNoResultsRendersMessages() throws Exception {
        // Loading state shows "Searching..."
        SearchState loading = new SearchState();
        loading.setSearchQuery("x");
        loading.setLoading(true);

        SwingUtilities.invokeAndWait(() -> {
            view.propertyChange(new PropertyChangeEvent(this, "state", state, loading));
            assertTrue(treeContainsLabelText(view, "Searching..."));
        });

        // No results shows "No posts found..."
        SearchState empty = new SearchState();
        empty.setSearchQuery("x");
        empty.setLoading(false);
        empty.setSearchResults(Collections.emptyList());

        SwingUtilities.invokeAndWait(() -> {
            view.propertyChange(new PropertyChangeEvent(this, "state", loading, empty));
            assertTrue(treeContainsLabelText(view, "No posts found"));
        });
    }

    @Test
    void typingUpdatesViewModelState() throws Exception {
        ArgumentCaptor<SearchState> captor = ArgumentCaptor.forClass(SearchState.class);
        SwingUtilities.invokeAndWait(() -> {
            // find first JTextField in the top section and type
            JTextField tf = null;
            for (Component c : view.getComponents()) {
                if (tf != null) break;
                if (c instanceof Container) {
                    for (Component cc : ((Container) c).getComponents()) {
                        if (cc instanceof LabelTextPanel) {
                            for (Component ccc : ((LabelTextPanel) cc).getComponents()) {
                                if (ccc instanceof JTextField) { tf = (JTextField) ccc; break; }
                            }
                        }
                    }
                }
            }
            assertNotNull(tf);
            tf.setText("phone"); // triggers DocumentListener
        });
        verify(vm, atLeastOnce()).setState(captor.capture());
        assertEquals("phone", captor.getValue().getSearchQuery());
    }

    @Test
    void propertyChange_withResultsDisplaysHeader() throws Exception {
        // Use a mocked Post to avoid depending on concrete entity constructor
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn("Found laptop");
        when(post.isLost()).thenReturn(false);

        SearchState withResults = new SearchState();
        withResults.setSearchQuery("lap");
        withResults.setLoading(false);
        withResults.setSearchResults(java.util.List.of(post));

        SwingUtilities.invokeAndWait(() -> {
            view.propertyChange(new PropertyChangeEvent(this, "state", state, withResults));
            assertTrue(treeContainsLabelText(view, "Search Results (1 posts found)"));
        });
    }
}
