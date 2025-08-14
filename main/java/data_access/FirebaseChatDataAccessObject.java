package data_access;

import com.google.firebase.database.*;
import use_case.dms.DMsUserDataAccessInterface;
import entity.User;
import entity.Chat;
import entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.time.LocalDateTime;

public class FirebaseChatDataAccessObject implements DMsUserDataAccessInterface {

    private final DatabaseReference chatsRef;
    private final DatabaseReference messagesRef;
    private final FirebaseUserDataAccessObject userDAO;

    public FirebaseChatDataAccessObject() {
        this.chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        this.userDAO = new FirebaseUserDataAccessObject();
    }

    @Override
    public List<Chat> getChatsForUser(String username) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.getChatsForUser() called ===");
        System.out.println("DEBUG: Getting chats for user: '" + username + "'");
        CompletableFuture<List<Chat>> future = new CompletableFuture<>();

        // Get all chats and filter by participants
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Chat> chats = new ArrayList<>();
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null && chat.getParticipants() != null) {
                        // Check if the user is a participant in this chat
                        boolean isParticipant = false;
                        for (String participant : chat.getParticipants()) {
                            if (participant.equals(username)) {
                                isParticipant = true;
                                break;
                            }
                        }
                        if (isParticipant) {
                            chats.add(chat);
                        }
                    }
                }
                System.out.println("DEBUG: Firebase chats retrieved: " + chats.size());
                future.complete(chats);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error getting chats: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to get chats: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error getting chats: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Chat createChat(List<String> participants) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.createChat() called ===");
        System.out.println("DEBUG: Creating chat with participants: " + participants.size());

        for (String participant : participants) {
            System.out.println("DEBUG: Participant: " + participant);
        }

        String chatId = generateChatId();
        LocalDateTime createdAt = LocalDateTime.now();
        Chat chat = new Chat(chatId, participants, createdAt, false);

        System.out.println("DEBUG: About to save chat to Firebase with ID: " + chatId);

        // Use a simpler approach - just save and return the chat
        chatsRef.child(chatId).setValue(chat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("DEBUG: Firebase error creating chat: " + databaseError.getMessage());
                } else {
                    System.out.println("DEBUG: Firebase chat created successfully: " + chatId);
                }
            }
        });

        // Return the chat immediately - Firebase operations are asynchronous
        System.out.println("DEBUG: Returning chat object with ID: " + chatId);
        return chat;
    }

    @Override
    public Message sendMessage(String chatId, String sender, String content) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.sendMessage() called ===");
        System.out.println("DEBUG: Sending message to chat: '" + chatId + "' from: '" + sender + "'");

        String messageId = generateMessageId();
        LocalDateTime sentAt = LocalDateTime.now();
        Message message = new Message(messageId, chatId, sender, content, sentAt, false);

        messagesRef.child(chatId).child(messageId).setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("DEBUG: Firebase error sending message: " + databaseError.getMessage());
                } else {
                    System.out.println("DEBUG: Firebase message sent successfully: " + messageId);
                }
            }
        });

        return message;
    }

    @Override
    public List<Message> getMessagesForChat(String chatId) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.getMessagesForChat() called ===");
        System.out.println("DEBUG: Getting messages for chat: '" + chatId + "'");

        CompletableFuture<List<Message>> future = new CompletableFuture<>();

        messagesRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                System.out.println("DEBUG: Firebase messages retrieved: " + messages.size());
                future.complete(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error getting messages: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to get messages: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error getting messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Chat getChatById(String chatId) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.getChatById() called ===");
        System.out.println("DEBUG: Getting chat by ID: '" + chatId + "'");

        CompletableFuture<Chat> future = new CompletableFuture<>();

        chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                System.out.println("DEBUG: Firebase chat retrieved: " + (chat != null ? chat.getChatId() : "null"));
                future.complete(chat);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error getting chat: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to get chat: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error getting chat: " + e.getMessage());
            return null;
        }
    }

    private String generateChatId() {
        return "chat_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    @Override
    public User getUserByUsername(String username) {
        return userDAO.get(username);
    }

    @Override
    public boolean chatExistsBetweenUsers(String user1, String user2) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.chatExistsBetweenUsers() called ===");
        System.out.println("DEBUG: Checking if chat exists between: '" + user1 + "' and '" + user2 + "'");

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Get all chats and check if any contain both users
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = false;
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null && chat.getParticipants() != null) {
                        List<String> participants = chat.getParticipants();
                        if (participants.size() == 2 &&
                                participants.contains(user1) &&
                                participants.contains(user2)) {
                            exists = true;
                            break;
                        }
                    }
                }
                System.out.println("DEBUG: Chat exists between users: " + exists);
                future.complete(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error checking chat existence: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to check chat existence: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error checking chat existence: " + e.getMessage());
            return false;
        }
    }

    public void updateChatIsBlocked(String chatId, boolean isBlocked) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.updateChatIsBlocked() called ===");
        System.out.println("DEBUG: Updating isBlocked for chat: '" + chatId + "' to: " + isBlocked);

        chatsRef.child(chatId).child("blocked").setValue(isBlocked, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("DEBUG: Firebase error updating isBlocked: " + databaseError.getMessage());
                } else {
                    System.out.println("DEBUG: Successfully updated isBlocked to " + isBlocked + " for chat " + chatId);
                }
            }
        });
    }

    public boolean isChatBlocked(String chatId) {
        System.out.println("\n=== DEBUG: FirebaseChatDataAccessObject.isChatBlocked() called ===");
        System.out.println("DEBUG: Checking if chat is blocked for ID: " + chatId);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        chatsRef.child(chatId).child("blocked").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isBlocked = dataSnapshot.getValue(Boolean.class);
                System.out.println("DEBUG: isBlocked value: " + isBlocked);
                future.complete(isBlocked != null && isBlocked);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("DEBUG: Firebase error checking isBlocked: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to check isBlocked: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error checking isBlocked: " + e.getMessage());
            return false; // or throw an exception, depending on your use case
        }
    }
}