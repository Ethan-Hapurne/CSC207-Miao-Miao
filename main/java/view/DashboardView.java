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

    // SESSION CHANGE: In-memory nested comment storage for demo
    private final Map<Integer, List<CommentNode>> postComments = new HashMap<>();
    private int commentIdCounter = 1;
    private static class CommentNode {
        String username;
        String content;
        int likes;
        int id;
        List<CommentNode> replies = new ArrayList<>();
        CommentNode(String username, String content, int id) {
            this.username = username;
            this.content = content;
            this.likes = 0;
            this.id = id;
        }
    }

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

    private static String formatTimestamp(String timestamp) {
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

        // SESSION CHANGE: Author label now shows 'By username'
        JLabel authorLabel = new JLabel("By " + post.getAuthor());
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? Color.RED : Color.GREEN);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel timeLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));

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

        // Title as bold heading (increase from 22 to 28)
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Details panel (vertical)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Base font size for details: 15 (25% bigger than 12)
        Font detailFont = new Font("Arial", Font.PLAIN, 15);
        Font labelFont = new Font("Arial", Font.BOLD, 15);

        // Content/Description
        JLabel contentLabel = new JLabel("Content: " + post.getDescription());
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentLabel.setFont(detailFont);
        detailsPanel.add(contentLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Tags
        String tags = (post.getTags() != null && !post.getTags().isEmpty()) ? String.join(", ", post.getTags()) : "None";
        JLabel tagsLabel = new JLabel("Tags: " + tags);
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsLabel.setForeground(Color.BLUE);
        tagsLabel.setFont(detailFont);
        detailsPanel.add(tagsLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Location
        JLabel locationLabel = new JLabel("Location: " + post.getLocation());
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationLabel.setFont(detailFont);
        detailsPanel.add(locationLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Type (LOST/FOUND) (increase from 14 to 18)
        JLabel typeLabel = new JLabel("Type: " + (post.isLost() ? "LOST" : "FOUND"));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setForeground(post.isLost() ? Color.RED : Color.GREEN);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        detailsPanel.add(typeLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Posted date/time
        JLabel postedLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        postedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        postedLabel.setFont(detailFont);
        detailsPanel.add(postedLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Author
        JLabel authorLabel = new JLabel("Author: " + post.getAuthor());
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorLabel.setFont(detailFont);
        detailsPanel.add(authorLabel);
        detailsPanel.add(Box.createVerticalStrut(8));

        // Likes
        JLabel likesLabel = new JLabel("Likes: " + post.getNumberOfLikes());
        likesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        likesLabel.setFont(detailFont);
        detailsPanel.add(likesLabel);

        // COMMENT SECTION (in-memory, as before)
        JPanel commentSection = new JPanel(new BorderLayout());
        commentSection.setBorder(BorderFactory.createTitledBorder("Comments"));
        List<CommentNode> comments = postComments.getOrDefault(post.getPostID(), new ArrayList<>());
        JPanel commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        for (CommentNode comment : comments) {
            commentsListPanel.add(createCommentPanel(comment, post.getPostID(), 0));
            commentsListPanel.add(Box.createVerticalStrut(8));
        }
        JScrollPane commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setPreferredSize(new java.awt.Dimension(400, 220));
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentSection.add(commentsScrollPane, BorderLayout.CENTER);
        // Input bar (always at bottom)
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField commentInput = new JTextField();
        JButton postCommentButton = new JButton("Post Comment");
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
        // Layout: details at top, comment section (comments + input bar) at bottom
        postDetailPanel.add(titleLabel, BorderLayout.NORTH);
        postDetailPanel.add(detailsPanel, BorderLayout.CENTER);
        postDetailPanel.add(commentSection, BorderLayout.SOUTH);
        postDetailPanel.revalidate();
        postDetailPanel.repaint();
    }
    // Recursive panel for a comment and its replies
    private JPanel createCommentPanel(CommentNode comment, int postId, int indentLevel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, indentLevel * 30, 0, 0));
        JLabel userLabel = new JLabel(comment.username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(userLabel);
        JLabel contentLabel = new JLabel(comment.content);
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(contentLabel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton likeButton = new JButton("Like (" + comment.likes + ")");
        JButton replyButton = new JButton("Reply");
        actions.add(likeButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(replyButton);
        panel.add(actions);
        likeButton.addActionListener(e -> {
            comment.likes++;
            showPostDetails(findPostById(postId));
        });
        replyButton.addActionListener(e -> {
            JTextField replyInput = new JTextField();
            int result = JOptionPane.showConfirmDialog(panel, replyInput, "Reply to " + comment.username, JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String replyText = replyInput.getText().trim();
                if (!replyText.isEmpty()) {
                    String username = "User" + ((int)(Math.random()*1000));
                    CommentNode reply = new CommentNode(username, replyText, commentIdCounter++);
                    comment.replies.add(reply);
                    showPostDetails(findPostById(postId));
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

    public String getViewName() {
        return viewName;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
}