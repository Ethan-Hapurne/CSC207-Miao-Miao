package view;

import entity.Post;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The View for Advanced Search functionality.
 */
public class AdvancedSearchView extends JPanel implements PropertyChangeListener {

    private final String viewName = "advanced_search";
    private final SearchViewModel searchViewModel;
    
    // Search fields
    private final JTextField titleField = new JTextField(20);
    private final JTextField locationField = new JTextField(20);
    private final JTextField tagsField = new JTextField(20);
    private final JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Any", "Lost", "Found"});

    
    // UI components
    private final JLabel searchErrorField = new JLabel();
    private final JButton searchButton = new JButton("Advanced Search");
    private final JButton backButton = new JButton("Back");
    private final JPanel resultsPanel = new JPanel();
    private final JScrollPane scrollPane;

    private SearchController searchController;

    public AdvancedSearchView(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
        this.searchViewModel.addPropertyChangeListener(this);
        this.setLayout(new BorderLayout());

        // Top panel with search form
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Advanced Search");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        // Create form panel
        JPanel formPanel = createSearchForm();
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchButton);
        buttonPanel.add(backButton);

        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(formPanel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(searchErrorField);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(buttonPanel);

        // Results panel
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        // Hook up search button
        searchButton.addActionListener(evt -> {
            performAdvancedSearch();
        });

        backButton.addActionListener(evt -> searchController.navigateBack());
    }

    private JPanel createSearchForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        // Location field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        formPanel.add(locationField, gbc);

        // Tags field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Tags (comma-separated):"), gbc);
        gbc.gridx = 1;
        formPanel.add(tagsField, gbc);

        // Type filter
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(typeComboBox, gbc);



        return formPanel;
    }

    private void performAdvancedSearch() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        String tagsText = tagsField.getText().trim();
        String typeSelection = (String) typeComboBox.getSelectedItem();


        // Parse tags
        List<String> tags = new ArrayList<>();
        if (!tagsText.isEmpty()) {
            tags = Arrays.asList(tagsText.split(","));
            // Clean up tags (remove whitespace)
            tags = tags.stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }

        // Parse type filter
        Boolean isLost = null;
        if ("Lost".equals(typeSelection)) {
            isLost = true;
        } else if ("Found".equals(typeSelection)) {
            isLost = false;
        }

        System.out.println("[UI] Advanced search - Title: '" + title + 
                         "', Location: '" + location + 
                         "', Tags: " + tags + 
                         ", Type: " + typeSelection + "'");

        searchController.executeAdvancedSearch(title, location, tags, isLost);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName())) {
            SearchState state = (SearchState) evt.getNewValue();
            searchErrorField.setText(state.getSearchError());
            updateResultsDisplay(state.getSearchResults(), state.isLoading());
        }
    }

    private void updateResultsDisplay(List<Post> posts, boolean isLoading) {
        resultsPanel.removeAll();

        if (isLoading) {
            JLabel loadingLabel = new JLabel("Searching...");
            loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(loadingLabel);
        } else if (posts != null && !posts.isEmpty()) {
            JLabel resultsLabel = new JLabel("Advanced Search Results (" + posts.size() + " posts found):");
            resultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsLabel.setFont(new Font("Arial", Font.BOLD, 14));
            resultsPanel.add(resultsLabel);
            resultsPanel.add(Box.createVerticalStrut(10));

            for (Post post : posts) {
                resultsPanel.add(createPostPanel(post));
                resultsPanel.add(Box.createVerticalStrut(10));
            }
        } else if (posts != null) {
            JLabel noResultsLabel = new JLabel("No posts found matching your search criteria.");
            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(noResultsLabel);
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createPostPanel(Post post) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.setBackground(Color.WHITE);

        String title = post.getTitle() != null ? post.getTitle() : "Untitled";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String description = post.getDescription() != null ? post.getDescription() : "No description available";
        JLabel descLabel = new JLabel(description);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setBackground(Color.WHITE);

        String author = post.getAuthor() != null ? post.getAuthor() : "Unknown";
        String location = post.getLocation() != null ? post.getLocation() : "No location specified";
        String timestamp = post.getTimestamp() != null ? post.getTimestamp() : "Unknown time";

        detailsPanel.add(new JLabel("By: " + author));
        detailsPanel.add(new JLabel("Location: " + location));
        JLabel typeLabel = new JLabel(post.isLost() ? "LOST" : "FOUND");
        typeLabel.setForeground(post.isLost() ? Color.RED : Color.GREEN);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        detailsPanel.add(typeLabel);
        detailsPanel.add(new JLabel("Posted: " + timestamp));

        List<String> tags = post.getTags();
        String tagString = (tags != null && !tags.isEmpty()) ? String.join(", ", tags) : "No tags";
        JLabel tagsLabel = new JLabel("Tags: " + tagString);
        tagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsLabel.setForeground(Color.BLUE);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(detailsPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(tagsLabel);

        return panel;
    }

    public String getViewName() {
        return viewName;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }
}
