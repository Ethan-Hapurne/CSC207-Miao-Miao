package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.dms.DMsController;
import interface_adapter.dms.DMsState;
import interface_adapter.dms.DMsViewModel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * UI-level tests for {@link DMsView}. These tests:
 * <ul>
 *   <li>Run in headless mode (no real dialogs).</li>
 *   <li>Enable Byte Buddy experimental flag so Mockito works on JDK&nbsp;24.</li>
 *   <li>Never mock entity classes in a risky way; only minimal stubbing via Mockito.</li>
 *   <li>Avoid any branch that would show a {@code JOptionPane}.</li>
 * </ul>
 */
public final class DMsViewTest {

    /** Fixed username used across tests. */
    private static final String USER_ALICE = "alice";
    /** Fixed other participant. */
    private static final String USER_BOB = "bob";
    /** Fixed chat id. */
    private static final String CHAT_ID = "c1";

    private DMsController controller;
    private DMsViewModel viewModel;
    private ViewManagerModel viewManagerModel;
    private DMsView view;

    /**
     * Enable headless mode and Byte Buddy experimental support
     * (required for Mockito on JDK 24) before any mocks are created.
     */
    @BeforeAll
    static void enableHeadlessAndByteBuddy() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @BeforeEach
    void setUp() {
        controller = mock(DMsController.class);
        viewModel = mock(DMsViewModel.class);
        viewManagerModel = mock(ViewManagerModel.class);

        view = new DMsView(viewManagerModel, viewModel);
        view.setDMsController(controller);
        view.setCurrentUsername(USER_ALICE);
    }

    @Test
    @DisplayName("getViewName returns 'dms'")
    void getViewName_returnsDms() {
        assertEquals("dms", view.getViewName());
    }

    @Test
    @DisplayName("Component shown triggers loadChats for current user")
    void componentShown_loadsChats() {
        view.getComponentListeners()[0]
                .componentShown(new ComponentEvent(view, ComponentEvent.COMPONENT_SHOWN));
        verify(controller, times(1)).loadChats(USER_ALICE);
    }

    @Test
    @DisplayName("Send button: unblocked chat and non-empty input sends message")
    void sendButton_unblocked_sendsMessage() throws Exception {
        setPrivate(view, "selectedChatId", CHAT_ID);
        when(controller.isChatBlocked(CHAT_ID)).thenReturn(false);

        final JTextField input = (JTextField) getPrivate(view, "chatInputField");
        input.setText("hello world");

        final JButton send = (JButton) getPrivate(view, "sendButton");
        send.doClick();

        verify(controller).sendMessage(CHAT_ID, USER_ALICE, "hello world");
    }

    @Test
    @DisplayName("Send button: empty input does nothing")
    void sendButton_emptyInput_noAction() throws Exception {
        setPrivate(view, "selectedChatId", CHAT_ID);
        when(controller.isChatBlocked(CHAT_ID)).thenReturn(false);

        final JTextField input = (JTextField) getPrivate(view, "chatInputField");
        input.setText("   ");

        final JButton send = (JButton) getPrivate(view, "sendButton");
        send.doClick();

        verify(controller, never()).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("New DM button: empty input does nothing")
    void newDmButton_emptyInput_noAction() throws Exception {
        final JTextField newDM = (JTextField) getPrivate(view, "newDMField");
        newDM.setText("   ");

        final JButton newDMButton = (JButton) getPrivate(view, "newDMButton");
        newDMButton.doClick();

        verify(controller, never()).createChat(anyList());
        verify(controller, never()).chatExistsBetweenUsers(anyString(), anyString());
        verify(controller, never()).getUserByUsername(anyString());
    }

    @Test
    @DisplayName("propertyChange: chats/messages/currentChat update UI; clicking list item loads messages")
    void propertyChange_updatesUi_and_clickListLoadsMessages() throws Exception {
        // --- Prepare Chat and Message stubs via Mockito (safe with Byte Buddy flag) ---
        final entity.Chat chat = mock(entity.Chat.class);
        final List<String> participants = new ArrayList<>();
        participants.add(USER_ALICE);
        participants.add(USER_BOB);
        when(chat.getChatId()).thenReturn(CHAT_ID);
        when(chat.getParticipants()).thenReturn(participants);

        final entity.Message m1 = mock(entity.Message.class);
        when(m1.getSentAt()).thenReturn("10:00");
        when(m1.getSender()).thenReturn(USER_ALICE);
        when(m1.getContent()).thenReturn("Hi Bob");

        final entity.Message m2 = mock(entity.Message.class);
        when(m2.getSentAt()).thenReturn("10:01");
        when(m2.getSender()).thenReturn(USER_BOB);
        when(m2.getContent()).thenReturn("Yo Alice");

        final List<entity.Chat> chats = List.of(chat);
        final List<entity.Message> messages = List.of(m1, m2);

        // --- State with full data, no error ---
        final DMsState state = mock(DMsState.class);
        when(state.getError()).thenReturn(null);
        when(state.getChats()).thenReturn(chats);
        when(state.getMessages()).thenReturn(messages);
        when(state.getCurrentChat()).thenReturn(chat);

        // Fire propertyChange -> should update chats list, chat area, header & selectedChatId
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, "dms", null, state);
        view.propertyChange(evt);

        // Assert header shows "bob" and selectedChatId is set.
        final JLabel chatWith = (JLabel) getPrivate(view, "chatWithLabel");
        assertEquals(USER_BOB, chatWith.getText());

        final String selected = (String) getPrivate(view, "selectedChatId");
        assertEquals(CHAT_ID, selected);

        // Assert chat text contains both lines:
        final JTextArea chatArea = (JTextArea) getPrivate(view, "chatArea");
        final String text = chatArea.getText();
        // Expected fragments (exact formatting is defined in DMsView#updateChatArea)
        // Format: "[time] sender: content\n"
        // We only check containment to stay robust.
        org.junit.jupiter.api.Assertions.assertTrue(text.contains("[10:00] alice: Hi Bob"));
        org.junit.jupiter.api.Assertions.assertTrue(text.contains("[10:01] bob: Yo Alice"));

        // --- Click the first chat list item -> should call controller.loadMessages(c1, alice) ---
        final JPanel dmsListPanel = (JPanel) getPrivate(view, "dmsListPanel");
        Component item = null;
        for (Component c : dmsListPanel.getComponents()) {
            if (c instanceof JPanel && c.getMouseListeners().length > 0) {
                item = c;
                break;
            }
        }
        // Simulate mouse click on the list item panel.
        final MouseEvent click = new MouseEvent(item, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 5, 5, 1, false);
        item.getMouseListeners()[0].mouseClicked(click);

        verify(controller, atLeastOnce()).loadMessages(CHAT_ID, USER_ALICE);
    }

    @Test
    @DisplayName("propertyChange: no error & no data runs without exception")
    void propertyChange_noError_noData_ok() {
        final DMsState state = mock(DMsState.class);
        when(state.getError()).thenReturn(null);
        when(state.getChats()).thenReturn(null);
        when(state.getMessages()).thenReturn(null);
        when(state.getCurrentChat()).thenReturn(null);

        final PropertyChangeEvent evt = new PropertyChangeEvent(this, "dms", null, state);
        view.propertyChange(evt);
        // No exception is success.
    }

    // ---------------- reflection helpers ----------------

    /**
     * Gets a private field via reflection.
     *
     * @param target    object that holds the field
     * @param fieldName field name
     * @return field value
     * @throws NoSuchFieldException   when field not found
     * @throws IllegalAccessException when access denied
     */
    private static Object getPrivate(final Object target, final String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        final Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    /**
     * Sets a private field via reflection.
     *
     * @param target    object that holds the field
     * @param fieldName field name
     * @param value     value to set
     * @throws NoSuchFieldException   when field not found
     * @throws IllegalAccessException when access denied
     */
    private static void setPrivate(final Object target, final String fieldName, final Object value)
            throws NoSuchFieldException, IllegalAccessException {
        final Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
