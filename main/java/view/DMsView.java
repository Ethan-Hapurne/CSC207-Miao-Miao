package view;

import entity.User;
import entity.Chat;
import entity.Message;
import interface_adapter.ViewManagerModel;
import interface_adapter.dms.DMsController;
import interface_adapter.dms.DMsState;
import interface_adapter.dms.DMsViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The View for the DMs page.
 */
public class DMsView extends JPanel implements PropertyChangeListener {
    
    // Constants for magic numbers
    private static final int TEXT_FIELD_COLUMNS_20 = 20;
    private static final int TEXT_FIELD_COLUMNS_30 = 30;
    private static final int PADDING_5 = 5;
    private static final int PADDING_10 = 10;
    private static final int DMS_PANEL_WIDTH = 300;
    private static final int DMS_PANEL_HEIGHT = 600;
    private static final int CHAT_PANEL_WIDTH = 500;
    private static final int CHAT_PANEL_HEIGHT = 600;
    private static final int FONT_SIZE_12 = 12;
    private static final int FONT_SIZE_14 = 14;
    private static final int FONT_SIZE_16 = 16;
    
    // Color constants
    private static final Color LIGHT_GRAY_BACKGROUND = new Color(245, 245, 245);
    private static final Color WHITE_COLOR = Color.WHITE;
    private static final Color BLACK_COLOR = Color.BLACK;
    private static final Color GRAY_COLOR = Color.GRAY;
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color DARKER_GRAY = new Color(105, 105, 105);
    
    // String constants
    private static final String VIEW_NAME = "dms";
    private static final String NEW_DM_BUTTON_TEXT = "New DM";
    private static final String BACK_BUTTON_TEXT = "Back";
    private static final String SEND_BUTTON_TEXT = "Send";
    private static final String BLOCK_BUTTON_TEXT = "Block";
    private static final String USERNAME_LABEL = "Username:";
    private static final String DMS_BORDER_TITLE = "Direct Messages";
    private static final String CHAT_BORDER_TITLE = "DM Chat";
    private static final String FONT_NAME = "Arial";
    
    private final JTextField newDMField = new JTextField(TEXT_FIELD_COLUMNS_20);
    private final JButton newDMButton = new JButton(NEW_DM_BUTTON_TEXT);
    private final JButton backButton = new JButton(BACK_BUTTON_TEXT);
    private final JPanel dmsListPanel = new JPanel();
    private JScrollPane dmsScrollPane = new JScrollPane();
    private final JPanel chatPanel = new JPanel();
    private JScrollPane chatScrollPane = new JScrollPane();
    private final JTextArea chatArea = new JTextArea();
    private final JTextField chatInputField = new JTextField(TEXT_FIELD_COLUMNS_30);
    private final JButton sendButton = new JButton(SEND_BUTTON_TEXT);
    private JLabel chatWithLabel;
    private JButton blockButton;

    private DMsController dmsController;
    private DMsViewModel dmsViewModel;
    private String currentUsername;
    private String selectedChatId;

    /**
     * Creates a new DMsView.
     * @param viewManagerModel the view manager model
     * @param dMsViewModel the DMs view model
     */
    public DMsView(final ViewManagerModel viewManagerModel, 
                   final DMsViewModel dMsViewModel) {
        this.dmsViewModel = dMsViewModel;
        this.dmsViewModel.addPropertyChangeListener(this);

        // Set up the main layout
        this.setLayout(new BorderLayout());

        // Create top toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, 
            PADDING_10, PADDING_10));

        // Left side - dm
        JPanel dmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dmPanel.add(new JLabel(USERNAME_LABEL));
        dmPanel.add(newDMField);
        dmPanel.add(newDMButton);

        // Right side - back button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);

        toolbarPanel.add(dmPanel, BorderLayout.WEST);
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);

        // DMs list on the left
        dmsListPanel.setLayout(new BoxLayout(dmsListPanel, BoxLayout.Y_AXIS));
        dmsListPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_5, PADDING_5, 
            PADDING_5, PADDING_5));
        dmsListPanel.setBackground(LIGHT_GRAY_BACKGROUND);

        dmsScrollPane = new JScrollPane(dmsListPanel);
        dmsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dmsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dmsScrollPane.setPreferredSize(new Dimension(DMS_PANEL_WIDTH, DMS_PANEL_HEIGHT));
        dmsScrollPane.setBorder(BorderFactory.createTitledBorder(DMS_BORDER_TITLE));
        dmsScrollPane.getViewport().setBackground(LIGHT_GRAY_BACKGROUND);

        // Add component listener to detect when view becomes visible
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                // Load chats when the view becomes visible
                if (dmsController != null && currentUsername != null) {
                    dmsController.loadChats(currentUsername);
                }
            }
        });

        // Chat panel on the right
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder(CHAT_BORDER_TITLE));
        chatPanel.setPreferredSize(new Dimension(CHAT_PANEL_WIDTH, CHAT_PANEL_HEIGHT));

        // Header bar for DM chat
        JPanel headerPanel = new JPanel(new BorderLayout());
        chatWithLabel = new JLabel("");
        blockButton = new JButton(BLOCK_BUTTON_TEXT);
        headerPanel.add(chatWithLabel, BorderLayout.WEST);
        headerPanel.add(blockButton, BorderLayout.EAST);
        chatPanel.add(headerPanel, BorderLayout.NORTH);

        // Chat area (scrollable, non-editable)
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setPreferredSize(new Dimension(CHAT_PANEL_WIDTH, 500));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Chat input area (bottom of chat panel)
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, 
            PADDING_10, PADDING_10));
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST); // Non-functional
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        // Main split panel (DMs list left, chat right)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dmsScrollPane, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        // Set minimum sizes to ensure proper layout
        dmsScrollPane.setMinimumSize(new Dimension(250, 400));
        chatPanel.setMinimumSize(new Dimension(400, 400));

        // Add components to main panel
        this.add(toolbarPanel, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);

        backButton.addActionListener(e -> {viewManagerModel.popViewOrClose();});

        blockButton.addActionListener(e -> {
            String userToBlock = chatWithLabel.getText();
            if (userToBlock != null && !userToBlock.isEmpty()) {
                if (selectedChatId != null && dmsController != null) {

                    if (!dmsController.isChatBlocked(selectedChatId)) {
                        dmsController.sendMessage(selectedChatId, currentUsername, "User: " + currentUsername + " has blocked this chat.");
                    }

                    dmsController.updateChatIsBlocked(selectedChatId, true);
                    JOptionPane.showMessageDialog(this, "Blocked this user in chat: " + selectedChatId);
                } else {
                    JOptionPane.showMessageDialog(this, "No chat selected or controller missing.", "Error", JOptionPane.ERROR_MESSAGE);
                }
        }});

        newDMButton.addActionListener(e -> {
            if (!newDMField.getText().trim().isEmpty() && dmsController != null && currentUsername != null) {
                String targetUsername = newDMField.getText().trim();

                // Check if target username is the same as current user
                if (targetUsername.equals(currentUsername)) {
                    JOptionPane.showMessageDialog(this, "You cannot create a DM with yourself!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if a chat already exists with this user (server-side check)
                if (dmsController.chatExistsBetweenUsers(currentUsername, targetUsername)) {
                    JOptionPane.showMessageDialog(this, "A chat with '" + targetUsername + "' already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    newDMField.setText(""); // Clear the field
                    return;
                }

                // Create a new chat with the current user and target user
                List<User> participants = new ArrayList<>();

                // Get current user from data access layer
                User currentUser = dmsController.getUserByUsername(currentUsername);
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(this, "Current user not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                participants.add(currentUser);

                // Get target user from data access layer
                User targetUser = dmsController.getUserByUsername(targetUsername);
                if (targetUser == null) {
                    JOptionPane.showMessageDialog(this, "User '" + targetUsername + "' not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                participants.add(targetUser);

                dmsController.createChat(participants);
                newDMField.setText(""); // Clear the field
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedChatId != null && dmsController != null) {
                if (dmsController.isChatBlocked(selectedChatId)) {
                    JOptionPane.showMessageDialog(this, "This chat is blocked. You cannot send messages.", "Blocked", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!chatInputField.getText().trim().isEmpty() && currentUsername != null) {
                    String messageContent = chatInputField.getText().trim();
                    dmsController.sendMessage(selectedChatId, currentUsername, messageContent);
                    chatInputField.setText(""); // Clear the field
                }
        }});
    }

    public String getViewName() {
        return VIEW_NAME;
    }

    public void setDMsController(DMsController dmsController) {
        this.dmsController = dmsController;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("DEBUG: DMsView propertyChange called with property: " + evt.getPropertyName());
        DMsState state = (DMsState) evt.getNewValue();
        System.out.println("DEBUG: State received - chats: " + (state.getChats() != null ? state.getChats().size() : "null") +
                ", messages: " + (state.getMessages() != null ? state.getMessages().size() : "null") +
                ", currentChat: " + (state.getCurrentChat() != null ? state.getCurrentChat().getChatId() : "null"));

        if (state.getError() != null) {
            JOptionPane.showMessageDialog(this, state.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (state.getChats() != null) {
            updateChatsList(state.getChats());
        }

        if (state.getMessages() != null) {
            updateChatArea(state.getMessages());
        }

        if (state.getCurrentChat() != null) {
            System.out.println("DEBUG: Setting current chat: " + state.getCurrentChat().getChatId());
            selectedChatId = state.getCurrentChat().getChatId();
            updateChatHeader(state.getCurrentChat());
        }
    }

    private void updateChatsList(List<Chat> chats) {
        System.out.println("DEBUG: updateChatsList called with " + (chats != null ? chats.size() : "null") + " chats");
        dmsListPanel.removeAll();

        if (chats == null || chats.isEmpty()) {
            System.out.println("DEBUG: No chats to display, showing placeholder");
            JLabel placeholderLabel = new JLabel("No DMs available.", SwingConstants.CENTER);
            placeholderLabel.setFont(new Font(FONT_NAME, Font.ITALIC, FONT_SIZE_14));
            placeholderLabel.setForeground(GRAY_COLOR);
            placeholderLabel.setBorder(BorderFactory.createEmptyBorder(PADDING_10, PADDING_10, PADDING_10, PADDING_10));
            dmsListPanel.add(placeholderLabel);
        } else {
            System.out.println("DEBUG: Adding " + chats.size() + " chat items to the list");
            for (int i = 0; i < chats.size(); i++) {
                Chat chat = chats.get(i);
                System.out.println("DEBUG: Adding chat with ID: " + chat.getChatId() + ", participants: " + chat.getParticipants());
                JPanel chatItem = createChatListItem(chat);
                dmsListPanel.add(chatItem);

                // Add spacing between items (except after the last one)
                if (i < chats.size() - 1) {
                    dmsListPanel.add(Box.createVerticalStrut(2));
                }
            }
        }

        // Add some padding at the bottom
        dmsListPanel.add(Box.createVerticalStrut(PADDING_10));

        dmsListPanel.revalidate();
        dmsListPanel.repaint();
        dmsScrollPane.revalidate();
        dmsScrollPane.repaint();
        System.out.println("DEBUG: updateChatsList completed");
    }

    private JPanel createChatListItem(Chat chat) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(WHITE_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Fixed height for consistency
        panel.setPreferredSize(new Dimension(280, 80));

        // Get the other participant's name (not the current user)
        String otherParticipantName = "";
        for (String participant : chat.getParticipants()) {
            if (!participant.equals(currentUsername)) {
                otherParticipantName = participant;
                break;
            }
        }

        JLabel nameLabel = new JLabel(otherParticipantName);
        nameLabel.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_14));
        nameLabel.setForeground(new Color(50, 50, 50));

        // Since messages are now stored separately, show a placeholder
        String lastMessage = "Click to view messages";

        JLabel messageLabel = new JLabel(lastMessage);
        messageLabel.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE_12));
        messageLabel.setForeground(GRAY_COLOR);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(messageLabel, BorderLayout.SOUTH);

        panel.add(textPanel, BorderLayout.CENTER);

        // Add hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(LIGHT_BLUE); // Light blue on hover
                panel.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(WHITE_COLOR);
                panel.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedChatId = chat.getChatId();
                if (dmsController != null && currentUsername != null) {
                    dmsController.loadMessages(selectedChatId, currentUsername);
                }
            }
        });

        return panel;
    }

    private void updateChatArea(List<Message> messages) {
        chatArea.setText("");
        if (messages != null && !messages.isEmpty()) {
            StringBuilder chatText = new StringBuilder();

            for (Message message : messages) {
                String time = message.getSentAt();
                String senderName = message.getSender(); // Now directly a String username
                String content = message.getContent();

                chatText.append(String.format("[%s] %s: %s\n", time, senderName, content));
            }

            chatArea.setText(chatText.toString());
            // Scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    /** Updates the header label to show the other participant’s username. */
    private void updateChatHeader(Chat chat) {
        String other = "";
        for (String p : chat.getParticipants()) {
            if (!p.equals(currentUsername)) {
                other = p;
                break;
            }
        }
        chatWithLabel.setText(other);
    }
}