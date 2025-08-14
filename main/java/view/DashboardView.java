package view;

import data_access.FirebaseUserDataAccessObject;
import entity.Comment;
import entity.Post;
import interface_adapter.dashboard.DashboardController;
import interface_adapter.dashboard.DashboardState;
import interface_adapter.dashboard.DashboardViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;


/**
 * The View for the Dashboard (Piazza-like platform).
 */
public class DashboardView extends JPanel implements PropertyChangeListener {

    // Constants for magic numbers
    private static final int SEARCH_FIELD_COLUMNS = 20;
    private static final int FONT_SIZE_14 = 14;
    private static final int FONT_SIZE_12 = 12;
    private static final int FONT_SIZE_16 = 16;
    private static final int FONT_SIZE_18 = 18;
    private static final int FONT_SIZE_24 = 24;
    private static final int PADDING_8 = 8;
    private static final int PADDING_10 = 10;
    private static final int PADDING_12 = 12;
    private static final int PADDING_15 = 15;
    private static final int PADDING_16 = 16;
    private static final int PADDING_20 = 20;
    private static final int PADDING_30 = 30;
    private static final int MARGIN_5 = 5;
    private static final int MARGIN_10 = 10;
    private static final int MARGIN_15 = 15;
    private static final int MARGIN_20 = 20;
    private static final int MARGIN_8 = 8;
    private static final int MARGIN_12 = 12;
    private static final int PADDING_2 = 2;
    private static final int WINDOW_WIDTH_300 = 300;
    private static final int WINDOW_WIDTH_400 = 400;
    private static final int WINDOW_WIDTH_550 = 550;
    private static final int WINDOW_WIDTH_500 = 500;
    private static final int WINDOW_WIDTH_600 = 600;
    private static final int WINDOW_HEIGHT_100 = 100;
    private static final int WINDOW_HEIGHT_200 = 200;
    private static final int WINDOW_HEIGHT_250 = 250;
    private static final int WINDOW_HEIGHT_300 = 300;
    private static final int WINDOW_HEIGHT_400 = 400;
    private static final int WINDOW_HEIGHT_500 = 500;
    private static final int WINDOW_HEIGHT_600 = 600;
    private static final int BUTTON_HEIGHT_35 = 35;
    private static final int BUTTON_HEIGHT_40 = 40;
    private static final int BUTTON_HEIGHT_50 = 50;
    private static final int BUTTON_WIDTH_80 = 80;
    private static final int BUTTON_WIDTH_100 = 100;
    private static final int BUTTON_WIDTH_120 = 120;
    private static final int BUTTON_WIDTH_150 = 150;
    private static final int BUTTON_WIDTH_200 = 200;
    private static final int BUTTON_WIDTH_300 = 300;
    private static final int BUTTON_WIDTH_400 = 400;
    private static final int BUTTON_WIDTH_500 = 500;
    private static final int TEXT_AREA_ROWS_3 = 3;
    private static final int TEXT_AREA_ROWS_4 = 4;
    private static final int TEXT_AREA_ROWS_5 = 5;
    private static final int TEXT_AREA_ROWS_6 = 6;
    private static final int TEXT_AREA_ROWS_8 = 8;
    private static final int TEXT_AREA_ROWS_10 = 10;
    private static final int TEXT_AREA_COLUMNS_20 = 20;
    private static final int TEXT_AREA_COLUMNS_30 = 30;
    private static final int TEXT_AREA_COLUMNS_40 = 40;
    private static final int TEXT_AREA_COLUMNS_50 = 50;
    private static final int TEXT_AREA_COLUMNS_60 = 60;
    private static final int TEXT_AREA_COLUMNS_80 = 80;
    private static final int TEXT_AREA_COLUMNS_100 = 100;
    private static final int SCROLL_SPEED_1000 = 1000;
    private static final int MAX_METHOD_LENGTH = 150;

    // Color constants
    private static final Color LIGHT_GRAY_BACKGROUND = new Color(248, 249, 250);
    private static final Color WHITE_COLOR = new Color(255, 255, 255);
    private static final Color DARK_TEXT_COLOR = new Color(33, 37, 41);
    private static final Color PRIMARY_BLUE = new Color(0, 123, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color LIGHT_BLUE = new Color(222, 226, 230);
    private static final Color DARK_GRAY = new Color(108, 117, 125);
    private static final Color LIGHT_GREEN = new Color(123, 255, 123);
    private static final Color LIGHT_RED = new Color(255, 193, 193);
    private static final Color LIGHT_YELLOW = new Color(255, 255, 193);
    private static final Color LIGHT_CYAN = new Color(193, 255, 255);
    private static final Color LIGHT_MAGENTA = new Color(255, 193, 255);
    private static final Color LIGHT_ORANGE = new Color(255, 193, 7);
    private static final Color LIGHT_PURPLE = new Color(193, 193, 255);
    private static final Color LIGHT_PINK = new Color(255, 193, 193);
    private static final Color LIGHT_BROWN = new Color(193, 193, 193);
    private static final Color LIGHT_BLACK = new Color(0, 0, 0);
    private static final Color LIGHT_WHITE = new Color(255, 255, 255);

    private final String viewName = "dashboard";
    private final DashboardViewModel dashboardViewModel;
    private final JTextField searchField = new JTextField(SEARCH_FIELD_COLUMNS);

    private final JComboBox<String> searchCriteriaDropdown = new JComboBox<>(new String[]{
        "General Search", "Title", "Location", "Tags", "Lost Items", "Found Items"
    });
    private JButton searchButton;
    private JButton addPostButton;
    private JButton backButton;
    private final JPanel postsPanel = new JPanel();
    private final JPanel postDetailPanel = new JPanel();
    private JScrollPane postsScrollPane = new JScrollPane();
    
    // My Posts components
    private JPanel myPostsPanel;
    private JPanel myPostDetailPanel;
    private JScrollPane myPostsScrollPane;
    // Note: postsScrollPane is not made final
    private final JTabbedPane tabbedPane;

    private DashboardController dashboardController;

    // SESSION CHANGE: In-memory nested comment storage for demo
    private final Map<Integer, List<CommentNode>> postComments = new HashMap<>();
    private int commentIdCounter = 1;
    private Post currentPost = null; // Store the currently displayed post
    private final Set<Integer> likedPosts = new HashSet<>(); // Track which posts have been liked
    private String currentUser = null; // Track the current user
    
    /**
     * Represents a comment node in the comment tree structure.
     */
    private static class CommentNode {
        private final String username;
        private final String content;
        private int likes;
        private final int id;
        private final List<CommentNode> replies = new ArrayList<>();
        
        /**
         * Creates a new comment node.
         * @param username the username of the commenter
         * @param content the content of the comment
         * @param id the unique identifier for the comment
         */
        CommentNode(final String username, final String content, final int id) {
            this.username = username;
            this.content = content;
            this.likes = 0;
            this.id = id;
        }
    }

    /**
     * Creates a new DashboardView.
     * @param dashboardViewModel the view model for the dashboard
     */
    public DashboardView(final DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
        this.dashboardViewModel.addPropertyChangeListener(this);

        // Set up the main layout with modern styling
        this.setLayout(new BorderLayout());
        this.setBackground(LIGHT_GRAY_BACKGROUND);

        // Create top toolbar
        JPanel toolbarPanel = createToolbarPanel();

        // Create main content area with tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        tabbedPane.setBackground(WHITE_COLOR);
        tabbedPane.setForeground(DARK_TEXT_COLOR);

        // Posts tabs
        JPanel postsTab = createPostsTab();
        tabbedPane.addTab("General Postings", postsTab);
        tabbedPane.addTab("My Posts", createMyPostsTab());

        // Add components to main panel
        this.add(toolbarPanel, BorderLayout.NORTH);
        this.add(tabbedPane, BorderLayout.CENTER);

        // Add component listener to detect when view becomes visible
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Load posts when the view becomes visible
                if (dashboardController != null) {
                    dashboardController.loadPosts();
                }
            }
        });
    }

    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(WHITE_COLOR);
        toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_BLUE),
            BorderFactory.createEmptyBorder(PADDING_15, PADDING_20, PADDING_15, PADDING_20)
        ));

        // Left side - search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGIN_10, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        searchLabel.setForeground(DARK_TEXT_COLOR);
        
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        searchField.setPreferredSize(new Dimension(WINDOW_WIDTH_400, BUTTON_HEIGHT_35));
        

        
        // Style the search criteria dropdown
        searchCriteriaDropdown.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        searchCriteriaDropdown.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        searchCriteriaDropdown.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE, 1));
        
        searchButton = createStyledButton("Search", PRIMARY_BLUE);
        searchButton.setPreferredSize(new Dimension(BUTTON_WIDTH_80, BUTTON_HEIGHT_35));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchCriteriaDropdown);

        searchPanel.add(searchButton);

        // Right side - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, MARGIN_10, 0));
        buttonPanel.setOpaque(false);
        
        addPostButton = createStyledButton("+ Add Post", SUCCESS_GREEN);
        addPostButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        
        backButton = createStyledButton("Back", DARK_GRAY);
        backButton.setPreferredSize(new Dimension(BUTTON_WIDTH_80, BUTTON_HEIGHT_35));

        buttonPanel.add(addPostButton);
        buttonPanel.add(backButton);

        toolbarPanel.add(searchPanel, BorderLayout.WEST);
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);

        // Add action listeners
        searchButton.addActionListener(evt -> {
            if (evt.getSource().equals(searchButton)) {
                String searchQuery = searchField.getText().trim();
        
                String selectedCriteria = (String) searchCriteriaDropdown.getSelectedItem();
                
                performCriteriaSearch(searchQuery, selectedCriteria);
            }
        });

        addPostButton.addActionListener(evt -> {
            if (evt.getSource().equals(addPostButton)) {
                showAddPostDialog();
            }
        });

        backButton.addActionListener(evt -> {
            if (evt.getSource().equals(backButton)) {
                dashboardController.navigateBack();
            }
        });

        return toolbarPanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
        button.setForeground(DARK_TEXT_COLOR);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_15, PADDING_8, PADDING_15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker().darker(), 1),
                    BorderFactory.createEmptyBorder(PADDING_8, PADDING_15, PADDING_8, PADDING_15)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                    BorderFactory.createEmptyBorder(PADDING_8, PADDING_15, PADDING_8, PADDING_15)
                ));
            }
        });

        return button;
    }

    private JPanel createPostsTab() {
        JPanel postsTab = new JPanel(new BorderLayout());
        postsTab.setBackground(LIGHT_GRAY_BACKGROUND);

        // Posts list on the left
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        postsPanel.setBackground(WHITE_COLOR);
        postsPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, PADDING_10, PADDING_10));
        
        postsScrollPane = new JScrollPane(postsPanel);
        postsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        postsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        postsScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH_400, WINDOW_HEIGHT_600));
        postsScrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE, 1));

        // Post detail panel on the right
        postDetailPanel.setLayout(new BorderLayout());
        postDetailPanel.setBackground(WHITE_COLOR);
        postDetailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_15, PADDING_15, PADDING_15, PADDING_15)
        ));
        postDetailPanel.setPreferredSize(new Dimension(WINDOW_WIDTH_550, WINDOW_HEIGHT_600));

        // Add a placeholder for post details
        JLabel placeholderLabel = new JLabel("Select a post to view details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, FONT_SIZE_16));
        placeholderLabel.setForeground(DARK_TEXT_COLOR);
        postDetailPanel.add(placeholderLabel, BorderLayout.CENTER);

        postsTab.add(postsScrollPane, BorderLayout.WEST);
        postsTab.add(postDetailPanel, BorderLayout.CENTER);

        return postsTab;
    }

    private JPanel createMyPostsTab() {
        JPanel myPostsTab = new JPanel(new BorderLayout());
        myPostsTab.setBackground(LIGHT_GRAY_BACKGROUND);

        // My posts list on the left
        JPanel myPostsPanel = new JPanel();
        myPostsPanel.setLayout(new BoxLayout(myPostsPanel, BoxLayout.Y_AXIS));
        myPostsPanel.setBackground(WHITE_COLOR);
        myPostsPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, PADDING_10, PADDING_10));
        
        JScrollPane myPostsScrollPane = new JScrollPane(myPostsPanel);
        myPostsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        myPostsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        myPostsScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH_400, WINDOW_HEIGHT_600));
        myPostsScrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE, 1));

        // My post detail panel on the right
        JPanel myPostDetailPanel = new JPanel();
        myPostDetailPanel.setLayout(new BorderLayout());
        myPostDetailPanel.setBackground(WHITE_COLOR);
        myPostDetailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_15, PADDING_15, PADDING_15, PADDING_15)
        ));
        myPostDetailPanel.setPreferredSize(new Dimension(WINDOW_WIDTH_550, WINDOW_HEIGHT_600));

        // Add a placeholder for my post details
        JLabel placeholderLabel = new JLabel("Select your post to view/edit details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, FONT_SIZE_16));
        placeholderLabel.setForeground(DARK_TEXT_COLOR);
        myPostDetailPanel.add(placeholderLabel, BorderLayout.CENTER);

        myPostsTab.add(myPostsScrollPane, BorderLayout.WEST);
        myPostsTab.add(myPostDetailPanel, BorderLayout.CENTER);

        // Store references for later use
        this.myPostsPanel = myPostsPanel;
        this.myPostDetailPanel = myPostDetailPanel;
        this.myPostsScrollPane = myPostsScrollPane;

        return myPostsTab;
    }

    private void showAddPostDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Post", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(WINDOW_WIDTH_600, WINDOW_HEIGHT_500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(LIGHT_GRAY_BACKGROUND);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, PADDING_20, PADDING_20, PADDING_20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PADDING_8, PADDING_8, PADDING_8, PADDING_8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(TEXT_AREA_COLUMNS_20);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(titleField, gbc);

        // Content field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel contentLabel = new JLabel("Content:");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(contentLabel, gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(TEXT_AREA_ROWS_4, TEXT_AREA_COLUMNS_20);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        contentArea.setLineWrap(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        formPanel.add(contentScrollPane, gbc);

        // Tags field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel tagsLabel = new JLabel("Tags (comma-separated):");
        tagsLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(tagsLabel, gbc);
        gbc.gridx = 1;
        JTextField tagsField = new JTextField(TEXT_AREA_COLUMNS_20);
        tagsField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        tagsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(tagsField, gbc);

        // Location field
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(locationLabel, gbc);
        gbc.gridx = 1;
        JTextField locationField = new JTextField(TEXT_AREA_COLUMNS_20);
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        locationField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(locationField, gbc);

        // Lost/Found radio buttons
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setOpaque(false);
        JRadioButton lostButton = new JRadioButton("Lost", true);
        JRadioButton foundButton = new JRadioButton("Found");
        lostButton.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        foundButton.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(lostButton);
        typeGroup.add(foundButton);
        typePanel.add(lostButton);
        typePanel.add(foundButton);
        formPanel.add(typePanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, MARGIN_10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, 0, 0, 0));
        
        JButton submitButton = createStyledButton("Submit", SUCCESS_GREEN);
        JButton cancelButton = createStyledButton("Cancel", DARK_GRAY);
        submitButton.setPreferredSize(new Dimension(BUTTON_WIDTH_100, BUTTON_HEIGHT_35));
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH_100, BUTTON_HEIGHT_35));

        submitButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String tagsText = tagsField.getText().trim();
            String location = locationField.getText().trim();
            boolean isLost = lostButton.isSelected();

            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and content are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> tags = new ArrayList<>();
            if (!tagsText.isEmpty()) {
                tags = Arrays.asList(tagsText.split(","));
            }

            dashboardController.addPost(title, content, tags, location, isLost);
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            final DashboardState state = (DashboardState) evt.getNewValue();

            // Update posts list
            updatePostsList(state.getPosts());

            // Update selected post details
            updatePostDetails(state.getSelectedPost());

            // Show error or success messages
            if (!state.getError().isEmpty()) {
                JOptionPane.showMessageDialog(this, state.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (!state.getSuccessMessage().isEmpty()) {
                System.out.println("DEBUG: Success message received: '" + state.getSuccessMessage() + "'");
                JOptionPane.showMessageDialog(this, state.getSuccessMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear the success message after showing it
                DashboardState currentState = dashboardViewModel.getState();
                currentState.setSuccessMessage("");
                dashboardViewModel.setState(currentState);
                // Reload posts after successful post creation (but don't trigger another property change)
                SwingUtilities.invokeLater(() -> {
                    System.out.println("DEBUG: Reloading posts after success message");
                    dashboardController.loadPosts();
                });
            }
        }
    }

    private void updatePostsList(List<Post> posts) {
        postsPanel.removeAll();

        if (posts != null && !posts.isEmpty()) {
            // Sort posts by timestamp in descending order (most recent first)
            List<Post> sortedPosts = new ArrayList<>(posts);
            sortedPosts.sort((p1, p2) -> {
                try {
                    LocalDateTime dt1 = LocalDateTime.parse(p1.getTimestamp());
                    LocalDateTime dt2 = LocalDateTime.parse(p2.getTimestamp());
                    return dt2.compareTo(dt1); // Reverse order (most recent first)
                } catch (Exception e) {
                    return 0; // Keep original order if timestamps can't be parsed
                }
            });
            
            for (Post post : sortedPosts) {
                postsPanel.add(createPostListItem(post));
                postsPanel.add(Box.createVerticalStrut(MARGIN_8));
            }
        } else {
            JLabel noPostsLabel = new JLabel("No posts found.");
            noPostsLabel.setFont(new Font("Segoe UI", Font.ITALIC, FONT_SIZE_16));
            noPostsLabel.setForeground(DARK_TEXT_COLOR);
            noPostsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            postsPanel.add(noPostsLabel);
        }

        postsPanel.revalidate();
        postsPanel.repaint();
        
        // Also update the My Posts list
        updateMyPostsList(posts);
    }

    private static String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "Unknown time";
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(timestamp);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
            return dt.format(formatter);
        } catch (DateTimeParseException | NullPointerException e) {
            return timestamp; // fallback to original if parse fails
        }
    }

    private JPanel createPostListItem(Post post) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_15, PADDING_15, PADDING_15, PADDING_15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT_35 * 2)); // Adjusted for list item height
        panel.setBackground(WHITE_COLOR);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_16));
        titleLabel.setForeground(DARK_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Preview of content
        String contentPreview = post.getDescription();
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        JLabel contentLabel = new JLabel(contentPreview);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        contentLabel.setForeground(DARK_TEXT_COLOR);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Details
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGIN_15, 0));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setOpaque(false);

        // Author label with enhanced styling - make it clickable
        String authorText = post.getAuthor() != null ? post.getAuthor() : "Anonymous";
        JLabel authorLabel = new JLabel("👤 " + authorText);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
        authorLabel.setForeground(PRIMARY_BLUE); // Blue color for prominence
        authorLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!"Anonymous".equals(authorText)) {
                    showUserProfile(authorText);
                }
            }
        });
        
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? DANGER_RED : SUCCESS_GREEN);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));

        JLabel timeLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        timeLabel.setForeground(DARK_TEXT_COLOR);

        detailsPanel.add(authorLabel);
        detailsPanel.add(typeLabel);
        detailsPanel.add(timeLabel);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(MARGIN_8));
        panel.add(contentLabel);
        panel.add(Box.createVerticalStrut(MARGIN_8));
        panel.add(detailsPanel);

        // Add click listener to show post details
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showPostDetails(post);
            }
        });

        return panel;
    }

    private void showPostDetails(Post post) {
        currentPost = post; // Store the current post
        postDetailPanel.removeAll();
        postDetailPanel.setLayout(new BorderLayout());

        // Title as bold heading
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_24));
        titleLabel.setForeground(DARK_TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, PADDING_20, PADDING_10));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Details panel (vertical)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, PADDING_30, PADDING_20, PADDING_30));
        detailsPanel.setOpaque(false);

        Font detailFont = new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14);
        Font labelFont = new Font("Segoe UI", Font.BOLD, FONT_SIZE_14);

        // Content/Description
        JLabel contentLabel = new JLabel("Content: " + post.getDescription());
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentLabel.setFont(detailFont);
        contentLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(contentLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Tags
        String tags = (post.getTags() != null && !post.getTags().isEmpty()) ? String.join(", ", post.getTags()) : "None";
        JLabel tagsLabel = new JLabel("Tags: " + tags);
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsLabel.setForeground(PRIMARY_BLUE);
        tagsLabel.setFont(detailFont);
        detailsPanel.add(tagsLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Location
        JLabel locationLabel = new JLabel("Location: " + post.getLocation());
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationLabel.setFont(detailFont);
        locationLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(locationLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Type (LOST/FOUND)
        JLabel typeLabel = new JLabel("Type: " + (post.isLost() ? "LOST" : "FOUND"));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setForeground(post.isLost() ? DANGER_RED : SUCCESS_GREEN);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_16));
        detailsPanel.add(typeLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Posted date/time
        JLabel postedLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        postedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        postedLabel.setFont(detailFont);
        postedLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(postedLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Author with credibility points
        String authorText = post.getAuthor() != null ? post.getAuthor() : "Anonymous";
        String credibilityText = "";
        if (!"Anonymous".equals(authorText)) {
            try {
                // Try to get user credibility from Firebase
                data_access.FirebaseUserDataAccessObject userDAO = new data_access.FirebaseUserDataAccessObject();
                entity.User authorUser = userDAO.get(authorText);
                if (authorUser != null) {
                    credibilityText = " (Credibility: " + authorUser.getCredibilityScore() + " pts)";
                }
            } catch (Exception e) {
                // If we can't get user info, just continue without credibility
            }
        }
        JLabel authorLabel = new JLabel("👤 Posted by: " + authorText + credibilityText);
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        authorLabel.setForeground(PRIMARY_BLUE);
        detailsPanel.add(authorLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Likes
        JLabel likesLabel = new JLabel("Likes: " + post.getNumberOfLikes());
        likesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        likesLabel.setFont(detailFont);
        likesLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(likesLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
        
        // Like button for the post
        JButton postLikeButton = createStyledButton("❤ Like Post", DANGER_RED);
        postLikeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        postLikeButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        detailsPanel.add(postLikeButton);
        
        // Add like functionality
        postLikeButton.addActionListener(e -> {
            // Check if this post has already been liked
            if (!likedPosts.contains(post.getPostID())) {
                // Increment likes
                post.setNumberOfLikes(post.getNumberOfLikes() + 1);
                likedPosts.add(post.getPostID());
                // Update button text to show it's been liked
                postLikeButton.setText("❤ Liked!");
                postLikeButton.setBackground(SUCCESS_GREEN); // Green color for liked
                // Refresh the display to show updated like count
                showPostDetails(post);
            } else {
                // Post already liked, show a message
                JOptionPane.showMessageDialog(this, "You've already liked this post!", "Already Liked", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Resolve Post button (only show if post is not already resolved AND it's the current user's post)
        if (!post.isResolved() && currentUser != null && currentUser.equals(post.getAuthor())) {
            detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
            JButton resolvePostButton = createStyledButton("✅ Resolve Post", SUCCESS_GREEN);
            resolvePostButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            resolvePostButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
            detailsPanel.add(resolvePostButton);
            
            // Add resolve functionality
            resolvePostButton.addActionListener(e -> {
                showResolvePostDialog(post);
            });
        } else if (post.isResolved()) {
            // Show resolution info if post is already resolved
            detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
            JLabel resolvedLabel = new JLabel("✅ Resolved by: " + post.getResolvedBy());
            resolvedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            resolvedLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
            resolvedLabel.setForeground(SUCCESS_GREEN);
            detailsPanel.add(resolvedLabel);
            
            if (post.getCreditedTo() != null) {
                JLabel creditedLabel = new JLabel("Credited to: " + post.getCreditedTo());
                creditedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                creditedLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
                creditedLabel.setForeground(DARK_GRAY);
                detailsPanel.add(creditedLabel);
            }
        }

        // COMMENT SECTION (in-memory, as before)
        JPanel commentSection = new JPanel(new BorderLayout());
        commentSection.setBorder(BorderFactory.createTitledBorder("Comments"));
        commentSection.setBackground(LIGHT_GRAY_BACKGROUND);
        List<CommentNode> comments = postComments.getOrDefault(post.getPostID(), new ArrayList<>());
        JPanel commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(LIGHT_GRAY_BACKGROUND);
        for (CommentNode comment : comments) {
            commentsListPanel.add(createCommentPanel(comment, post.getPostID(), 0));
            commentsListPanel.add(Box.createVerticalStrut(MARGIN_8));
        }
        JScrollPane commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setPreferredSize(new java.awt.Dimension(WINDOW_WIDTH_400, WINDOW_HEIGHT_100)); // Reduced from 150 to 100
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentSection.add(commentsScrollPane, BorderLayout.CENTER);
        // Input bar (always at bottom)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(WHITE_COLOR);
        JTextField commentInput = new JTextField();
        commentInput.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        commentInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        JButton postCommentButton = createStyledButton("Post Comment", PRIMARY_BLUE);
        postCommentButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        inputPanel.add(commentInput, BorderLayout.CENTER);
        inputPanel.add(postCommentButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add a comment"));
        commentSection.add(inputPanel, BorderLayout.SOUTH);
        // Post comment action (simulate username as 'UserX')
        postCommentButton.addActionListener(e -> {
            String text = commentInput.getText().trim();
            if (!text.isEmpty()) {
                String username = "User" + ((int)(Math.random()*1000));
                CommentNode newComment = new CommentNode(username, text, commentIdCounter++);
                postComments.computeIfAbsent(post.getPostID(), k -> new ArrayList<>()).add(newComment);
                showPostDetails(post); // Refresh details to show new comment
            }
        });
        // Wrap the details panel in a scroll pane
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH_400, WINDOW_HEIGHT_300)); // Set a reasonable size
        
        // Layout: title at top, scrollable details in center, comment section at bottom
        postDetailPanel.add(titleLabel, BorderLayout.NORTH);
        postDetailPanel.add(detailsScrollPane, BorderLayout.CENTER);
        
        // Create a bottom container with spacing and comment section
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        
        // Add some spacing between details and comment section
        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH_400, MARGIN_15));
        spacerPanel.setOpaque(false);
        
        bottomContainer.add(spacerPanel, BorderLayout.NORTH);
        bottomContainer.add(commentSection, BorderLayout.CENTER);
        
        postDetailPanel.add(bottomContainer, BorderLayout.SOUTH);
        postDetailPanel.revalidate();
        postDetailPanel.repaint();
    }
    
    // Recursive panel for a comment and its replies
    private JPanel createCommentPanel(CommentNode comment, int postId, int indentLevel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, indentLevel * 30, 0, 0));
        panel.setBackground(WHITE_COLOR);
        
        JLabel userLabel = new JLabel(comment.username);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        userLabel.setForeground(DARK_TEXT_COLOR);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(userLabel);
        
        JLabel contentLabel = new JLabel(comment.content);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        contentLabel.setForeground(DARK_TEXT_COLOR);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(contentLabel);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton likeButton = new JButton("Like (" + comment.likes + ")");
        likeButton.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        likeButton.setForeground(PRIMARY_BLUE);
        likeButton.setBorder(BorderFactory.createEmptyBorder());
        likeButton.setContentAreaFilled(false);
        likeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton replyButton = new JButton("Reply");
        replyButton.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        replyButton.setForeground(PRIMARY_BLUE);
        replyButton.setBorder(BorderFactory.createEmptyBorder());
        replyButton.setContentAreaFilled(false);
        replyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        actions.add(likeButton);
        actions.add(Box.createHorizontalStrut(MARGIN_8));
        actions.add(replyButton);
        panel.add(actions);
        
        likeButton.addActionListener(e -> {
            comment.likes++;
            if (currentPost != null) {
                showPostDetails(currentPost);
            }
        });
        
        replyButton.addActionListener(e -> {
            JTextField replyInput = new JTextField();
            replyInput.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
            int result = JOptionPane.showConfirmDialog(panel, replyInput, "Reply to " + comment.username, JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String replyText = replyInput.getText().trim();
                if (!replyText.isEmpty()) {
                    String username = "User" + ((int)(Math.random()*1000));
                    CommentNode reply = new CommentNode(username, replyText, commentIdCounter++);
                    comment.replies.add(reply);
                    if (currentPost != null) {
                        showPostDetails(currentPost);
                    }
                }
            }
        });
        
        for (CommentNode reply : comment.replies) {
            panel.add(createCommentPanel(reply, postId, indentLevel + 1));
        }
        return panel;
    }

    // Helper to find a post by ID (for demo, just search the current list)
    private Post findPostById(int postId) {
        for (Component comp : postsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                for (Component c : p.getComponents()) {
                    if (c instanceof JLabel) {
                        JLabel l = (JLabel) c;
                        try {
                            if (Integer.parseInt(l.getText()) == postId) {
                                return new Post(postId, "", "", new ArrayList<>(), java.time.LocalDateTime.now(), "", "", "", true, 0, new HashMap<>());
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        // Fallback: return a dummy post
        return new Post(postId, "", "", new ArrayList<>(), java.time.LocalDateTime.now(), "", "", "", true, 0, new HashMap<>());
    }

    private void updatePostDetails(Post post) {
        if (post != null) {
            showPostDetails(post);
        }
    }

    private void updateMyPostsList(List<Post> allPosts) {
        if (allPosts == null) {
            System.out.println("DEBUG: updateMyPostsList() called with null posts list");
            return;
        }
        System.out.println("DEBUG: updateMyPostsList() called with " + allPosts.size() + " posts, currentUser: '" + currentUser + "'");
        if (myPostsPanel == null || currentUser == null) {
            System.out.println("DEBUG: Skipping updateMyPostsList - myPostsPanel: " + (myPostsPanel == null) + ", currentUser: " + (currentUser == null));
            return;
        }
        
        myPostsPanel.removeAll();
        
        // Filter posts to show only current user's posts
        List<Post> myPosts = allPosts.stream()
            .filter(post -> {
                boolean matches = currentUser.equals(post.getAuthor());
                System.out.println("DEBUG: Post '" + post.getTitle() + "' by '" + post.getAuthor() + "' matches currentUser '" + currentUser + "': " + matches);
                return matches;
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (myPosts.isEmpty()) {
            JLabel noPostsLabel = new JLabel("You haven't created any posts yet.");
            noPostsLabel.setFont(new Font("Segoe UI", Font.ITALIC, FONT_SIZE_16));
            noPostsLabel.setForeground(DARK_TEXT_COLOR);
            noPostsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            myPostsPanel.add(noPostsLabel);
        } else {
            // Sort my posts by timestamp in descending order (most recent first)
            myPosts.sort((p1, p2) -> {
                try {
                    LocalDateTime dt1 = LocalDateTime.parse(p1.getTimestamp());
                    LocalDateTime dt2 = LocalDateTime.parse(p2.getTimestamp());
                    return dt2.compareTo(dt1); // Reverse order (most recent first)
                } catch (Exception e) {
                    return 0; // Keep original order if timestamps can't be parsed
                }
            });
            
            for (Post post : myPosts) {
                JPanel postItem = createMyPostListItem(post);
                myPostsPanel.add(postItem);
                myPostsPanel.add(Box.createVerticalStrut(MARGIN_8));
            }
        }
        
        myPostsPanel.revalidate();
        myPostsPanel.repaint();
    }

    private JPanel createMyPostListItem(Post post) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(WHITE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_15, PADDING_15, PADDING_15, PADDING_15)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_16));
        titleLabel.setForeground(DARK_TEXT_COLOR);

        // Type badge
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
        typeLabel.setForeground(post.isLost() ? DANGER_RED : SUCCESS_GREEN);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(PADDING_2, MARGIN_8, PADDING_2, MARGIN_8));

        // Top row: title and type
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(typeLabel, BorderLayout.EAST);

        // Bottom row: author, timestamp and likes
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        
        // Author info
        String authorText = post.getAuthor() != null ? post.getAuthor() : "Anonymous";
        JLabel authorLabel = new JLabel("👤 " + authorText);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
        authorLabel.setForeground(PRIMARY_BLUE);
        
        JLabel timestampLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        timestampLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        timestampLabel.setForeground(DARK_TEXT_COLOR);
        
        JLabel likesLabel = new JLabel("Likes: " + post.getNumberOfLikes());
        likesLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
        likesLabel.setForeground(DARK_TEXT_COLOR);

        // Create a left panel with author and timestamp
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(authorLabel);
        leftPanel.add(Box.createHorizontalStrut(MARGIN_15));
        leftPanel.add(timestampLabel);

        bottomRow.add(leftPanel, BorderLayout.WEST);
        bottomRow.add(likesLabel, BorderLayout.EAST);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(bottomRow, BorderLayout.SOUTH);

        // Add click listener to show post details
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showMyPostDetails(post);
            }
        });

        return panel;
    }

    private void showMyPostDetails(Post post) {
        currentPost = post; // Store the current post
        myPostDetailPanel.removeAll();
        myPostDetailPanel.setLayout(new BorderLayout());

        // Title as bold heading
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_24));
        titleLabel.setForeground(DARK_TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, PADDING_20, PADDING_10));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Details panel (vertical)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, PADDING_30, PADDING_20, PADDING_30));
        detailsPanel.setOpaque(false);

        Font detailFont = new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14);
        Font labelFont = new Font("Segoe UI", Font.BOLD, FONT_SIZE_14);

        // Content/Description
        JLabel contentLabel = new JLabel("Content: " + post.getDescription());
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentLabel.setFont(detailFont);
        contentLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(contentLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Tags
        String tags = (post.getTags() != null && !post.getTags().isEmpty()) ? String.join(", ", post.getTags()) : "None";
        JLabel tagsLabel = new JLabel("Tags: " + tags);
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsLabel.setForeground(PRIMARY_BLUE);
        tagsLabel.setFont(detailFont);
        detailsPanel.add(tagsLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Location
        JLabel locationLabel = new JLabel("Location: " + post.getLocation());
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationLabel.setFont(detailFont);
        locationLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(locationLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Type (LOST/FOUND)
        JLabel typeLabel = new JLabel("Type: " + (post.isLost() ? "LOST" : "FOUND"));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setForeground(post.isLost() ? DANGER_RED : SUCCESS_GREEN);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_16));
        detailsPanel.add(typeLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Posted date/time
        JLabel postedLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        postedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        postedLabel.setFont(detailFont);
        postedLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(postedLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Author with credibility points
        String authorText = post.getAuthor() != null ? post.getAuthor() : "Anonymous";
        String credibilityText = "";
        if (!"Anonymous".equals(authorText)) {
            try {
                // Try to get user credibility from Firebase
                data_access.FirebaseUserDataAccessObject userDAO = new data_access.FirebaseUserDataAccessObject();
                entity.User authorUser = userDAO.get(authorText);
                if (authorUser != null) {
                    credibilityText = " (Credibility: " + authorUser.getCredibilityScore() + " pts)";
                }
            } catch (Exception e) {
                // If we can't get user info, just continue without credibility
            }
        }
        JLabel authorLabel = new JLabel("👤 Posted by: " + authorText + credibilityText);
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        authorLabel.setForeground(PRIMARY_BLUE);
        detailsPanel.add(authorLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_12));

        // Likes
        JLabel likesLabel = new JLabel("Likes: " + post.getNumberOfLikes());
        likesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        likesLabel.setFont(detailFont);
        likesLabel.setForeground(DARK_TEXT_COLOR);
        detailsPanel.add(likesLabel);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
        
        // Edit button for the post
        JButton editPostButton = createStyledButton("✏️ Edit Post", WARNING_ORANGE);
        editPostButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        editPostButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        detailsPanel.add(editPostButton);
        detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
        
        // Resolve Post button (only show if post is not already resolved)
        if (!post.isResolved()) {
            JButton resolvePostButton = createStyledButton("✅ Resolve Post", SUCCESS_GREEN);
            resolvePostButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            resolvePostButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
            detailsPanel.add(resolvePostButton);
            detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
            
            // Add resolve functionality
            resolvePostButton.addActionListener(e -> {
                showResolvePostDialog(post);
            });
        } else {
            // Show resolution info if post is already resolved
            JLabel resolvedLabel = new JLabel("✅ Resolved by: " + post.getResolvedBy());
            resolvedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            resolvedLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_12));
            resolvedLabel.setForeground(SUCCESS_GREEN);
            detailsPanel.add(resolvedLabel);
            detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
            
            if (post.getCreditedTo() != null) {
                JLabel creditedLabel = new JLabel("Credited to: " + post.getCreditedTo());
                creditedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                creditedLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_12));
                creditedLabel.setForeground(DARK_GRAY);
                detailsPanel.add(creditedLabel);
                detailsPanel.add(Box.createVerticalStrut(MARGIN_8));
            }
        }

        // Delete button for the post
        JButton deletePostButton = createStyledButton("🗑️ Delete Post", DANGER_RED);
        deletePostButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deletePostButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_35));
        detailsPanel.add(deletePostButton);

        // Add edit functionality
        editPostButton.addActionListener(e -> {
            showEditPostDialog(post);
        });
        
        // Add delete functionality
        deletePostButton.addActionListener(e -> {
            // Check if the current user is the author of the post
            if (currentUser == null || !currentUser.equals(post.getAuthor())) {
                JOptionPane.showMessageDialog(this,
                    "You can only delete your own posts!",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this post?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                // Call the controller to delete the post
                if (dashboardController != null) {
                    dashboardController.deletePost(post.getPostID());
                    // After successful deletion, refresh the posts list
                    dashboardController.loadPosts();
                }
            }
        });

        // Wrap the details panel in a scroll pane
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH_400, WINDOW_HEIGHT_300)); // Set a reasonable size
        
        // Layout: title at top, scrollable details in center
        myPostDetailPanel.add(titleLabel, BorderLayout.NORTH);
        myPostDetailPanel.add(detailsScrollPane, BorderLayout.CENTER);
        myPostDetailPanel.revalidate();
        myPostDetailPanel.repaint();
    }

    private void showEditPostDialog(Post post) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Post", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(WINDOW_WIDTH_600, WINDOW_HEIGHT_500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(LIGHT_GRAY_BACKGROUND);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, PADDING_20, PADDING_20, PADDING_20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PADDING_8, PADDING_8, PADDING_8, PADDING_8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(post.getTitle(), TEXT_AREA_COLUMNS_20);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(titleField, gbc);

        // Content field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel contentLabel = new JLabel("Content:");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(contentLabel, gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(post.getDescription(), TEXT_AREA_ROWS_4, TEXT_AREA_COLUMNS_20);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH_300, WINDOW_HEIGHT_100));
        formPanel.add(contentScrollPane, gbc);

        // Tags field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel tagsLabel = new JLabel("Tags:");
        tagsLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(tagsLabel, gbc);
        gbc.gridx = 1;
        String tagsText = (post.getTags() != null) ? String.join(", ", post.getTags()) : "";
        JTextField tagsField = new JTextField(tagsText, TEXT_AREA_COLUMNS_20);
        tagsField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        tagsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(tagsField, gbc);

        // Location field
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(locationLabel, gbc);
        gbc.gridx = 1;
        JTextField locationField = new JTextField(post.getLocation(), TEXT_AREA_COLUMNS_20);
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        locationField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(locationField, gbc);

        // Type selection
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setOpaque(false);
        JRadioButton lostButton = new JRadioButton("Lost", post.isLost());
        JRadioButton foundButton = new JRadioButton("Found", !post.isLost());
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(lostButton);
        typeGroup.add(foundButton);
        typePanel.add(lostButton);
        typePanel.add(foundButton);
        formPanel.add(typePanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton saveButton = createStyledButton("Save Changes", SUCCESS_GREEN);
        JButton cancelButton = createStyledButton("Cancel", DARK_GRAY);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            // Validate input
            String newTitle = titleField.getText().trim();
            String newContent = contentArea.getText().trim();
            String newLocation = locationField.getText().trim();
            
            if (newTitle.isEmpty() || newContent.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Title and content are required!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update the post object
            post.setTitle(newTitle);
            post.setDescription(newContent);
            post.setLocation(newLocation);
            post.setLost(lostButton.isSelected());
            
            // Parse tags
            String newTagsText = tagsField.getText().trim();
            if (!newTagsText.isEmpty()) {
                List<String> newTags = Arrays.asList(newTagsText.split(","));
                post.setTags(newTags);
            } else {
                post.setTags(new ArrayList<>());
            }
            
            // Call the controller to update the post
            if (dashboardController != null) {
                dashboardController.updatePost(post);
            }
            
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void performCriteriaSearch(String searchQuery, String criteria) {
        if (searchQuery.isEmpty()) {
            // If no query, load all posts
            dashboardController.loadPosts();
            return;
        }

        switch (criteria) {
            case "General Search":
                // Use general search (existing functionality)
                dashboardController.searchPosts(searchQuery);
                break;
                
            case "Title":
                // Search specifically in titles using advanced search
                dashboardController.executeAdvancedSearch(searchQuery, "", new ArrayList<>(), null);
                break;
                
            case "Location":
                // Search specifically in locations using advanced search
                dashboardController.executeAdvancedSearch("", searchQuery, new ArrayList<>(), null);
                break;
                
            case "Tags":
                // Search specifically in tags using advanced search
                List<String> tagsList = Arrays.asList(searchQuery.split(","));
                tagsList = tagsList.stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
                dashboardController.executeAdvancedSearch("", "", tagsList, null);
                break;
                
            case "Lost Items":
                // Search for lost items only
                dashboardController.executeAdvancedSearch(searchQuery, "", new ArrayList<>(), true);
                break;
                
            case "Found Items":
                // Search for found items only
                dashboardController.executeAdvancedSearch(searchQuery, "", new ArrayList<>(), false);
                break;
                
            default:
                // Default to general search
                dashboardController.searchPosts(searchQuery);
                break;
        }
    }

    public String getViewName() {
        return viewName;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setCurrentUser(String username) {
        this.currentUser = username;
        System.out.println("DEBUG: DashboardView.setCurrentUser() called with: '" + username + "'");
    }

    private void showUserProfile(String username) {
        // This will be handled by the ViewManager to navigate to user profile
        System.out.println("DEBUG: Requesting to show profile for user: " + username);
        // For now, just show a message - in a full implementation, this would navigate to the user profile view
        JOptionPane.showMessageDialog(this, 
            "User Profile: " + username + "\n\nThis feature is coming soon!", 
            "User Profile", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showResolvePostDialog(Post post) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resolve Post", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(WINDOW_WIDTH_500, WINDOW_HEIGHT_250); // Increased dialog size
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(LIGHT_GRAY_BACKGROUND);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, PADDING_20, PADDING_20, PADDING_20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PADDING_8, PADDING_8, PADDING_8, PADDING_8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username to credit
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel creditLabel = new JLabel("Credit this user (type 0 for none):");
        creditLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_14));
        formPanel.add(creditLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Make the text field expand
        JTextField creditField = new JTextField(TEXT_AREA_COLUMNS_30); // Increased columns
        creditField.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_14));
        creditField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            BorderFactory.createEmptyBorder(PADDING_8, PADDING_12, PADDING_8, PADDING_12)
        ));
        formPanel.add(creditField, gbc);
        gbc.weightx = 0.0; // Reset weight

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, MARGIN_10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_20, 0, 0, 0));
        
        JButton resolveButton = createStyledButton("Resolve Post", SUCCESS_GREEN);
        JButton cancelButton = createStyledButton("Cancel", DARK_GRAY);
        resolveButton.setPreferredSize(new Dimension(BUTTON_WIDTH_150, BUTTON_HEIGHT_40)); // Increased button size
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH_120, BUTTON_HEIGHT_40)); // Increased button size

        resolveButton.addActionListener(e -> {
            String creditedUsername = creditField.getText().trim();
            if (creditedUsername.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a username to credit, or type 0 to skip.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Call the controller to resolve the post
            if (dashboardController != null) {
                dashboardController.resolvePost(String.valueOf(post.getPostID()), creditedUsername, currentUser);
            }
            
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(resolveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}