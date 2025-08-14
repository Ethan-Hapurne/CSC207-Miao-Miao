package use_case.admin;

import entity.Post;
import java.util.List;

/**
 * Output data for the dashboard use case.
 */
public class AdminOutputData {
    private List<Post> posts;
    private Post selectedPost;
    private final String error;
    private final String successMessage;

    public AdminOutputData(List<Post> posts) {
        this.posts = posts;
        this.selectedPost = null;
        this.error = null;
        this.successMessage = null;
    }


    public AdminOutputData(String error) {
        this.posts = null;
        this.selectedPost = null;
        this.error = error;
        this.successMessage = null;
    }

    public AdminOutputData(String successMessage, boolean isSuccess) {
        this.posts = null;
        this.selectedPost = null;
        this.error = null;
        this.successMessage = successMessage;
    }

    // Getters
    public List<Post> getPosts() { return posts; }
    public Post getSelectedPost() { return selectedPost; }
    public String getError() { return error; }
    public String getSuccessMessage() { return successMessage; }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    public void setSelectedPost(Post post) {
        this.selectedPost = post;
    }

}