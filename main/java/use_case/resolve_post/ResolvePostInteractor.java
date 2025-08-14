package use_case.resolve_post;

import entity.Post;
import entity.User;

/**
 * Interactor for the resolve post use case.
 * Implements the business logic for resolving posts and crediting users.
 */
public class ResolvePostInteractor implements ResolvePostInputBoundary {
    private final ResolvePostUserDataAccessInterface resolvePostDataAccessObject;
    private final ResolvePostOutputBoundary resolvePostOutputBoundary;

    public ResolvePostInteractor(ResolvePostUserDataAccessInterface resolvePostDataAccessObject,
                                ResolvePostOutputBoundary resolvePostOutputBoundary) {
        this.resolvePostDataAccessObject = resolvePostDataAccessObject;
        this.resolvePostOutputBoundary = resolvePostOutputBoundary;
    }

    @Override
    public void execute(ResolvePostInputData resolvePostInputData) {
        try {
            // Get the post to be resolved
            Post post = resolvePostDataAccessObject.getPostById(resolvePostInputData.getPostId());
            if (post == null) {
                resolvePostOutputBoundary.prepareFailView(
                    new ResolvePostOutputData("Post not found.", false)
                );
                return;
            }

            // Check if post is already resolved
            if (post.isResolved()) {
                resolvePostOutputBoundary.prepareFailView(
                    new ResolvePostOutputData("Post is already resolved.", false)
                );
                return;
            }

            // Get the user to be credited
            User creditedUser = resolvePostDataAccessObject.getUserByUsername(resolvePostInputData.getCreditedUsername());
            if (creditedUser == null) {
                resolvePostOutputBoundary.prepareFailView(
                    new ResolvePostOutputData("Credited user not found.", false)
                );
                return;
            }

            // Get the user who is resolving the post
            User resolvingUser = resolvePostDataAccessObject.getUserByUsername(resolvePostInputData.getResolvedByUsername());
            if (resolvingUser == null) {
                resolvePostOutputBoundary.prepareFailView(
                    new ResolvePostOutputData("Resolving user not found.", false)
                );
                return;
            }

            // Mark post as resolved
            post.setResolved(true);
            post.setResolvedBy(resolvePostInputData.getResolvedByUsername());
            post.setCreditedTo(resolvePostInputData.getCreditedUsername());

            // Credit the user
            creditedUser.addResolvedPost(resolvePostInputData.getPostId());
            creditedUser.addCredibilityPoints(10); // Award 10 points for resolving a post

            // Update both post and user in database
            boolean postUpdated = resolvePostDataAccessObject.updatePost(post);
            boolean userUpdated = resolvePostDataAccessObject.updateUser(creditedUser);

            if (postUpdated && userUpdated) {
                String successMessage = String.format(
                    "Post resolved successfully! %s has been credited with 10 credibility points. " +
                    "New credibility score: %d", 
                    creditedUser.getName(), 
                    creditedUser.getCredibilityScore()
                );
                
                resolvePostOutputBoundary.prepareSuccessView(
                    new ResolvePostOutputData(successMessage, true, creditedUser.getName(), creditedUser.getCredibilityScore())
                );
            } else {
                resolvePostOutputBoundary.prepareFailView(
                    new ResolvePostOutputData("Failed to update post or user in database.", false)
                );
            }

        } catch (Exception e) {
            resolvePostOutputBoundary.prepareFailView(
                new ResolvePostOutputData("An error occurred: " + e.getMessage(), false)
            );
        }
    }
}

