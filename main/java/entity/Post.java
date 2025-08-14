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
    private boolean resolved;
    private String resolvedBy;
    private String creditedTo;
    
    /**
     * Default constructor required for Firebase.
     */
    public Post() {}
    
    /**
     * Creates a new Post with the specified parameters.
     * @param postID the unique identifier for the post
     * @param title the title of the post
     * @param description the description/content of the post
     * @param tags the list of tags associated with the post
     * @param timestamp the timestamp when the post was created
     * @param author the author of the post
     * @param location the location associated with the post
     * @param imageURL the URL of the image associated with the post
     * @param isLost whether the post is for a lost item (true) or found item (false)
     * @param numberOfLikes the number of likes on the post
     * @param reactions the map of reactions on the post
     */
    public Post(final int postID, final String title, final String description, 
                final List<String> tags, final LocalDateTime timestamp, final String author, 
                final String location, final String imageURL, final boolean isLost, 
                final int numberOfLikes, final Map<Integer, String> reactions) {
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
    
    /**
     * Gets the post ID.
     * @return the post ID
     */
    public int getPostID() { 
        return postID; 
    }
    
    /**
     * Sets the post ID.
     * @param postID the post ID to set
     */
    public void setPostID(final int postID) { 
        this.postID = postID; 
    }
    
    /**
     * Gets the title.
     * @return the title
     */
    public String getTitle() { 
        return title; 
    }
    
    /**
     * Sets the title.
     * @param title the title to set
     */
    public void setTitle(final String title) { 
        this.title = title; 
    }
    
    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() { 
        return description; 
    }
    
    /**
     * Sets the description.
     * @param description the description to set
     */
    public void setDescription(final String description) { 
        this.description = description; 
    }
    
    /**
     * Gets the tags.
     * @return the tags
     */
    public List<String> getTags() { 
        return tags; 
    }
    
    /**
     * Sets the tags.
     * @param tags the tags to set
     */
    public void setTags(final List<String> tags) { 
        this.tags = tags; 
    }
    
    /**
     * Gets the timestamp.
     * @return the timestamp
     */
    public String getTimestamp() { 
        return timestamp; 
    }
    
    /**
     * Sets the timestamp.
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(final String timestamp) { 
        this.timestamp = timestamp; 
    }
    
    /**
     * Gets the author.
     * @return the author
     */
    public String getAuthor() { 
        return author; 
    }
    
    /**
     * Sets the author.
     * @param author the author to set
     */
    public void setAuthor(final String author) { 
        this.author = author; 
    }
    
    /**
     * Gets the location.
     * @return the location
     */
    public String getLocation() { 
        return location; 
    }
    
    /**
     * Sets the location.
     * @param location the location to set
     */
    public void setLocation(final String location) { 
        this.location = location; 
    }
    
    /**
     * Gets the image URL.
     * @return the image URL
     */
    public String getImageURL() { 
        return imageURL; 
    }
    
    /**
     * Sets the image URL.
     * @param imageURL the image URL to set
     */
    public void setImageURL(final String imageURL) { 
        this.imageURL = imageURL; 
    }
    
    /**
     * Checks if the post is for a lost item.
     * @return true if the post is for a lost item, false otherwise
     */
    public boolean isLost() { 
        return isLost; 
    }
    
    /**
     * Sets whether the post is for a lost item.
     * @param isLost true if the post is for a lost item, false otherwise
     */
    public void setLost(final boolean isLost) { 
        this.isLost = isLost; 
    }
    
    /**
     * Gets the number of likes.
     * @return the number of likes
     */
    public int getNumberOfLikes() { 
        return numberOfLikes; 
    }
    
    /**
     * Sets the number of likes.
     * @param numberOfLikes the number of likes to set
     */
    public void setNumberOfLikes(final int numberOfLikes) { 
        this.numberOfLikes = numberOfLikes; 
    }
    
    /**
     * Gets the reactions.
     * @return the reactions
     */
    public Map<Integer, String> getReactions() { 
        return reactions; 
    }
    
    /**
     * Sets the reactions.
     * @param reactions the reactions to set
     */
    public void setReactions(final Map<Integer, String> reactions) { 
        this.reactions = reactions; 
    }

    /**
     * Gets the comments.
     * @return the comments
     */
    public List<Comment> getComments() { 
        return comments; 
    }
    
    /**
     * Sets the comments.
     * @param comments the comments to set
     */
    public void setComments(final List<Comment> comments) { 
        this.comments = comments; 
    }

    /**
     * Checks if the post is resolved.
     * @return true if the post is resolved, false otherwise
     */
    public boolean isResolved() { 
        return resolved; 
    }
    
    /**
     * Sets whether the post is resolved.
     * @param resolved true if the post is resolved, false otherwise
     */
    public void setResolved(final boolean resolved) { 
        this.resolved = resolved; 
    }

    /**
     * Gets the user who resolved the post.
     * @return the user who resolved the post
     */
    public String getResolvedBy() { 
        return resolvedBy; 
    }
    
    /**
     * Sets the user who resolved the post.
     * @param resolvedBy the user who resolved the post
     */
    public void setResolvedBy(final String resolvedBy) { 
        this.resolvedBy = resolvedBy; 
    }

    /**
     * Gets the user credited for the resolution.
     * @return the user credited for the resolution
     */
    public String getCreditedTo() { 
        return creditedTo; 
    }
    
    /**
     * Sets the user credited for the resolution.
     * @param creditedTo the user credited for the resolution
     */
    public void setCreditedTo(final String creditedTo) { 
        this.creditedTo = creditedTo; 
    }
} 