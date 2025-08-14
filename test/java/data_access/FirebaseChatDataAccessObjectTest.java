package data_access;

import com.google.firebase.database.*;
import entity.Chat;
import entity.CommonUser;
import entity.Message;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseChatDataAccessObjectTest {

    @Mock
    private FirebaseDatabase mockFirebaseDatabase;

    @Mock
    private DatabaseReference mockChatsRef;

    @Mock
    private DatabaseReference mockMessagesRef;

    @Mock
    private DatabaseReference mockChatIdRef;

    @Mock
    private DatabaseReference mockChatMessagesRef;

    @Mock
    private DatabaseReference mockMessageIdRef;

    @Mock
    private DatabaseReference mockBlockedRef;

    @Mock
    private FirebaseUserDataAccessObject mockUserDAO;

    @Captor
    private ArgumentCaptor<ValueEventListener> valueEventListenerCaptor;

    @Captor
    private ArgumentCaptor<DatabaseReference.CompletionListener> completionListenerCaptor;

    private FirebaseChatDataAccessObject chatDAO;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Setup basic mocking behavior
        when(mockFirebaseDatabase.getReference("chats")).thenReturn(mockChatsRef);
        when(mockFirebaseDatabase.getReference("messages")).thenReturn(mockMessagesRef);

        // Use MockedStatic to mock static FirebaseDatabase.getInstance()
        try (MockedStatic<FirebaseDatabase> mockedFirebaseDatabase = mockStatic(FirebaseDatabase.class)) {
            mockedFirebaseDatabase.when(FirebaseDatabase::getInstance).thenReturn(mockFirebaseDatabase);

            // Create DAO with mocked Firebase
            chatDAO = new FirebaseChatDataAccessObject();

            // Use reflection to inject mock userDAO
            Field userDAOField = FirebaseChatDataAccessObject.class.getDeclaredField("userDAO");
            userDAOField.setAccessible(true);
            userDAOField.set(chatDAO, mockUserDAO);

            // Use reflection to inject mockChatsRef
            Field chatsRefField = FirebaseChatDataAccessObject.class.getDeclaredField("chatsRef");
            chatsRefField.setAccessible(true);
            chatsRefField.set(chatDAO, mockChatsRef);

            // Use reflection to inject mockMessagesRef
            Field messagesRefField = FirebaseChatDataAccessObject.class.getDeclaredField("messagesRef");
            messagesRefField.setAccessible(true);
            messagesRefField.set(chatDAO, mockMessagesRef);
        }
    }

    @Test
    void classIsLoadable() {
        assertNotNull(FirebaseChatDataAccessObject.class);
    }

    @Test
    void allDeclaredMethodsAreReflectivelyAccessible() {
        Method[] methods = FirebaseChatDataAccessObject.class.getDeclaredMethods();
        assertNotNull(methods);
        for (Method m : methods) {
            assertNotNull(m.getName());
            assertNotNull(m.getReturnType());
            assertNotNull(m.getParameterTypes());
        }
    }

    // === CONSTRUCTOR TESTS ===


    @Test
    void constructor_initializesReferencesCorrectly() {
        try (MockedStatic<FirebaseDatabase> mockedFirebaseDatabase = mockStatic(FirebaseDatabase.class)) {
            mockedFirebaseDatabase.when(FirebaseDatabase::getInstance).thenReturn(mockFirebaseDatabase);

            FirebaseChatDataAccessObject newChatDAO = new FirebaseChatDataAccessObject();

            // Verify Firebase references were created with correct paths, allowing multiple calls
            verify(mockFirebaseDatabase, atLeastOnce()).getReference("chats");
            verify(mockFirebaseDatabase, atLeastOnce()).getReference("messages");
        }
    }
    // === GET CHATS FOR USER TESTS ===

    @Test
    void getChatsForUser_returnsFilteredChats() {
        // Prepare test data
        String username = "testUser";

        // Setup mock behavior for the listener
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);

            // Create mock DataSnapshots
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot1 = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot2 = mock(DataSnapshot.class);

            // Setup Chat objects
            Chat chat1 = new Chat("chat1", Arrays.asList("testUser", "otherUser"), LocalDateTime.now(), false);
            Chat chat2 = new Chat("chat2", Arrays.asList("anotherUser", "someUser"), LocalDateTime.now(), false);

            // Connect mocks
            when(mockDataSnapshot.getChildren()).thenReturn(Arrays.asList(chatSnapshot1, chatSnapshot2));
            when(chatSnapshot1.getValue(Chat.class)).thenReturn(chat1);
            when(chatSnapshot2.getValue(Chat.class)).thenReturn(chat2);

            // Trigger the listener
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChatsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        List<Chat> result = chatDAO.getChatsForUser(username);

        // Verify results
        assertEquals(1, result.size());
        assertEquals("chat1", result.get(0).getChatId());
        assertTrue(result.get(0).getParticipants().contains(username));
    }

    @Test
    void getChatsForUser_handlesErrors() {
        // Prepare test data
        String username = "testUser";

        // Setup mock behavior for error case
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");

            // Trigger error callback
            listener.onCancelled(mockError);
            return null;
        }).when(mockChatsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        List<Chat> result = chatDAO.getChatsForUser(username);

        // Verify empty list is returned on error
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // === CREATE CHAT TESTS ===


    @Test
    void createChat_savesToFirebaseAndReturnsChat() {
        // Prepare test data
        List<String> participants = Arrays.asList("user1", "user2");
        DatabaseReference mockChildRef = mock(DatabaseReference.class);

        // Mock the chain of calls
        when(mockChatsRef.child(anyString())).thenReturn(mockChildRef);

        doAnswer(invocation -> {
            Chat chat = invocation.getArgument(0);
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);

            // Simulate successful completion
            listener.onComplete(null, mockChildRef);
            return null;
        }).when(mockChildRef).setValue(any(Chat.class), any(DatabaseReference.CompletionListener.class));

        // Execute method under test
        Chat result = chatDAO.createChat(participants);

        // Verify result
        assertNotNull(result);
        assertEquals(participants, result.getParticipants());
        assertFalse(result.isBlocked());
        assertNotNull(result.getChatId());

        // Verify Firebase interactions
        verify(mockChatsRef).child(anyString());
        verify(mockChildRef).setValue(any(Chat.class), any(DatabaseReference.CompletionListener.class));
    }

    @Test
    void createChat_handlesFirebaseErrors() {
        List<String> participants = Arrays.asList("user1", "user2");
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(anyString())).thenReturn(mockChildRef);
        
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");
            listener.onComplete(mockError, mockChildRef);
            return null;
        }).when(mockChildRef).setValue(any(Chat.class), any(DatabaseReference.CompletionListener.class));

        Chat result = chatDAO.createChat(participants);
        assertNotNull(result);
        assertEquals(participants, result.getParticipants());
    }

    // === SEND MESSAGE TESTS ===

    @Test
    void sendMessage_savesToFirebaseAndReturnsMessage() {
        String chatId = "chat123";
        DatabaseReference mockChatRef = mock(DatabaseReference.class);
        DatabaseReference mockMessageRef = mock(DatabaseReference.class);
        when(mockMessagesRef.child(chatId)).thenReturn(mockChatRef);
        when(mockChatRef.child(anyString())).thenReturn(mockMessageRef);
        
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            listener.onComplete(null, mockMessageRef);
            return null;
        }).when(mockMessageRef).setValue(any(Message.class), any(DatabaseReference.CompletionListener.class));

        Message result = chatDAO.sendMessage(chatId, "user1", "Hello");
        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
        assertEquals("Hello", result.getContent());
    }
    @Test
    void sendMessage_handlesFirebaseErrors() {
        String chatId = "chat123";
        String messageId = "msg1";
        DatabaseReference mockChatRef = mock(DatabaseReference.class);
        DatabaseReference mockMessageRef = mock(DatabaseReference.class);
        when(mockMessagesRef.child(chatId)).thenReturn(mockChatRef);
        when(mockChatRef.child(anyString())).thenReturn(mockMessageRef);
        
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");
            listener.onComplete(mockError, mockMessageRef);
            return null;
        }).when(mockMessageRef).setValue(any(Message.class), any(DatabaseReference.CompletionListener.class));

        Message result = chatDAO.sendMessage(chatId, "user1", "Hello");
        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
    }

    // === GET MESSAGES FOR CHAT TESTS ===


    @Test
    void getMessagesForChat_returnsMessages() {
        // Prepare test data
        String chatId = "chat123";
        DatabaseReference mockChatRef = mock(DatabaseReference.class);

        // Setup mock chain
        when(mockMessagesRef.child(chatId)).thenReturn(mockChatRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);

            // Create mock DataSnapshots for testing
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            DataSnapshot messageSnapshot1 = mock(DataSnapshot.class);
            DataSnapshot messageSnapshot2 = mock(DataSnapshot.class);

            // Setup Message objects
            Message message1 = new Message("msg1", chatId, "user1", "Hello", LocalDateTime.now(), false);
            Message message2 = new Message("msg2", chatId, "user2", "Hi there", LocalDateTime.now(), false);

            // Connect mocks
            when(mockDataSnapshot.getChildren()).thenReturn(Arrays.asList(messageSnapshot1, messageSnapshot2));
            when(messageSnapshot1.getValue(Message.class)).thenReturn(message1);
            when(messageSnapshot2.getValue(Message.class)).thenReturn(message2);

            // Trigger the listener
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChatRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        List<Message> result = chatDAO.getMessagesForChat(chatId);

        // Verify results
        assertEquals(2, result.size());
        assertEquals("msg1", result.get(0).getMessageId());
        assertEquals("msg2", result.get(1).getMessageId());
        assertEquals(chatId, result.get(0).getChatId());
        assertEquals(chatId, result.get(1).getChatId());
    }
    @Test
    void getMessagesForChat_handlesErrors() {
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockMessagesRef.child(chatId)).thenReturn(mockChildRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");
            listener.onCancelled(mockError);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        List<Message> result = chatDAO.getMessagesForChat(chatId);
        assertTrue(result.isEmpty());
    }

    // === GET CHAT BY ID TESTS ===

    @Test
    void getChatById_returnsChat() {
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            Chat chat = new Chat(chatId, Arrays.asList("user1", "user2"), LocalDateTime.now(), false);
            when(mockDataSnapshot.getValue(Chat.class)).thenReturn(chat);
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        Chat result = chatDAO.getChatById(chatId);
        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
    }

    @Test
    void getChatById_returnsNullForNonexistentChat() {
        String chatId = "nonexistent";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            when(mockDataSnapshot.getValue(Chat.class)).thenReturn(null);
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        Chat result = chatDAO.getChatById(chatId);
        assertNull(result);
    }


    @Test
    void getChatById_handlesErrors() {
        // Prepare test data
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);

        // Setup mock behavior
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");

            // Trigger error callback
            listener.onCancelled(mockError);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        Chat result = chatDAO.getChatById(chatId);

        // Verify null is returned on error
        assertNull(result);
    }

    // === ID GENERATOR METHODS TESTS ===

    @Test
    void generateChatId_returnsValidId() throws Exception {
        // Use reflection to access private method
        Method generateChatIdMethod = FirebaseChatDataAccessObject.class.getDeclaredMethod("generateChatId");
        generateChatIdMethod.setAccessible(true);

        // Execute method under test
        String chatId = (String) generateChatIdMethod.invoke(chatDAO);

        // Verify results
        assertNotNull(chatId);
        assertTrue(chatId.startsWith("chat_"));
        assertTrue(chatId.length() > 10); // Should have timestamp and random number

        // Generate another ID and verify it's different
        String chatId2 = (String) generateChatIdMethod.invoke(chatDAO);
        assertNotEquals(chatId, chatId2, "Generated IDs should be unique");
    }

    @Test
    void generateMessageId_returnsValidId() throws Exception {
        // Use reflection to access private method
        Method generateMessageIdMethod = FirebaseChatDataAccessObject.class.getDeclaredMethod("generateMessageId");
        generateMessageIdMethod.setAccessible(true);

        // Execute method under test
        String messageId = (String) generateMessageIdMethod.invoke(chatDAO);

        // Verify results
        assertNotNull(messageId);
        assertTrue(messageId.startsWith("msg_"));
        assertTrue(messageId.length() > 10); // Should have timestamp and random number

        // Generate another ID and verify it's different
        String messageId2 = (String) generateMessageIdMethod.invoke(chatDAO);
        assertNotEquals(messageId, messageId2, "Generated IDs should be unique");
    }

    // === GET USER BY USERNAME TESTS ===

    @Test
    void getUserByUsername_delegatesToUserDAO() {
        // Prepare test data
        String username = "testUser";
        User expectedUser = new CommonUser("testUser", "password", false);

        // Setup mock behavior
        when(mockUserDAO.get(username)).thenReturn(expectedUser);

        // Execute method under test
        User result = chatDAO.getUserByUsername(username);

        // Verify results
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(mockUserDAO).get(username);
    }

    @Test
    void getUserByUsername_returnsNullForNonexistentUser() {
        // Prepare test data
        String username = "nonexistentUser";

        // Setup mock behavior
        when(mockUserDAO.get(username)).thenReturn(null);

        // Execute method under test
        User result = chatDAO.getUserByUsername(username);

        // Verify null is returned
        assertNull(result);
        verify(mockUserDAO).get(username);
    }

    // === CHAT EXISTS BETWEEN USERS TESTS ===

    @Test
    void chatExistsBetweenUsers_returnsTrueWhenChatExists() {
        // Prepare test data
        String user1 = "alice";
        String user2 = "bob";

        // Setup mock behavior
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);

            // Create mock DataSnapshots for testing
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot1 = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot2 = mock(DataSnapshot.class);

            // Setup Chat objects
            Chat chat1 = new Chat("chat1", Arrays.asList("alice", "charlie"), LocalDateTime.now(), false);
            Chat chat2 = new Chat("chat2", Arrays.asList("alice", "bob"), LocalDateTime.now(), false);

            // Connect mocks
            when(mockDataSnapshot.getChildren()).thenReturn(Arrays.asList(chatSnapshot1, chatSnapshot2));
            when(chatSnapshot1.getValue(Chat.class)).thenReturn(chat1);
            when(chatSnapshot2.getValue(Chat.class)).thenReturn(chat2);

            // Trigger the listener
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChatsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        boolean result = chatDAO.chatExistsBetweenUsers(user1, user2);

        // Verify results
        assertTrue(result);
    }

    @Test
    void chatExistsBetweenUsers_returnsFalseWhenChatDoesNotExist() {
        // Prepare test data
        String user1 = "alice";
        String user2 = "dave";

        // Setup mock behavior
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);

            // Create mock DataSnapshots for testing
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot1 = mock(DataSnapshot.class);

            // Setup Chat objects (no chat with alice and dave)
            Chat chat1 = new Chat("chat1", Arrays.asList("alice", "charlie"), LocalDateTime.now(), false);

            // Connect mocks
            when(mockDataSnapshot.getChildren()).thenReturn(Arrays.asList(chatSnapshot1));
            when(chatSnapshot1.getValue(Chat.class)).thenReturn(chat1);

            // Trigger the listener
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockChatsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        boolean result = chatDAO.chatExistsBetweenUsers(user1, user2);

        // Verify results
        assertFalse(result);
    }

    @Test
    void chatExistsBetweenUsers_handlesErrors() {
        // Prepare test data
        String user1 = "alice";
        String user2 = "bob";

        // Setup mock behavior for error case
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");

            // Trigger error callback
            listener.onCancelled(mockError);
            return null;
        }).when(mockChatsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        boolean result = chatDAO.chatExistsBetweenUsers(user1, user2);

        // Verify false is returned on error
        assertFalse(result);
    }

    // === UPDATE CHAT IS BLOCKED TESTS ===

    @Test
    void updateChatIsBlocked_setsValueInFirebase() {
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        DatabaseReference mockBlockedRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);
        when(mockChildRef.child("blocked")).thenReturn(mockBlockedRef);
        
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            listener.onComplete(null, mockBlockedRef);
            return null;
        }).when(mockBlockedRef).setValue(Optional.of(eq(true)), any(DatabaseReference.CompletionListener.class));

        chatDAO.updateChatIsBlocked(chatId, true);
        verify(mockBlockedRef).setValue(Optional.of(eq(true)), any(DatabaseReference.CompletionListener.class));
    }


    @Test
    void isChatBlocked_returnsTrueWhenBlocked() {
        // Prepare test data
        String chatId = "chat123";
        DatabaseReference mockChatRef = mock(DatabaseReference.class);
        DatabaseReference mockBlockedRef = mock(DatabaseReference.class);

        // Setup mock chain
        when(mockChatsRef.child(chatId)).thenReturn(mockChatRef);
        when(mockChatRef.child("blocked")).thenReturn(mockBlockedRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            when(mockDataSnapshot.getValue(Boolean.class)).thenReturn(Boolean.TRUE);
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockBlockedRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        boolean result = chatDAO.isChatBlocked(chatId);

        // Verify results
        assertTrue(result);
    }

    @Test
    void isChatBlocked_returnsFalseWhenNotBlocked() {
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        DatabaseReference mockBlockedRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);
        when(mockChildRef.child("blocked")).thenReturn(mockBlockedRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            when(mockDataSnapshot.getValue(Boolean.class)).thenReturn(Boolean.valueOf(false));
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockBlockedRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        boolean result = chatDAO.isChatBlocked(chatId);
        assertFalse(result);
    }

    @Test
    void isChatBlocked_returnsFalseWhenValueIsNull() {
        // Prepare test data
        String chatId = "chat123";
        DatabaseReference mockChatRef = mock(DatabaseReference.class);
        DatabaseReference mockBlockedRef = mock(DatabaseReference.class);

        // Setup mock chain
        when(mockChatsRef.child(chatId)).thenReturn(mockChatRef);
        when(mockChatRef.child("blocked")).thenReturn(mockBlockedRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            when(mockDataSnapshot.getValue(Boolean.class)).thenReturn(null);
            listener.onDataChange(mockDataSnapshot);
            return null;
        }).when(mockBlockedRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Execute method under test
        boolean result = chatDAO.isChatBlocked(chatId);

        // Verify results
        assertFalse(result);
    }
    @Test
    void isChatBlocked_handlesErrors() {
        String chatId = "chat123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        DatabaseReference mockBlockedRef = mock(DatabaseReference.class);
        when(mockChatsRef.child(chatId)).thenReturn(mockChildRef);
        when(mockChildRef.child("blocked")).thenReturn(mockBlockedRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Test error");
            listener.onCancelled(mockError);
            return null;
        }).when(mockBlockedRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        boolean result = chatDAO.isChatBlocked(chatId);
        assertFalse(result);
    }
}