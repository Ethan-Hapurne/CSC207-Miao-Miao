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

                case "advanced_search":
                    // Perform advanced search with specific criteria
                    List<Post> advancedSearchResults = dashboardDataAccessObject.searchPostsByCriteria(
                        dashboardInputData.getPostTitle(),     // title
                        dashboardInputData.getPostLocation(),  // location
                        dashboardInputData.getPostTags(),      // tags
                        dashboardInputData.isLost() ? Boolean.TRUE : null  // isLost
                    );
                    DashboardOutputData advancedOutputData = new DashboardOutputData(advancedSearchResults);
                    dashboardOutputBoundary.prepareSuccessView(advancedOutputData);
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

                case "update_post":
                    if (dashboardInputData.getPost() != null) {
                        boolean updateSuccess = dashboardDataAccessObject.updatePost(dashboardInputData.getPost());
                        if (updateSuccess) {
                            DashboardOutputData updatePostOutputData = new DashboardOutputData("Post updated successfully!", true);
                            dashboardOutputBoundary.prepareSuccessView(updatePostOutputData);
                        } else {
                            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Failed to update post."));
                        }
                    } else {
                        dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Post data is required for update."));
                    }
                    break;

                case "resolve_post":
                    // Handle resolve post action, allowing "0" to mean no credit
                    if (dashboardInputData.getResolvedByUsername() == null) {
                        dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Resolving username is required."));
                        break;
                    }

                    // Get the post to be resolved
                    entity.Post post = dashboardDataAccessObject.getPostById(String.valueOf(dashboardInputData.getPostId()));
                    if (post == null) {
                        dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Post not found."));
                        break;
                    }

                    // Check if post is already resolved
                    if (post.isResolved()) {
                        dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Post is already resolved."));
                        break;
                    }

                    String credited = dashboardInputData.getCreditedUsername();
                    boolean skipCredit = credited != null && credited.trim().equals("0");

                    // Mark post as resolved
                    post.setResolved(true);
                    post.setResolvedBy(dashboardInputData.getResolvedByUsername());
                    post.setCreditedTo(skipCredit ? null : credited);

                    if (skipCredit) {
                        // Only update the post; no user credit
                        boolean postUpdated = dashboardDataAccessObject.updatePost(post);
                        if (postUpdated) {
                            DashboardOutputData resolvePostOutputData = new DashboardOutputData("Post resolved successfully.", true);
                            dashboardOutputBoundary.prepareSuccessView(resolvePostOutputData);
                        } else {
                            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Failed to update post in database."));
                        }
                    } else {
                        // Credit the specified user
                        if (credited == null || credited.trim().isEmpty()) {
                            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Credited username is required or type 0 to skip."));
                            break;
                        }

                        entity.User creditedUser = dashboardDataAccessObject.getUserByUsername(credited.trim());
                        if (creditedUser == null) {
                            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Credited user not found."));
                            break;
                        }

                        creditedUser.addResolvedPost(String.valueOf(dashboardInputData.getPostId()));
                        creditedUser.addCredibilityPoints(1); // Award 1 point for resolving a post

                        boolean postUpdated = dashboardDataAccessObject.updatePost(post);
                        boolean userUpdated = dashboardDataAccessObject.updateUser(creditedUser);

                        if (postUpdated && userUpdated) {
                            String successMessage = String.format(
                                "Post resolved successfully! %s has been credited with 1 credibility point. New credibility score: %d",
                                creditedUser.getName(),
                                creditedUser.getCredibilityScore()
                            );
                            dashboardOutputBoundary.prepareSuccessView(new DashboardOutputData(successMessage, true));
                        } else {
                            dashboardOutputBoundary.prepareFailView(new DashboardOutputData("Failed to update post or user in database."));
                        }
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