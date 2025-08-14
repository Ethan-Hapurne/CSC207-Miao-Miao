package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of the User interface.
 */
public class CommonUser implements User {

    // Firebase requires a no-arg constructor for deserialization
    public CommonUser() {
        this.name = "";
        this.password = "";
        this.admin = false;
        this.credibilityScore = 0;
        this.resolvedPosts = new ArrayList<>();
    }

    private String name;
    private String password;
    private boolean admin;
    private int credibilityScore;
    private List<String> resolvedPosts;

    public CommonUser(String name, String password, boolean admin) {
        this.name = name;
        this.password = password;
        this.admin = admin;
        this.credibilityScore = 0;
        this.resolvedPosts = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public int getCredibilityScore() {
        return credibilityScore;
    }

    @Override
    public List<String> getResolvedPosts() {
        return resolvedPosts != null ? resolvedPosts : new ArrayList<>();
    }

    @Override
    public void addResolvedPost(String postId) {
        if (resolvedPosts == null) {
            resolvedPosts = new ArrayList<>();
        }
        if (!resolvedPosts.contains(postId)) {
            resolvedPosts.add(postId);
        }
    }

    @Override
    public void addCredibilityPoints(int points) {
        this.credibilityScore += points;
    }

    // Firebase setters for deserialization
    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setCredibilityScore(int credibilityScore) {
        this.credibilityScore = credibilityScore;
    }

    public void setResolvedPosts(List<String> resolvedPosts) {
        this.resolvedPosts = resolvedPosts;
    }
}
