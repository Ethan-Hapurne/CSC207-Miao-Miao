package use_case.dashboard;

import java.util.List;

/**
 * Input data for the dashboard use case.
 */
public class DashboardInputData {
    private final String action; // "load_posts", "search_posts", "add_post"
    private final String searchQuery;

    private final String postTitle;
    private final String postContent;
    private final List<String> postTags;
    private final String postLocation;
    private final boolean isLost;
    private final String author; // Add author field
    private final entity.Post post; // For update operations
    private final int postId; // For delete operations
    private final String creditedUsername; // For resolve post operations
    private final String resolvedByUsername; // For resolve post operations

    public DashboardInputData(String action) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = null;
        this.postContent = null;
        this.postTags = null;
        this.postLocation = null;
        this.isLost = false;
        this.author = null;
        this.post = null;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }

    public DashboardInputData(String action, String searchQuery) {
        this.action = action;
        this.searchQuery = searchQuery;
        this.postTitle = null;
        this.postContent = null;
        this.postTags = null;
        this.postLocation = null;
        this.isLost = false;
        this.author = null;
        this.post = null;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }



    public DashboardInputData(String action, String title, String location, List<String> tags, Boolean isLost) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = title;
        this.postContent = null;
        this.postTags = tags;
        this.postLocation = location;
        this.isLost = isLost != null ? isLost : false;
        this.author = null;
        this.post = null;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }

    public DashboardInputData(String action, String postTitle, String postContent, 
                            List<String> postTags, String postLocation, boolean isLost) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postTags = postTags;
        this.postLocation = postLocation;
        this.isLost = isLost;
        this.author = null;
        this.post = null;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }
    
    public DashboardInputData(String action, String postTitle, String postContent, 
                            List<String> postTags, String postLocation, boolean isLost, String author) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postTags = postTags;
        this.postLocation = postLocation;
        this.isLost = isLost;
        this.author = author;
        this.post = null;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }
    
    public DashboardInputData(String action, entity.Post post) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = null;
        this.postContent = null;
        this.postTags = null;
        this.postLocation = null;
        this.isLost = false;
        this.author = null;
        this.post = post;
        this.postId = 0;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }
    
    // Make sure there's a constructor that accepts an action and an integer post ID
    public DashboardInputData(String action, int postId) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = null;
        this.postContent = null;
        this.postTags = null;
        this.postLocation = null;
        this.isLost = false;
        this.author = null;
        this.post = null;
        this.postId = postId;
        this.creditedUsername = null;
        this.resolvedByUsername = null;
    }

    public DashboardInputData(String action, String postId, String creditedUsername, String resolvedByUsername) {
        this.action = action;
        this.searchQuery = null;
        this.postTitle = null;
        this.postContent = null;
        this.postTags = null;
        this.postLocation = null;
        this.isLost = false;
        this.author = null;
        this.post = null;
        this.postId = Integer.parseInt(postId);
        this.creditedUsername = creditedUsername;
        this.resolvedByUsername = resolvedByUsername;
    }

    // Getters
    public String getAction() { return action; }
    public String getSearchQuery() { return searchQuery; }

    public String getPostTitle() { return postTitle; }
    public String getPostContent() { return postContent; }
    public List<String> getPostTags() { return postTags; }
    public String getPostLocation() { return postLocation; }
    public boolean isLost() { return isLost; }
    public String getAuthor() { return author; }
    public entity.Post getPost() { return post; }
    
    // Make sure the getPostId method returns the post ID
    public int getPostId() {
        return postId;
    }
    public String getCreditedUsername() { return creditedUsername; }
    public String getResolvedByUsername() { return resolvedByUsername; }
}