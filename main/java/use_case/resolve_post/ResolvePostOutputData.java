package use_case.resolve_post;

/**
 * Output data for the resolve post use case.
 */
public class ResolvePostOutputData {
    private final String message;
    private final boolean success;
    private final String creditedUsername;
    private final int newCredibilityScore;

    public ResolvePostOutputData(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.creditedUsername = null;
        this.newCredibilityScore = 0;
    }

    public ResolvePostOutputData(String message, boolean success, String creditedUsername, int newCredibilityScore) {
        this.message = message;
        this.success = success;
        this.creditedUsername = creditedUsername;
        this.newCredibilityScore = newCredibilityScore;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCreditedUsername() {
        return creditedUsername;
    }

    public int getNewCredibilityScore() {
        return newCredibilityScore;
    }
}

