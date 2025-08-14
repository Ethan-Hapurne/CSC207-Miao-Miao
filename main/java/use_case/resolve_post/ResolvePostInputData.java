package use_case.resolve_post;

/**
 * Input data for the resolve post use case.
 */
public class ResolvePostInputData {
    private final String postId;
    private final String creditedUsername;
    private final String resolvedByUsername;

    public ResolvePostInputData(String postId, String creditedUsername, String resolvedByUsername) {
        this.postId = postId;
        this.creditedUsername = creditedUsername;
        this.resolvedByUsername = resolvedByUsername;
    }

    public String getPostId() {
        return postId;
    }

    public String getCreditedUsername() {
        return creditedUsername;
    }

    public String getResolvedByUsername() {
        return resolvedByUsername;
    }
}

