package use_case.resolve_post;

/**
 * Output boundary for the resolve post use case.
 */
public interface ResolvePostOutputBoundary {
    /**
     * Prepares the success view when a post is successfully resolved.
     * @param resolvePostOutputData the output data containing success information
     */
    void prepareSuccessView(ResolvePostOutputData resolvePostOutputData);

    /**
     * Prepares the fail view when post resolution fails.
     * @param resolvePostOutputData the output data containing error information
     */
    void prepareFailView(ResolvePostOutputData resolvePostOutputData);
}

