package use_case.dashboard;

import entity.Post;
import java.util.List;

// SESSION CHANGE: Dashboard search returns all posts alphabetically if query is blank. See also: FirebasePostDataAccessObject, DashboardView, LoginPresenter, SignupPresenter, LoggedInView, AppBuilder.

/**
 * Interactor for the dashboard use case.
 * Implements the business logic for dashboard operations.
 */
public class DashboardInteractor implements DashboardInputBoundary {
    private final DashboardUserDataAccessInterface dashboardDataAccessObject;
    private final DashboardOutputBoundary dashboardOutputBoundary;

    public DashboardInteractor(DashboardUserDataAccessInterface dashboardDataAccessObject,
                              DashboardOutputBoundary dashboardOutputBoundary) {
        this.dashboardDataAccessObject = dashboardDataAccessObject;
        this.dashboardOutputBoundary = dashboardOutputBoundary;
    }

    @Override
    public void execute(DashboardInputData dashboardInputData) {
        try {
            switch (dashboardInputData.getAction()) {
                case "load_posts":
                    List<Post> posts = dashboardDataAccessObject.getAllPosts();
                    DashboardOutputData outputData = new DashboardOutputData(posts);
                    dashboardOutputBoundary.prepareSuccessView(outputData);
                    break;

                case "search_posts":
                    // SESSION CHANGE: If search query is blank, return all posts sorted alphabetically by title
                    if (dashboardInputData.getSearchQuery() != null && !dashboardInputData.getSearchQuery().trim().isEmpty()) {
                        List<Post> searchResults = dashboardDataAccessObject.searchPosts(dashboardInputData.getSearchQuery().trim());
                        DashboardOutputData searchOutputData = new DashboardOutputData(searchResults);
                        dashboardOutputBoundary.prepareSuccessView(searchOutputData);
                    } else {
                        List<Post> allPosts = dashboardDataAccessObject.getAllPosts();
                        allPosts.sort(java.util.Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
                        DashboardOutputData allPostsOutputData = new DashboardOutputData(allPosts);
                        dashboardOutputBoundary.prepareSuccessView(allPostsOutputData);
                    }
                    break;

                case "add_post":
                    if (dashboardInputData.getPostTitle() != null && !dashboardInputData.getPostTitle().trim().isEmpty() &&
                        dashboardInputData.getPostContent() != null && !dashboardInputData.getPostContent().trim().isEmpty()) {
                        
                        Post newPost = dashboardDataAccessObject.addPost(
                            dashboardInputData.getPostTitle().trim(),
                            dashboardInputData.getPostContent().trim(),
                            dashboardInputData.getPostTags(),
                            dashboardInputData.getPostLocation(),
                            dashboardInputData.isLost(),
                            dashboardInputData.getAuthor() != null ? dashboardInputData.getAuthor() : "anonymous"
                        );
                        
                        DashboardOutputData addPostOutputData = new DashboardOutputData("Post created successfully!", true);
                        dashboardOutputBoundary.prepareSuccessView(addPostOutputData);
                    } else {
                        dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Post title and content are required."));
                    }
                    break;

                default:
                    dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Invalid action."));
                    break;
            }
        } catch (Exception e) {
            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("An error occurred: " + e.getMessage()));
        }
    }
} 