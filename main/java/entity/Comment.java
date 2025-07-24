package entity;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String id;
    private String username;
    private String content;
    private int likes;
    private List<Comment> replies;

    public Comment() {
        // Required for Firebase
    }

    public Comment(String id, String username, String content) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.likes = 0;
        this.replies = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public void like() { this.likes++; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }
    public void addReply(Comment reply) { this.replies.add(reply); }
} 