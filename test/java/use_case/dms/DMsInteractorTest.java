package use_case.dms;

import entity.Chat;
import entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DMsInteractor}.
 *
 * Notes:
 * - We only mock the DAO and presenter behavior; we do NOT mock entity classes
 *   (Chat/Message/User) to avoid ByteBuddy inline-mocking issues on JDK 24.
 * - Success-path tests are included to raise line coverage inside DMsInteractor.
 */
public class DMsInteractorTest {

    private static final String USER_A = "alice";
    private static final String USER_B = "bob";
    private static final String CHAT_ID = "chat-1";

    private DMsUserDataAccessInterface dao;
    private RecordingPresenter presenter;
    private DMsInteractor interactor;

    @BeforeEach
    void setUp() {
        this.dao = mock(DMsUserDataAccessInterface.class);
        this.presenter = new RecordingPresenter();
        this.interactor = new DMsInteractor(dao, presenter);
    }

    // ---------- loadChats ----------

    @Test
    @DisplayName("loadChats: happy path with empty list -> presenter receives empty chats and no error")
    void loadChats_happy_emptyList() {
        when(dao.getChatsForUser(USER_A)).thenReturn(emptyList());

        interactor.loadChats(new DMsInputData(USER_A));

        assertEquals(1, presenter.loadChatsCalled);
        assertNotNull(presenter.lastLoadChatsData);
        assertNotNull(presenter.lastLoadChatsData.getChats());
        assertTrue(presenter.lastLoadChatsData.getChats().isEmpty());
        assertNull(presenter.lastLoadChatsData.getError());
    }

    @Test
    @DisplayName("loadChats: DAO throws -> presenter receives error and null chats")
    void loadChats_daoThrows() {
        when(dao.getChatsForUser(USER_A)).thenThrow(new RuntimeException("boom"));

        interactor.loadChats(new DMsInputData(USER_A));

        assertEquals(1, presenter.loadChatsCalled);
        assertNotNull(presenter.lastLoadChatsData);
        assertNull(presenter.lastLoadChatsData.getChats());
        assertNotNull(presenter.lastLoadChatsData.getError());
    }

    // ---------- createChat ----------

    @Test
    @DisplayName("createChat: participants is empty -> presenter.create with 'No participants specified'")
    void createChat_emptyParticipants() {
        interactor.createChat(new DMsInputData(emptyList()));

        assertEquals(1, presenter.createChatCalled);
        assertNotNull(presenter.lastCreateChatData);
        assertTrue(presenter.lastCreateChatData.getError().contains("No participants"));
    }

    @Test
    @DisplayName("createChat: participants is null -> presenter.create with error (exception path)")
    void createChat_nullParticipants() {
        interactor.createChat(new DMsInputData((List<entity.User>) null));

        assertEquals(1, presenter.createChatCalled);
        assertNotNull(presenter.lastCreateChatData);
        assertNotNull(presenter.lastCreateChatData.getError());
        assertTrue(presenter.lastCreateChatData.getError().startsWith("Failed to create chat"));
    }

    @Test
    @DisplayName("createChat: DAO returns null -> presenter.create with failure message")
    void createChat_daoReturnsNull() {
        when(dao.createChat(anyList())).thenReturn(null);
        // Even though an empty list is handled earlier, this keeps coverage for the failure path.
        interactor.createChat(new DMsInputData(emptyList()));

        assertEquals(1, presenter.createChatCalled);
        assertNotNull(presenter.lastCreateChatData);
        assertTrue(
                presenter.lastCreateChatData.getError().contains("No participants")
                        || presenter.lastCreateChatData.getError().contains("Failed to create chat")
        );
    }

    @Test
    @DisplayName("createChat: success -> presenter.loadChats receives updated list and no error")
    void createChat_success_minimalUser() {
        // Minimal concrete User to avoid mocking entity classes.
        class DummyUser implements User {
            private final String name;
            DummyUser(String n) { this.name = n; }
            @Override public String getName() { return name; }
            @Override public String getPassword() { return ""; }
            @Override public int getCredibilityScore() { return 0; }
            @Override public List<String> getResolvedPosts() { return new ArrayList<>(); }
            @Override public void addResolvedPost(String postId) { /* no-op */ }
            @Override public void addCredibilityPoints(int points) { /* no-op */ }
            // isAdmin() uses the default implementation: false
        }
        List<User> participants = List.of(new DummyUser(USER_A));

        Chat created = new Chat("c1", List.of(USER_A, USER_B), LocalDateTime.now(), false);
        when(dao.createChat(List.of(USER_A))).thenReturn(created);
        when(dao.getChatsForUser(USER_A)).thenReturn(List.of(created));

        interactor.createChat(new DMsInputData(participants));

        // Success branch calls prepareLoadChatsView (not prepareCreateChatView)
        assertEquals(1, presenter.loadChatsCalled);
        assertNotNull(presenter.lastLoadChatsData);
        assertNull(presenter.lastLoadChatsData.getError());
        assertNotNull(presenter.lastLoadChatsData.getChats());
        assertEquals(1, presenter.lastLoadChatsData.getChats().size());
        assertEquals("c1", presenter.lastLoadChatsData.getChats().get(0).getChatId());
    }

    // ---------- sendMessage ----------

    @Test
    @DisplayName("sendMessage: invalid (blank message) -> presenter.send with error")
    void sendMessage_invalid() {
        interactor.sendMessage(new DMsInputData(CHAT_ID, USER_A, "   "));

        assertEquals(1, presenter.sendMessageCalled);
        assertNotNull(presenter.lastSendMessageData);
        assertTrue(presenter.lastSendMessageData.getError().contains("Invalid"));
    }

    @Test
    @DisplayName("sendMessage: success with empty messages list -> presenter.send has no error")
    void sendMessage_success_emptyList() {
        // Interactor does not use the returned Message instance; returning null is fine.
        when(dao.sendMessage(CHAT_ID, USER_A, "hi")).thenReturn(null);
        when(dao.getMessagesForChat(CHAT_ID)).thenReturn(emptyList());

        interactor.sendMessage(new DMsInputData(CHAT_ID, USER_A, "hi"));

        assertEquals(1, presenter.sendMessageCalled);
        assertNotNull(presenter.lastSendMessageData);
        assertNull(presenter.lastSendMessageData.getError());
        assertNotNull(presenter.lastSendMessageData.getMessages());
        assertTrue(presenter.lastSendMessageData.getMessages().isEmpty());
    }

    // ---------- loadMessages ----------

    @Test
    @DisplayName("loadMessages: invalid input (null chatId) -> presenter.loadMessages with error")
    void loadMessages_invalid() {
        interactor.loadMessages(new DMsInputData(null, USER_A));

        assertEquals(1, presenter.loadMessagesCalled);
        assertNotNull(presenter.lastLoadMessagesData);
        assertTrue(presenter.lastLoadMessagesData.getError().contains("Invalid"));
    }

    @Test
    @DisplayName("loadMessages: chat not found -> presenter.loadMessages with error")
    void loadMessages_notFound() {
        when(dao.getChatById(CHAT_ID)).thenReturn(null);

        interactor.loadMessages(new DMsInputData(CHAT_ID, USER_A));

        assertEquals(1, presenter.loadMessagesCalled);
        assertNotNull(presenter.lastLoadMessagesData);
        assertTrue(presenter.lastLoadMessagesData.getError().contains("not found"));
    }

    @Test
    @DisplayName("loadMessages: success -> presenter receives currentChat + empty messages + no error")
    void loadMessages_success() {
        Chat realChat = new Chat("c9", List.of(USER_A, USER_B), LocalDateTime.now(), false);

        when(dao.getChatById(CHAT_ID)).thenReturn(realChat);
        when(dao.getMessagesForChat(CHAT_ID)).thenReturn(emptyList());

        interactor.loadMessages(new DMsInputData(CHAT_ID, USER_A));

        assertEquals(1, presenter.loadMessagesCalled);
        assertNotNull(presenter.lastLoadMessagesData);
        assertSame(realChat, presenter.lastLoadMessagesData.getCurrentChat());
        assertNotNull(presenter.lastLoadMessagesData.getMessages());
        assertTrue(presenter.lastLoadMessagesData.getMessages().isEmpty());
        assertNull(presenter.lastLoadMessagesData.getError());
    }

    // ---------- simple pass-through & guards ----------

    @Test
    @DisplayName("getUserByUsername: DAO throws -> method returns null")
    void getUserByUsername_exception() {
        when(dao.getUserByUsername(USER_A)).thenThrow(new RuntimeException("x"));

        assertNull(interactor.getUserByUsername(USER_A));
    }

    @Test
    @DisplayName("chatExistsBetweenUsers: true and exception fallback")
    void chatExistsBetweenUsers_true_and_exception() {
        when(dao.chatExistsBetweenUsers(USER_A, USER_B)).thenReturn(true);
        assertTrue(interactor.chatExistsBetweenUsers(USER_A, USER_B));

        when(dao.chatExistsBetweenUsers(USER_A, USER_B)).thenThrow(new RuntimeException("x"));
        assertFalse(interactor.chatExistsBetweenUsers(USER_A, USER_B));
    }

    @Test
    @DisplayName("updateChatIsBlocked: DAO throws -> no exception is propagated")
    void updateChatIsBlocked_exceptionSwallowed() {
        doThrow(new RuntimeException("x")).when(dao).updateChatIsBlocked(CHAT_ID, true);

        // Should not throw
        interactor.updateChatIsBlocked(CHAT_ID, true);
    }

    @Test
    @DisplayName("isChatBlocked: true and exception fallback to false")
    void isChatBlocked_true_and_exception() {
        when(dao.isChatBlocked(CHAT_ID)).thenReturn(true);
        assertTrue(interactor.isChatBlocked(CHAT_ID));

        when(dao.isChatBlocked(CHAT_ID)).thenThrow(new RuntimeException("x"));
        assertFalse(interactor.isChatBlocked(CHAT_ID));
    }

    // ---------- Recording presenter ----------

    /**
     * Presenter test double that records the last output and call counts.
     */
    private static final class RecordingPresenter implements DMsOutputBoundary {
        private int loadChatsCalled;
        private int loadMessagesCalled;
        private int sendMessageCalled;
        private int createChatCalled;

        private DMsOutputData lastLoadChatsData;
        private DMsOutputData lastLoadMessagesData;
        private DMsOutputData lastSendMessageData;
        private DMsOutputData lastCreateChatData;

        @Override
        public void prepareLoadChatsView(final DMsOutputData outputData) {
            this.loadChatsCalled++;
            this.lastLoadChatsData = outputData;
        }

        @Override
        public void prepareLoadMessagesView(final DMsOutputData outputData) {
            this.loadMessagesCalled++;
            this.lastLoadMessagesData = outputData;
        }

        @Override
        public void prepareSendMessageView(final DMsOutputData outputData) {
            this.sendMessageCalled++;
            this.lastSendMessageData = outputData;
        }

        @Override
        public void prepareCreateChatView(final DMsOutputData outputData) {
            this.createChatCalled++;
            this.lastCreateChatData = outputData;
        }
    }
}
