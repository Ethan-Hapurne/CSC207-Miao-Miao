package entity;

import java.time.LocalDateTime;
import java.util.List;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Represents a chat between users.
 */
@IgnoreExtraProperties
public class Chat {

    private String chatId;
    private List<String> participants; // Now a list of usernames
    private String createdAt; // Store as string for Firebase compatibility
    private boolean isBlocked;

    // Firebase requires a no-arg constructor for deserialization
    private Chat() {}

    /**
     * Constructs a Chat entity.
     * @param chatId Unique identifier for the chat
     * @param participants List of usernames in the chat
     * @param createdAt Creation time of the chat
     */
    public Chat(String chatId, List<String> participants, LocalDateTime createdAt, boolean isBlocked) {
        this.chatId = chatId;
        this.participants = participants;
        this.createdAt = createdAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.isBlocked = isBlocked;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isBlocked() {return isBlocked;}

    public void setBlocked(boolean blocked) {this.isBlocked = blocked;}
}