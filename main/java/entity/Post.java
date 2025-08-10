package entity;

import com.google.firebase.database.IgnoreExtraProperties; // <— add this
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Represents a post in the lost-and-found system. */
@IgnoreExtraProperties // <— add this
public class Post {
    private int postID;
    private String title;
    private String description;
    private List<String> tags;
    /** Stored as ISO-8601 text (e.g., 2025-08-09T12:34:56) to keep it Firebase/JSON-friendly. */
    private String timestamp;
    private String author;
    private String location;
    private String imageURL;
    private boolean isLost;
    private int numberOfLikes;
    private Map<Integer, String> reactions;
    private List<Comment> comments;
    private boolean resolved;
    private String resolvedBy;
    private String creditedTo;

    /** No-arg constructor required by many serializers (e.g., Firebase/JSON). */
    public Post() {}

    /**
     * Convenience constructor. If {@code timestamp} is non-null, it will be
     * formatted as ISO-8601 and stored as a String to ease persistence.
     */
    public Post(int postID,
                String title,
                String description,
                List<String> tags,
                LocalDateTime timestamp,
                String author,
                String location,
                String imageURL,
                boolean isLost,
                int numberOfLikes,
                Map<Integer, String> reactions) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.timestamp = (timestamp != null)
                ? timestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        this.author = author;
        this.location = location;
        this.imageURL = imageURL;
        this.isLost = isLost;
        this.numberOfLikes = numberOfLikes;
        this.reactions = reactions;
    }

    // -------- Getters / Setters --------

    public int getPostID() { return postID; }
    public void setPostID(int postID) { this.postID = postID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getTimestamp() { return timestamp; }
    /** Allows setting a pre-formatted timestamp string (ISO-8601 recommended). */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public boolean isLost() { return isLost; }
    public void setLost(boolean isLost) { this.isLost = isLost; }

    public int getNumberOfLikes() { return numberOfLikes; }
    public void setNumberOfLikes(int numberOfLikes) { this.numberOfLikes = numberOfLikes; }

    public Map<Integer, String> getReactions() { return reactions; }
    public void setReactions(Map<Integer, String> reactions) { this.reactions = reactions; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getCreditedTo() { return creditedTo; }
    public void setCreditedTo(String creditedTo) { this.creditedTo = creditedTo; }
}
