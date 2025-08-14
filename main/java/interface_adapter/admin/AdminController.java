package interface_adapter.admin;

import interface_adapter.ViewManagerModel;
import use_case.admin.AdminInputBoundary;
import use_case.admin.AdminInputData;
import use_case.admin.AdminInteractor;
import use_case.admin.AdminOutputData;

import java.util.List;

/**
 * The controller for the Dashboard View.
 */
public class AdminController {

    private final AdminInputBoundary adminInputBoundary;
    private final ViewManagerModel viewManagerModel;
    private String currentUser; // Add current user tracking

    public AdminController(AdminInputBoundary adminInteractor, ViewManagerModel viewManagerModel) {
        this.adminInputBoundary = adminInteractor;
        this.viewManagerModel = viewManagerModel;
        this.currentUser = "anonymous"; // Default user
    }

    /**
     * Sets the current logged-in user.
     * @param username the username of the current user
     */
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    /**
     * Loads all posts for the dashboard.
     */
    public void loadPosts() {
        AdminInputData adminInputData = new AdminInputData("load_posts");
        adminInputBoundary.execute(adminInputData);
    }

    /**
     * Searches posts by query.
     * @param searchQuery the search query
     */
    public void searchPosts(String searchQuery) {
        AdminInputData adminInputData = new AdminInputData("search_posts", searchQuery);
        adminInputBoundary.execute(adminInputData);
    }

    /**
     * Adds a new post.
     * @param title the post title
     * @param content the post content
     * @param tags the post tags
     * @param location the post location
     * @param isLost whether it's a lost item
     */
    public void addPost(String title, String content, List<String> tags, String location, boolean isLost) {
        AdminInputData adminInputData = new AdminInputData("add_post", title, content, tags, location, isLost, currentUser);
        adminInputBoundary.execute(adminInputData);
    }
    // Add edit functionality
    public void editPost(String postId, String title, String description,
                         String location, List<String> tags, boolean isLost) {
        System.out.println("AdminController: Attempting to edit post with ID: " + postId);
        System.out.println("AdminController: Edit details - Title: " + title + ", Description length: " + 
                      (description != null ? description.length() : "null") + 
                      ", Tags: " + tags + ", Location: " + location);
    
        AdminInputData inputData = new AdminInputData("edit_post", postId, title, description, tags, location, isLost, currentUser);
        adminInputBoundary.execute(inputData);
    }

    public void deletePost(String postId) {
        System.out.println("\nAdminController: Starting delete operation");
        System.out.println("AdminController: Post ID to delete: " + postId);
        
        if (postId == null || postId.trim().isEmpty()) {
            System.err.println("AdminController: Invalid post ID (null or empty)");
            return;
        }
        
        try {
            System.out.println("AdminController: Creating AdminInputData for delete operation");
            AdminInputData inputData = new AdminInputData("delete_post", postId, true);
            System.out.println("AdminController: Executing delete operation through interactor");
            adminInputBoundary.execute(inputData);
        } catch (Exception e) {
            System.err.println("AdminController: Exception during delete operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

        /**
         * Navigates back to the previous view.
         */
    public void navigateBack() {
        viewManagerModel.popViewOrClose();
    }
}