package entity;

import com.google.firebase.database.IgnoreExtraProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a post in the lost and found system.
 * Firebase-compatible entity.
 */
@IgnoreExtraProperties
public class Post {
    private int postID;
    private String title;
    private String description;
    private List<String> tags;
    private String timestamp; // Store as string for Firebase
    private String author;
    private String location;
    private String imageURL;
    private boolean isLost;
    private int numberOfLikes;
    private Map<Integer, String> reactions;
    private List<Comment> comments;
    
    // Default constructor required for Firebase
    public Post() {}
    
    public Post(int postID, String title, String description, List<String> tags, 
                LocalDateTime timestamp, String author, String location, String imageURL, 
                boolean isLost, int numberOfLikes, Map<Integer, String> reactions) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.timestamp = timestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.author = author;
        this.location = location;
        this.imageURL = imageURL;
        this.isLost = isLost;
        this.numberOfLikes = numberOfLikes;
        this.reactions = reactions;
    }
    
    // Getters and setters (required for Firebase)
    public int getPostID() { return postID; }
    public void setPostID(int postID) { this.postID = postID; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getTimestamp() { return timestamp; }
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
} 