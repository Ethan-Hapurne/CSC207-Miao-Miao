package view;

import entity.Post;
import interface_adapter.dashboard.DashboardController;
import interface_adapter.dashboard.DashboardState;
import interface_adapter.dashboard.DashboardViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The View for the Dashboard (Piazza-like platform).
 */
public class DashboardView extends JPanel implements PropertyChangeListener {

    private final String viewName = "dashboard";
    private final DashboardViewModel dashboardViewModel;
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");
    private final JButton addPostButton = new JButton("Add Post");
    private final JButton backButton = new JButton("Back");
    private final JPanel postsPanel = new JPanel();
    private final JPanel postDetailPanel = new JPanel();
    private JScrollPane postsScrollPane = new JScrollPane();
    // Note: postsScrollPane is not made final
    private final JTabbedPane tabbedPane;

    private DashboardController dashboardController;

    public DashboardView(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
        this.dashboardViewModel.addPropertyChangeListener(this);

        // Set up the main layout
        this.setLayout(new BorderLayout());

        // Create top toolbar
        JPanel toolbarPanel = createToolbarPanel();

        // Create main content area with tabs
        tabbedPane = new JTabbedPane();

        // Posts tab
        JPanel postsTab = createPostsTab();
        tabbedPane.addTab("General Postings", postsTab);

        // Add more tabs as needed
        tabbedPane.addTab("My Posts", new JPanel());
        tabbedPane.addTab("Settings", new JPanel());

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
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Right side - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addPostButton);
        buttonPanel.add(backButton);

        toolbarPanel.add(searchPanel, BorderLayout.WEST);
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);

        // Add action listeners
        searchButton.addActionListener(evt -> {
            if (evt.getSource().equals(searchButton)) {
                dashboardController.searchPosts(searchField.getText());
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

    private JPanel createPostsTab() {
        JPanel postsTab = new JPanel(new BorderLayout());

        // Posts list on the left
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        postsScrollPane = new JScrollPane(postsPanel);
        postsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        postsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        postsScrollPane.setPreferredSize(new Dimension(400, 600));

        // Post detail panel on the right
        postDetailPanel.setLayout(new BorderLayout());
        postDetailPanel.setBorder(BorderFactory.createTitledBorder("Post Details"));
        postDetailPanel.setPreferredSize(new Dimension(500, 600));

        // Add a placeholder for post details
        JLabel placeholderLabel = new JLabel("Select a post to view details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        postDetailPanel.add(placeholderLabel, BorderLayout.CENTER);

        postsTab.add(postsScrollPane, BorderLayout.WEST);
        postsTab.add(postDetailPanel, BorderLayout.CENTER);

        return postsTab;
    }

    private void showAddPostDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Post", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        // Content field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Content:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(5, 20);
        contentArea.setLineWrap(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        formPanel.add(contentScrollPane, gbc);

        // Tags field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Tags (comma-separated):"), gbc);
        gbc.gridx = 1;
        JTextField tagsField = new JTextField(20);
        formPanel.add(tagsField, gbc);

        // Location field
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        JTextField locationField = new JTextField(20);
        formPanel.add(locationField, gbc);

        // Lost/Found radio buttons
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton lostButton = new JRadioButton("Lost", true);
        JRadioButton foundButton = new JRadioButton("Found");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(lostButton);
        typeGroup.add(foundButton);
        typePanel.add(lostButton);
        typePanel.add(foundButton);
        formPanel.add(typePanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

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
                JOptionPane.showMessageDialog(this, state.getSuccessMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear the success message after showing it
                DashboardState currentState = dashboardViewModel.getState();
                currentState.setSuccessMessage("");
                dashboardViewModel.setState(currentState);
                // Reload posts after successful post creation (but don't trigger another property change)
                SwingUtilities.invokeLater(() -> dashboardController.loadPosts());
            }
        }
    }

    private void updatePostsList(List<Post> posts) {
        postsPanel.removeAll();

        if (posts != null && !posts.isEmpty()) {
            for (Post post : posts) {
                postsPanel.add(createPostListItem(post));
                postsPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel noPostsLabel = new JLabel("No posts found.");
            noPostsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            postsPanel.add(noPostsLabel);
        }

        postsPanel.revalidate();
        postsPanel.repaint();
    }

    private JPanel createPostListItem(Post post) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setBackground(Color.WHITE);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Preview of content
        String contentPreview = post.getDescription();
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        JLabel contentLabel = new JLabel(contentPreview);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Details
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setBackground(Color.WHITE);

        JLabel authorLabel = new JLabel("By " + post.getAuthor());
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? Color.RED : Color.GREEN);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel timeLabel = new JLabel("Posted: " +
            post.getTimestamp());

        detailsPanel.add(authorLabel);
        detailsPanel.add(typeLabel);
        detailsPanel.add(timeLabel);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(contentLabel);
        panel.add(Box.createVerticalStrut(5));
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
        postDetailPanel.removeAll();
        postDetailPanel.setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content
        JTextArea contentArea = new JTextArea(post.getDescription());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(postDetailPanel.getBackground());
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        // Details panel
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailsPanel.add(new JLabel("Author:"));
        detailsPanel.add(new JLabel(post.getAuthor()));

        detailsPanel.add(new JLabel("Location:"));
        detailsPanel.add(new JLabel(post.getLocation()));

        detailsPanel.add(new JLabel("Type:"));
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? Color.RED : Color.GREEN);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        detailsPanel.add(typeLabel);

        detailsPanel.add(new JLabel("Posted:"));
        detailsPanel.add(new JLabel(post.getTimestamp()));

        detailsPanel.add(new JLabel("Tags:"));
        detailsPanel.add(new JLabel(String.join(", ", post.getTags())));

        detailsPanel.add(new JLabel("Likes:"));
        detailsPanel.add(new JLabel(String.valueOf(post.getNumberOfLikes())));

        postDetailPanel.add(titleLabel, BorderLayout.NORTH);
        postDetailPanel.add(contentScrollPane, BorderLayout.CENTER);
        postDetailPanel.add(detailsPanel, BorderLayout.SOUTH);

        postDetailPanel.revalidate();
        postDetailPanel.repaint();
    }

    private void updatePostDetails(Post post) {
        if (post != null) {
            showPostDetails(post);
        }
    }

    public String getViewName() {
        return viewName;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
}