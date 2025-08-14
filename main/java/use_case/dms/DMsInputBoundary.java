package use_case.dms;

import entity.User;

/**
 * Interface for chat use case input operations.
 */
public interface DMsInputBoundary {

    /**
     * Loads all chats for the current user.
     * @param inputData the input data containing user information
     */
    void loadChats(DMsInputData inputData);

    /**
     * Creates a new chat with specified participants.
     * @param inputData the input data containing participant information
     */
    void createChat(DMsInputData inputData);

    /**
     * Sends a message to a specific chat.
     * @param inputData the input data containing message information
     */
    void sendMessage(DMsInputData inputData);

    /**
     * Loads messages for a specific chat.
     * @param inputData the input data containing chat information
     */
    void loadMessages(DMsInputData inputData);

    /**
     * Gets a user by their username.
     * @param username the username to search for
     * @return the user, or null if not found
     */
    User getUserByUsername(String username);

    /**
     * Checks if a chat already exists between two users.
     * @param user1 the first username
     * @param user2 the second username
     * @return true if a chat exists between these users, false otherwise
     */
    boolean chatExistsBetweenUsers(String user1, String user2);

    /**
     * Changes the value of isBlocked for a certain chat.
     * @param chatId the ID of the chat
     * @param isBlocked the boolean for if it is blocked
     */
    void updateChatIsBlocked(String chatId, boolean isBlocked);

    /**
     * Checks if a chat is blocked.
     * @param chatId the ID of the chat
     * @return true if the chat is blocked
     */
    boolean isChatBlocked(String chatId);
}