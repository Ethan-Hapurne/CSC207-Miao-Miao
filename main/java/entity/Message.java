package entity;

import com.google.firebase.database.IgnoreExtraProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@IgnoreExtraProperties
public class Message {

    private String messageId;
    private String chatId;
    private String senderName;
    private String content;

    // store as ISO-8601 string for Firebase compatibility
    private String sentAt;
    private boolean systemMessage;

    // no-arg constructor for Firebase
    public Message() {}

    // Primary ctor: sentAt as String (preferred for Firebase)
    public Message(String chatId, String senderName, String content, String sentAt, boolean systemMessage) {
        this.chatId = chatId;
        this.senderName = senderName;
        this.content = content;
        this.sentAt = sentAt;
        this.systemMessage = systemMessage;
    }

    // Overload: accept LocalDateTime, convert to ISO-8601 string
    public Message(String chatId, String senderName, String content, LocalDateTime sentAt, boolean systemMessage) {
        this(chatId, senderName, content,
                sentAt != null ? sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                systemMessage);
    }

    // getters / setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // >>> add this so DMsView.java 的 message.getSentAt() 能编过
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public boolean isSystemMessage() { return systemMessage; }
    public void setSystemMessage(boolean systemMessage) { this.systemMessage = systemMessage; }
}
