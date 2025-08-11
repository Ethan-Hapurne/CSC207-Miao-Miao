package view;

import entity.Post;
import interface_adapter.admin.*;
import interface_adapter.admin.AdminViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class AdminView extends JPanel implements ActionListener, PropertyChangeListener {
    private final JButton editButton = new JButton("Edit Post");
    private final JDialog editDialog;
    private final JTextField titleField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(5, 20);
    private final JTextField locationField = new JTextField(20);
    private final JTextField tagsField = new JTextField(20);
    private final JCheckBox isLostCheckBox = new JCheckBox("Lost Item");
    private String selectedPostId;
    private final String viewName = "admin";
    private final AdminViewModel adminViewModel;
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");
    private final JButton addPostButton = new JButton("Add Post");
    private final JButton backButton = new JButton("Back");
    private final JPanel postsPanel = new JPanel();
    private final JPanel postDetailPanel = new JPanel();
    private JScrollPane postsScrollPane = new JScrollPane();
    // Note: postsScrollPane is not made final
    private final JTabbedPane tabbedPane;

    private AdminController adminController;

    public AdminView(AdminViewModel adminViewModel) {

        // Create edit dialog
        editDialog = new JDialog();
        editDialog.setTitle("Edit Post");
        editDialog.setModal(true);
        editDialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        // Add form components
        addFormRow(formPanel, "Title:", titleField, gbc, 0);
        addFormRow(formPanel, "Description:", new JScrollPane(descriptionArea), gbc, 1);
        addFormRow(formPanel, "Location:", locationField, gbc, 2);
        addFormRow(formPanel, "Tags:", tagsField, gbc, 3);
        addFormRow(formPanel, "", isLostCheckBox, gbc, 4);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        editDialog.add(formPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        saveButton.addActionListener(e -> saveEdit());
        cancelButton.addActionListener(e -> editDialog.setVisible(false));
        editButton.addActionListener(e -> showEditDialog());

        // Pack dialog
        editDialog.pack();
        editDialog.setLocationRelativeTo(null);
        this.adminViewModel = adminViewModel;
        this.adminViewModel.addPropertyChangeListener(this);

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
                if (adminController != null) {
                    adminController.loadPosts();
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
        buttonPanel.add(editButton);
        buttonPanel.add(addPostButton);
        buttonPanel.add(backButton);

        toolbarPanel.add(searchPanel, BorderLayout.WEST);
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);

        // Add action listeners
        searchButton.addActionListener(evt -> {
            if (evt.getSource().equals(searchButton)) {
                adminController.searchPosts(searchField.getText());
            }
        });

        addPostButton.addActionListener(evt -> {
            if (evt.getSource().equals(addPostButton)) {
                showAddPostDialog();
            }
        });

        backButton.addActionListener(evt -> {
            if (evt.getSource().equals(backButton)) {
                adminController.navigateBack();
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
        postDetailPanel.setName("postDetailPanel");

        // Add a placeholder for post details
        JLabel placeholderLabel = new JLabel("Select a post to view details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        postDetailPanel.add(placeholderLabel, BorderLayout.CENTER);

        postsTab.add(postsScrollPane, BorderLayout.WEST);
        postsTab.add(postDetailPanel, BorderLayout.CENTER);

        return postsTab;
    }

    private void addFormRow(JPanel panel, String label, Component component,
                          GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void showEditDialog() {
        if (selectedPostId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a post to edit first",
                    "No Post Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Populate fields with current post data
        Post post = getCurrentlySelectedPost();
        if (post != null) {
            titleField.setText(post.getTitle());
            descriptionArea.setText(post.getDescription());
            locationField.setText(post.getLocation());
            // Add null check for tags
            tagsField.setText(post.getTags() != null ? String.join(", ", post.getTags()) : "");
            isLostCheckBox.setSelected(post.isLost());
        }

        editDialog.setVisible(true);
    }
    private void saveEdit() {
        List<String> tags = Arrays.asList(tagsField.getText().split("\\s*,\\s*"));

        adminController.editPost(
            selectedPostId,
            titleField.getText(),
            descriptionArea.getText(),
            locationField.getText(),
            tags,
            isLostCheckBox.isSelected()
        );

        editDialog.setVisible(false);
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

            if (titleField.getText().trim().isEmpty() || descriptionArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Title and description are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // Don't proceed with the submission
            }


            List<String> tags = new ArrayList<>();
            if (!tagsText.isEmpty()) {
                tags = Arrays.asList(tagsText.split(","));
            }

            adminController.addPost(title, content, tags, location, isLost);
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
        if (evt.getPropertyName().equals("editSuccess")) {
            JOptionPane.showMessageDialog(this,
                "Post updated successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (evt.getPropertyName().equals("editError")) {
            JOptionPane.showMessageDialog(this,
                "Error updating post: " + evt.getNewValue(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        if (evt.getPropertyName().equals("state")) {
            final AdminState state = (AdminState) evt.getNewValue();

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
                AdminState currentState = adminViewModel.getState();
                currentState.setSuccessMessage("");
                adminViewModel.setState(currentState);
                // Reload posts after successful post creation (but don't trigger another property change)
                SwingUtilities.invokeLater(() -> adminController.loadPosts());
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

        // Add delete button
        JButton deleteButton = createDeleteButton(String.valueOf(post.getPostID()));

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

        JLabel authorLabel = new JLabel("By: " + post.getAuthor());
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
        panel.add(deleteButton);

        // Add click listener to show post details
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showPostDetails(post);
                setSelectedPost(String.valueOf(post.getPostID())); // Add this line
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
        // Add null check for tags
        String tagsText = post.getTags() != null ? String.join(", ", post.getTags()) : "";
        detailsPanel.add(new JLabel(tagsText));

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

    // Handle post selection
    public void setSelectedPost(String postId) {
        this.selectedPostId = postId;
        editButton.setEnabled(postId != null);
    }

    public String getViewName() {
        return viewName;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle any general action events if needed
        // Currently not used as specific action listeners are used for buttons
    }

    private Post getCurrentlySelectedPost() {
        AdminState currentState = adminViewModel.getState();
        List<Post> posts = currentState.getPosts();

        for (Post post : posts) {
            if (post.getPostID() == Integer.parseInt(selectedPostId)) {
                return post;
            }
        }

        return null;
    }

    // Add this method to create a delete button for each post
    private JButton createDeleteButton(String postId) {
        JButton deleteButton = new JButton("Delete Post");
        deleteButton.addActionListener(e -> {
            System.out.println("\n=== Delete Operation Started ===");
            System.out.println("AdminView: Delete button clicked for post ID: " + postId);
        
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this post?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                
            System.out.println("AdminView: User confirmation result: " + 
                          (result == JOptionPane.YES_OPTION ? "YES" : "NO"));
                          
            if (result == JOptionPane.YES_OPTION) {
                try {
                    System.out.println("AdminView: Initiating delete operation through controller");
                    adminController.deletePost(postId);
                } catch (Exception ex) {
                    System.err.println("AdminView: Error during delete operation: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                    "Failed to delete post: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return deleteButton;
    }
}