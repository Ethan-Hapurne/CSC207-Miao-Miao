package data_access.chat;

import com.google.firebase.database.*;
import data_access.FirebaseChatDataAccessObject;
import entity.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetChatsForUserTest {

    @Mock
    private FirebaseDatabase mockFirebaseDatabase;

    @Mock
    private DatabaseReference mockChatsRef;

    @Mock
    private DatabaseReference mockMessagesRef;

    @Captor
    private ArgumentCaptor<ValueEventListener> listenerCaptor;

    private FirebaseChatDataAccessObject chatDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup Firebase mocks
        try (MockedStatic<FirebaseDatabase> mockedFirebaseDatabase = mockStatic(FirebaseDatabase.class)) {
            mockedFirebaseDatabase.when(FirebaseDatabase::getInstance).thenReturn(mockFirebaseDatabase);
            when(mockFirebaseDatabase.getReference("chats")).thenReturn(mockChatsRef);
            when(mockFirebaseDatabase.getReference("messages")).thenReturn(mockMessagesRef);

            chatDAO = new FirebaseChatDataAccessObject();
        }
    }

    @Test
    void getChatsForUser_returnsFilteredChats() {
        // Prepare test data
        String username = "testUser";

        // Setup mock behavior using doAnswer
        doAnswer(invocation -> {
            // Capture the listener to trigger it manually
            ValueEventListener listener = invocation.getArgument(0);

            // Create mock DataSnapshots for testing
            DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot1 = mock(DataSnapshot.class);
            DataSnapshot chatSnapshot2 = mock(DataSnapshot.class);

            // Setup Chat objects with proper LocalDateTime
            LocalDateTime now = LocalDateTime.now();
            Chat chat1 = new Chat("chat1", Arrays.asList("testUser", "otherUser"), now, false);
            Chat chat2 = new Chat("chat2", Arrays.asList("anotherUser", "someUser"), now, false);

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

        // Setup mock behavior for error case using doAnswer
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
}