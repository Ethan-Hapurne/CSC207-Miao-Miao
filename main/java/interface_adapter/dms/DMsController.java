package interface_adapter.dms;

import use_case.dms.DMsInputBoundary;
import use_case.dms.DMsInputData;
import entity.User;
import java.util.List;

/**
 * Controller for chat functionality.
 */
public class DMsController {
    private final DMsInputBoundary dMsInputBoundary;

    public DMsController(DMsInputBoundary dMsInputBoundary) {
        this.dMsInputBoundary = dMsInputBoundary;
    }

    /**
     * Loads all chats for the current user.
     * @param username the username to load chats for
     */
    public void loadChats(String username) {
        DMsInputData inputData = new DMsInputData(username);
        dMsInputBoundary.loadChats(inputData);
    }

    /**
     * Creates a new chat with specified participants.
     * @param participants the list of participants for the new chat
     */
    public void createChat(List<User> participants) {
        DMsInputData inputData = new DMsInputData(participants);
        dMsInputBoundary.createChat(inputData);
    }

    /**
     * Sends a message to a specific chat.
     * @param chatId the ID of the chat to send the message to
     * @param username the username of the sender
     * @param messageContent the content of the message
     */
    public void sendMessage(String chatId, String username, String messageContent) {
        DMsInputData inputData = new DMsInputData(chatId, username, messageContent);
        dMsInputBoundary.sendMessage(inputData);
    }

    /**
     * Loads messages for a specific chat.
     * @param chatId the ID of the chat to load messages for
     * @param username the current user's username
     */
    public void loadMessages(String chatId, String username) {
        DMsInputData inputData = new DMsInputData(chatId, username);
        dMsInputBoundary.loadMessages(inputData);
    }

    /**
     * Gets a user by their username.
     * @param username the username to search for
     * @return the user, or null if not found
     */
    public User getUserByUsername(String username) {
        return dMsInputBoundary.getUserByUsername(username);
    }

    /**
     * Checks if a chat already exists between two users.
     * @param user1 the first username
     * @param user2 the second username
     * @return true if a chat exists between these users, false otherwise
     */
    public boolean chatExistsBetweenUsers(String user1, String user2) {
        return dMsInputBoundary.chatExistsBetweenUsers(user1, user2);
    }

    /**
     * Changes the value of isBlocked for a certain chat
     * @param chatId the ID of the chat
     * @param isBlocked the boolean for if it is blocked
     */
    public void updateChatIsBlocked(String chatId, boolean isBlocked) {
        dMsInputBoundary.updateChatIsBlocked(chatId, isBlocked);
    }

    /**
     * Checks if a chat is blocked.
     * @param chatId the ID of the chat
     * @return true if the chat is blocked
     */
    public boolean isChatBlocked(String chatId) {
        return dMsInputBoundary.isChatBlocked(chatId);
    }
}