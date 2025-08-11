package view;

import entity.Post;
import interface_adapter.admin.AdminController;
import interface_adapter.admin.AdminState;
import interface_adapter.admin.AdminViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeAll;

class AdminViewTest {
    private AdminView adminView;
    private AdminViewModel viewModel;

    @Mock
    private AdminController mockController;

    @BeforeAll
    static void setUp() {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @BeforeEach
    void setUp1() {
        MockitoAnnotations.openMocks(this);
        viewModel = new AdminViewModel();
        adminView = new AdminView(viewModel);
        adminView.setAdminController(mockController);
    }

    @Test
    void testInitialState() {
        assertEquals("admin", adminView.getViewName());
        assertNotNull(adminView.getComponents());
        assertTrue(adminView.isVisible());
    }

    @Test
    void testSearchFunctionality() {
        // Get the search field and button
        JTextField searchField = findComponent(adminView, JTextField.class, comp ->
                comp == adminView.getSearchField()); // Using field reference instead of size
        JButton searchButton = findComponent(adminView, JButton.class, comp ->
                "Search".equals(((JButton) comp).getText()));

        assertNotNull(searchField);
        assertNotNull(searchButton);

        // Simulate search
        searchField.setText("test search");
        searchButton.doClick();

        verify(mockController).searchPosts("test search");
    }

    @Test
    void testAddPost() {
        // Find and click Add Post button
        JButton addButton = findComponent(adminView, JButton.class, comp ->
                "Add Post".equals(((JButton) comp).getText()));
        assertNotNull(addButton);

        // Store current dialog count
        int initialDialogCount = Window.getWindows().length;

        // Click button to show dialog
        addButton.doClick();

        // Wait for dialog to appear
        JDialog dialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog && "Add New Post".equals(((JDialog) window).getTitle())) {
                dialog = (JDialog) window;
                break;
            }
        }
        assertNotNull(dialog);

        // Find all required form fields
        JTextField titleField = findComponent(dialog, JTextField.class, comp -> true);
        JTextArea contentArea = findComponent(dialog, JTextArea.class, comp -> true);
        assertNotNull(titleField);
        assertNotNull(contentArea);

        // Set empty values
        titleField.setText("");
        contentArea.setText("");

        // Find and click submit button
        JButton submitButton = findComponent(dialog, JButton.class, comp ->
                "Submit".equals(((JButton) comp).getText()));
        assertNotNull(submitButton);

        // Click submit with empty fields
        submitButton.doClick();

        // Verify addPost was never called with any arguments
        verify(mockController, never()).addPost(
                anyString(), anyString(), anyList(), anyString(), anyBoolean()
        );
    }

    @Test
    void testUpdatePostsList() {
        // Create sample posts
        List<Post> posts = Arrays.asList(
                createSamplePost(1, "Test Post 1"),
                createSamplePost(2, "Test Post 2")
        );

        // Update state with posts
        AdminState newState = new AdminState();
        newState.setPosts(posts);
        viewModel.setState(newState);

        // Trigger property change
        adminView.propertyChange(new PropertyChangeEvent(
                viewModel, "state", null, newState));

        // Verify posts panel contains items
        JPanel postsPanel = findComponent(adminView, JPanel.class, comp ->
                comp instanceof JPanel &&
                        ((JPanel)comp).getLayout() instanceof BoxLayout);
        assertNotNull(postsPanel);
        assertTrue(postsPanel.getComponentCount() > 0);
    }

    @Test
    void testPostSelection() {
        // Create and select a post
        Post testPost = createSamplePost(1, "Test Post");
        AdminState state = new AdminState();
        state.setPosts(Collections.singletonList(testPost));
        state.setSelectedPost(testPost);

        viewModel.setState(state);
        adminView.propertyChange(new PropertyChangeEvent(
                viewModel, "state", null, state));

        // Verify post details are displayed - use name instead of border content
        JPanel detailPanel = findComponent(adminView, JPanel.class, comp ->
                comp instanceof JPanel && comp.getName() != null &&
                        comp.getName().equals("postDetailPanel"));

        assertNotNull(detailPanel);
        assertTrue(detailPanel.getComponentCount() > 0);
    }

    @Test
    void testEditPost() {
        // Set up a selected post
        Post testPost = createSamplePost(1, "Test Post");
        AdminState state = new AdminState();
        state.setPosts(Collections.singletonList(testPost));
        state.setSelectedPost(testPost);
        viewModel.setState(state);

        // Set selected post ID
        adminView.setSelectedPost("1");

        // Find and click edit button
        JButton editButton = findComponent(adminView, JButton.class, comp ->
                "Edit Post".equals(((JButton) comp).getText()));
        assertNotNull(editButton);
        editButton.doClick();

        // Verify edit dialog appears
        JDialog dialog = findDialog(Window.getWindows(), "Edit Post");
        assertNotNull(dialog);
    }

    @Test
    void testDeletePost() {
        // Create a post with delete button
        Post testPost = createSamplePost(1, "Test Post");
        AdminState state = new AdminState();
        state.setPosts(Collections.singletonList(testPost));
        viewModel.setState(state);
        adminView.propertyChange(new PropertyChangeEvent(
                viewModel, "state", null, state));

        // Find and click delete button
        JButton deleteButton = findComponent(adminView, JButton.class, comp ->
                "Delete Post".equals(((JButton) comp).getText()));
        assertNotNull(deleteButton);

        // Simulate "Yes" in confirmation dialog
        SwingUtilities.invokeLater(() -> {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof JDialog && window.isVisible()) {
                    JDialog dialog = (JDialog) window;
                    if (dialog.getTitle().equals("Confirm Delete")) {
                        JButton yesButton = findComponent(dialog, JButton.class,
                                comp -> "Yes".equals(((JButton) comp).getText()));
                        if (yesButton != null) {
                            yesButton.doClick();
                        }
                    }
                }
            }
        });

        deleteButton.doClick();
        verify(mockController).deletePost("1");
    }

    // Helper methods
    private Post createSamplePost(int id, String title) {
        return new Post() {
            @Override
            public int getPostID() { return id; }
            @Override
            public String getTitle() { return title; }
            @Override
            public String getDescription() { return "Test description"; }
            @Override
            public String getLocation() { return "Test location"; }
            @Override
            public List<String> getTags() { return Arrays.asList("tag1", "tag2"); }
            @Override
            public boolean isLost() { return true; }
            @Override
            public String getAuthor() { return "Test Author"; }
            @Override
            public String getTimestamp() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.now().format(formatter);
            }
            @Override
            public int getNumberOfLikes() { return 0; }
        };
    }

    private <T extends Component> T findComponent(Container container, Class<T> clazz,
                                                  java.util.function.Predicate<Component> predicate) {
        for (Component comp : container.getComponents()) {
            if (clazz.isInstance(comp) && predicate.test(comp)) {
                return clazz.cast(comp);
            }
            if (comp instanceof Container) {
                T found = findComponent((Container) comp, clazz, predicate);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JDialog findDialog(Window[] windows, String title) {
        for (Window window : windows) {
            if (window instanceof JDialog) {
                JDialog dialog = (JDialog) window;
                if (dialog.getTitle().equals(title)) {
                    return dialog;
                }
            }
        }
        return null;
    }
}