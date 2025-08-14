package view;

import entity.Post;
import interface_adapter.ViewManagerModel;
import interface_adapter.fuzzy_search.FuzzySearchController;
import interface_adapter.fuzzy_search.FuzzySearchState;
import interface_adapter.fuzzy_search.FuzzySearchViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.fuzzy_search.FuzzySearchInputBoundary;
import use_case.fuzzy_search.FuzzySearchInputData;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI-level tests for {@link FuzzySearchView} using plain test doubles.
 * No Mockito / agents required (works on JDK 24+).
 */
public class FuzzySearchViewTest {

    /** Minimal interactor that just records the last query. */
    private static class RecordingInteractor implements FuzzySearchInputBoundary {
        String lastQuery;

        @Override
        public void execute(FuzzySearchInputData inputData) {
            this.lastQuery = inputData.getSearchQuery();
        }
    }

    /** A lightweight ViewManagerModel stub to observe back navigation. */
    private static class RecordingViewManagerModel extends ViewManagerModel {
        boolean popped;

        public RecordingViewManagerModel() {
            super();
        }

        @Override
        public void popViewOrClose() {
            popped = true;
        }
    }

    private RecordingInteractor interactor;
    private FuzzySearchController controller;
    private FuzzySearchViewModel viewModel;
    private RecordingViewManagerModel vmModel;
    private FuzzySearchView view;

    // Cached private UI fields (via reflection)
    private JTextField searchField;
    private JButton searchButton;
    private JButton backButton;
    private JLabel messageLabel;
    private JPanel resultsPanel;

    @BeforeEach
    void setUp() throws Exception {
        // Prepare MVC pieces
        interactor = new RecordingInteractor();
        controller = new FuzzySearchController(interactor);
        viewModel = new FuzzySearchViewModel();
        vmModel = new RecordingViewManagerModel();

        // Build view
        view = new FuzzySearchView(viewModel, controller, vmModel);

        // Reflect private Swing fields so assertions can be made without changing production code.
        searchField = getPrivate(view, "searchField", JTextField.class);
        searchButton = getPrivate(view, "searchButton", JButton.class);
        backButton = getPrivate(view, "backButton", JButton.class);
        messageLabel = getPrivate(view, "messageLabel", JLabel.class);
        resultsPanel = getPrivate(view, "resultsPanel", JPanel.class);

        // Ensure we're on a Swing-capable thread for button clicks, etc.
        // (Not strictly necessary in headless unit tests, but safe.)
        if (!EventQueue.isDispatchThread()) {
            // no-op; tests trigger listeners synchronously
        }
    }

    /**
     * Helper to read a private field via reflection and cast safely.
     */
    private static <T> T getPrivate(Object target, String fieldName, Class<T> type) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        Object val = f.get(target);
        assertNotNull(val, "Expected field '" + fieldName + "' to be initialized");
        assertTrue(type.isInstance(val), "Field '" + fieldName + "' is not a " + type.getSimpleName());
        return type.cast(val);
    }

    @Test
    void emptyQueryShowsValidationMessage() {
        // Given an empty query
        searchField.setText("   ");

        // When clicking Search
        searchButton.doClick();

        // Then the view shows a validation message instead of calling the interactor
        assertEquals("Please enter a search query.", messageLabel.getText());
        assertNull(interactor.lastQuery, "Interactor should not be called for empty query");
    }

    @Test
    void nonEmptyQueryIsSentToUseCase() {
        // Given a valid query
        String q = "wallet";
        searchField.setText(q);

        // When clicking Search
        searchButton.doClick();

        // Then the controller forwards the query to the interactor
        assertEquals(q, interactor.lastQuery);
    }

    @Test
    void propertyChangeWithResultsRendersCards() {
        // Given a state with 2 posts
        List<Post> posts = new ArrayList<>();
        posts.add(makePost(1, "Lost Wallet", "Black leather wallet", true, "Alice"));
        posts.add(makePost(2, "Found Keys", "Set of keys with red tag", false, "Bob"));

        FuzzySearchState state = new FuzzySearchState();
        state.setSuccess(true);
        state.setMessage("2 results");
        state.setSearchResults(posts);

        // When the view model fires a "state" change
        PropertyChangeEvent evt = new PropertyChangeEvent(viewModel, "state", null, state);
        view.propertyChange(evt);

        // Then result cards are added
        assertTrue(resultsPanel.getComponentCount() >= 2,
                "Expected at least 2 result components to be rendered");
        assertEquals("2 results", messageLabel.getText());
    }

    @Test
    void backButtonPopsViewManagerModel() {
        // When clicking Back
        backButton.doClick();

        // Then navigation popped (no exception, usable verification)
        assertTrue(vmModel.popped, "Expected popViewOrClose() to be invoked");
    }

    // ----------------------- helpers -----------------------

    /**
     * Create a Post using the no-arg ctor and setters (firebase-friendly),
     * which matches your entity implementation.
     */
    private static Post makePost(int id, String title, String desc, boolean lost, String author) {
        Post p = new Post();
        p.setPostID(id);
        p.setTitle(title);
        p.setDescription(desc);
        p.setLost(lost);
        p.setAuthor(author);
        p.setTimestamp(LocalDateTime.now().toString()); // format expected by the view's formatter
        return p;
    }
}
