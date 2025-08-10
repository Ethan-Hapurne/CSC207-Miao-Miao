package entity;

import com.google.firebase.database.IgnoreExtraProperties; // <— add this
import java.time.LocalDateTime;
import java.util.List;


/** Represents a message in a chat. */
@IgnoreExtraProperties // <— add this@
public class Chat {

    private String chatId;
    private List<String> participants;   // List of usernames
    private String createdAt;            // Stored as ISO-8601 string
    private boolean isBlocked;

    /** No-arg constructor for serializers/deserializers. */
    public Chat() {}

    /**
     * Constructs a Chat entity.
     * @param chatId unique identifier for the chat
     * @param participants list of usernames in the chat
     * @param createdAt creation time of the chat
     * @param isBlocked whether the chat is blocked
     */
    public Chat(String chatId, List<String> participants, LocalDateTime createdAt, boolean isBlocked) {
        this.chatId = chatId;
        this.participants = participants;
        this.createdAt = createdAt.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.isBlocked = isBlocked;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }
}
