package use_case.resolve_post;

/**
 * Input boundary for the resolve post use case.
 */
public interface ResolvePostInputBoundary {
    /**
     * Resolves a post and credits the specified user.
     * @param resolvePostInputData the input data containing post and user information
     */
    void execute(ResolvePostInputData resolvePostInputData);
}

