package interface_adapter.dashboard;

import interface_adapter.ViewManagerModel;
import use_case.dashboard.DashboardInputBoundary;
import use_case.dashboard.DashboardInputData;
import entity.Post;
import java.util.List;

/**
 * The controller for the Dashboard View.
 */
public class DashboardController {

    private final DashboardInputBoundary dashboardInteractor;
    private final ViewManagerModel viewManagerModel;
    private String currentUser; // Add current user tracking
    private view.DashboardView dashboardView; // Reference to DashboardView

    public DashboardController(DashboardInputBoundary dashboardInteractor, ViewManagerModel viewManagerModel) {
        this.dashboardInteractor = dashboardInteractor;
        this.viewManagerModel = viewManagerModel;
        this.currentUser = "anonymous"; // Default user
    }
    
    /**
     * Sets the current logged-in user.
     * @param username the username of the current user
     */
    public void setCurrentUser(String username) {
        this.currentUser = username;
        // Also set the current user in the DashboardView
        if (dashboardView != null) {
            dashboardView.setCurrentUser(username);
        }
    }
    
    /**
     * Sets the DashboardView reference.
     * @param dashboardView the DashboardView instance
     */
    public void setDashboardView(view.DashboardView dashboardView) {
        this.dashboardView = dashboardView;
    }

    /**
     * Loads all posts for the dashboard.
     */
    public void loadPosts() {
        DashboardInputData dashboardInputData = new DashboardInputData("load_posts");
        dashboardInteractor.execute(dashboardInputData);
    }

    /**
     * Searches posts by query.
     * @param searchQuery the search query
     */
    public void searchPosts(String searchQuery) {
        DashboardInputData dashboardInputData = new DashboardInputData("search_posts", searchQuery);
        dashboardInteractor.execute(dashboardInputData);
    }



    /**
     * Executes advanced search with specific criteria.
     * @param title title to search for
     * @param location location to search for  
     * @param tags tags to search for
     * @param isLost whether to search for lost or found items
     */
    public void executeAdvancedSearch(String title, String location, java.util.List<String> tags, Boolean isLost) {
        // Use a specific action for advanced search that the interactor can handle
        DashboardInputData dashboardInputData = new DashboardInputData("advanced_search", title, location, tags, isLost);
        dashboardInteractor.execute(dashboardInputData);
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
        DashboardInputData dashboardInputData = new DashboardInputData("add_post", title, content, tags, location, isLost, currentUser);
        dashboardInteractor.execute(dashboardInputData);
    }

    /**
     * Navigates back to the previous view.
     */
    public void navigateBack() {
        viewManagerModel.popViewOrClose();
    }
    
    /**
     * Updates an existing post.
     * @param post the post to update
     */
    public void updatePost(Post post) {
        DashboardInputData dashboardInputData = new DashboardInputData("update_post", post);
        dashboardInteractor.execute(dashboardInputData);
    }
    
    /**
     * Deletes a post.
     * @param postId the ID of the post to delete
     */
    public void deletePost(int postId) {
        DashboardInputData dashboardInputData = new DashboardInputData("delete_post", postId);
        dashboardInteractor.execute(dashboardInputData);
    }

    /**
     * Resolves a post and credits a user.
     * @param postId the ID of the post to resolve
     * @param creditedUsername the username to credit
     * @param resolvedByUsername the username of the person resolving the post
     */
    public void resolvePost(String postId, String creditedUsername, String resolvedByUsername) {
        DashboardInputData dashboardInputData = new DashboardInputData("resolve_post", postId, creditedUsername, resolvedByUsername);
        dashboardInteractor.execute(dashboardInputData);
    }
}
