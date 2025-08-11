package view;

import interface_adapter.fuzzy_search.FuzzySearchController;
import interface_adapter.fuzzy_search.FuzzySearchState;
import interface_adapter.fuzzy_search.FuzzySearchViewModel;
import interface_adapter.ViewManagerModel;
import entity.Post;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * View for the fuzzy search feature.
 */
public class FuzzySearchView extends JPanel implements ActionListener, PropertyChangeListener {
    public static final String viewName = "fuzzy search";

    private final FuzzySearchViewModel fuzzySearchViewModel;
    private FuzzySearchController fuzzySearchController;
    private final ViewManagerModel viewManagerModel;

    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton backButton;
    private final JLabel messageLabel;
    private final JPanel resultsPanel;
    private final JScrollPane resultsScrollPane;

    public FuzzySearchView(FuzzySearchViewModel fuzzySearchViewModel,
                          FuzzySearchController fuzzySearchController,
                          ViewManagerModel viewManagerModel) {
        this.fuzzySearchViewModel = fuzzySearchViewModel;
        this.fuzzySearchController = fuzzySearchController;
        this.viewManagerModel = viewManagerModel;
        this.fuzzySearchViewModel.addPropertyChangeListener(this);

        // Initialize components
        searchField = new JTextField(30);
        searchButton = new JButton("Search");
        backButton = new JButton("Back");
        messageLabel = new JLabel("");
        resultsPanel = new JPanel();
        resultsScrollPane = new JScrollPane(resultsPanel);

        // Set up the layout
        setupLayout();
        setupActionListeners();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(248, 249, 250));
        
        JLabel titleLabel = new JLabel("Fuzzy Search");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setBackground(new Color(248, 249, 250));
        
        JLabel searchLabel = new JLabel("Search Query:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        messagePanel.setBackground(new Color(248, 249, 250));
        messagePanel.add(messageLabel);

        // Results panel
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        resultsScrollPane.setPreferredSize(new Dimension(600, 400));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.add(backButton);

        // Add all panels to main layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.add(headerPanel);
        topPanel.add(searchPanel);
        topPanel.add(messagePanel);

        add(topPanel, BorderLayout.NORTH);
        add(resultsScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupActionListeners() {
        searchButton.addActionListener(this);
        backButton.addActionListener(this);
        
        // Allow Enter key to trigger search
        searchField.addActionListener(e -> searchButton.doClick());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(searchButton)) {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                fuzzySearchController.executeFuzzySearch(query);
            } else {
                messageLabel.setText("Please enter a search query.");
                messageLabel.setForeground(Color.RED);
            }
        } else if (e.getSource().equals(backButton)) {
            viewManagerModel.popViewOrClose();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            FuzzySearchState state = (FuzzySearchState) evt.getNewValue();
            updateView(state);
        }
    }

    private void updateView(FuzzySearchState state) {
        System.out.println("DEBUG: FuzzySearchView.updateView() called");
        System.out.println("DEBUG: State - success: " + state.isSuccess() + ", message: " + state.getMessage());
        System.out.println("DEBUG: Search results: " + (state.getSearchResults() != null ? state.getSearchResults().size() : "null"));
        
        messageLabel.setText(state.getMessage());
        messageLabel.setForeground(state.isSuccess() ? Color.GREEN : Color.RED);

        // Clear previous results
        resultsPanel.removeAll();

        if (state.isSuccess() && state.getSearchResults() != null && !state.getSearchResults().isEmpty()) {
            System.out.println("DEBUG: Displaying " + state.getSearchResults().size() + " search results");
            displaySearchResults(state.getSearchResults());
        } else {
            System.out.println("DEBUG: No results to display");
            // Show a message when no results
            JLabel noResultsLabel = new JLabel("No search results found.");
            noResultsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            noResultsLabel.setForeground(Color.GRAY);
            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(noResultsLabel);
        }

        // Refresh the view
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void displaySearchResults(List<Post> posts) {
        System.out.println("DEBUG: displaySearchResults called with " + posts.size() + " posts");
        for (Post post : posts) {
            System.out.println("DEBUG: Creating panel for post: " + post.getTitle());
            JPanel postPanel = createPostPanel(post);
            resultsPanel.add(postPanel);
            resultsPanel.add(Box.createVerticalStrut(10));
        }
        System.out.println("DEBUG: Added " + posts.size() + " post panels to resultsPanel");
    }

    private JPanel createPostPanel(Post post) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel(post.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Preview of content
        String contentPreview = post.getDescription();
        if (contentPreview != null && contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        JLabel contentLabel = new JLabel(contentPreview != null ? contentPreview : "No description");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(Color.BLACK);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Details
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setOpaque(false);

        // Author label
        String authorText = post.getAuthor() != null ? post.getAuthor() : "Anonymous";
        JLabel authorLabel = new JLabel("👤 " + authorText);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        authorLabel.setForeground(new Color(0, 123, 255));
        
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? new Color(220, 53, 69) : new Color(40, 167, 69));
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel timeLabel = new JLabel("Posted: " + formatTimestamp(post.getTimestamp()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);

        detailsPanel.add(authorLabel);
        detailsPanel.add(typeLabel);
        detailsPanel.add(timeLabel);

        // Add all components
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(contentLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(detailsPanel);

        return panel;
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "Unknown time";
        }
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(timestamp);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
            return dt.format(formatter);
        } catch (Exception e) {
            return timestamp; // fallback to original if parse fails
        }
    }

    public void setFuzzySearchController(FuzzySearchController controller) {
        this.fuzzySearchController = controller;
    }
}
