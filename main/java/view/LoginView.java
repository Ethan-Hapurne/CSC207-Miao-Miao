package view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import interface_adapter.ViewManagerModel;

/**
 * The View for when the user is logging into the program.
 */
public class LoginView extends JPanel implements ActionListener, PropertyChangeListener {

    // Constants for magic numbers
    private static final int TEXT_FIELD_COLUMNS = 20;
    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEIGHT = 500;
    private static final int BORDER_SIZE = 1;
    private static final int PADDING_30 = 30;
    private static final int PADDING_40 = 40;
    private static final int PADDING_50 = 50;
    private static final int FONT_SIZE_14 = 14;
    private static final int FONT_SIZE_28 = 28;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 120;
    
    // Color constants
    private static final Color LIGHT_GRAY_BACKGROUND = new Color(240, 242, 245);
    private static final Color WHITE_COLOR = Color.WHITE;
    private static final Color BLACK_COLOR = Color.BLACK;
    private static final Color GRAY_BORDER = new Color(200, 200, 200);
    private static final Color PRIMARY_BLUE = new Color(0, 123, 255);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color SECONDARY_GRAY = new Color(108, 117, 125);
    
    // String constants
    private static final String VIEW_NAME = "log in";
    private static final String TITLE_TEXT = "Welcome Back";
    private static final String SUBTITLE_TEXT = "Sign in to your account";
    private static final String USERNAME_LABEL = "Username";
    private static final String PASSWORD_LABEL = "Password";
    private static final String LOGIN_BUTTON_TEXT = "Log In";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static final String SIGNUP_BUTTON_TEXT = "Don't have an account? Sign up";
    private static final String FONT_NAME = "Segoe UI";
    
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManagerModel;

    private final JTextField usernameInputField = new JTextField(TEXT_FIELD_COLUMNS);
    private final JLabel usernameErrorField = new JLabel();

    private final JPasswordField passwordInputField = new JPasswordField(TEXT_FIELD_COLUMNS);
    private final JLabel passwordErrorField = new JLabel();

    private JButton logIn;
    private JButton cancel;
    private LoginController loginController;

    /**
     * Creates a new LoginView.
     * @param loginViewModel the login view model
     * @param viewManagerModel the view manager model
     */
    public LoginView(final LoginViewModel loginViewModel, 
                     final ViewManagerModel viewManagerModel) {

        this.loginViewModel = loginViewModel;
        this.viewManagerModel = viewManagerModel;
        this.loginViewModel.addPropertyChangeListener(this);

        // Set up the main panel with gradient background
        this.setLayout(new BorderLayout());
        this.setBackground(LIGHT_GRAY_BACKGROUND);
        
        // Create the main content panel
        JPanel mainContentPanel = createMainContentPanel();
        this.add(mainContentPanel, BorderLayout.CENTER);

        // Set up action listeners
        setupActionListeners();
    }

    /**
     * Creates the main content panel.
     * @return the main content panel
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(PADDING_50, PADDING_50, PADDING_50, PADDING_50));

        // Create the login card
        JPanel loginCard = createLoginCard();
        loginCard.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the card
        mainPanel.add(loginCard);

        return mainPanel;
    }

    /**
     * Creates the login card panel.
     * @return the login card panel
     */
    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRAY_BORDER, BORDER_SIZE),
            new EmptyBorder(PADDING_40, PADDING_40, PADDING_40, PADDING_40)
        ));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        // Title with modern styling
        JLabel title = new JLabel(TITLE_TEXT);
        title.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_28));
        title.setForeground(BLACK_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, PADDING_30, 0));

        // Subtitle
        JLabel subtitle = new JLabel(SUBTITLE_TEXT);
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE_14));
        subtitle.setForeground(BLACK_COLOR);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, PADDING_30, 0));

        // Username field
        JPanel usernamePanel = createInputPanel(USERNAME_LABEL, usernameInputField, usernameErrorField);
        
        // Password field
        JPanel passwordPanel = createInputPanel(PASSWORD_LABEL, passwordInputField, passwordErrorField);

        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();

        // Add components to card
        card.add(title);
        card.add(subtitle);
        card.add(usernamePanel);
        card.add(Box.createVerticalStrut(20));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(buttonsPanel);

        return card;
    }

    private JPanel createInputPanel(String labelText, JTextField inputField, JLabel errorLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 80));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the entire panel

        // Label
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the label
        label.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Input field styling
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        inputField.setMaximumSize(new Dimension(320, 45));
        inputField.setPreferredSize(new Dimension(320, 45));
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the input field

        // Error label
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the error label
        errorLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(label);
        panel.add(inputField);
        panel.add(errorLabel);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 120));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the entire panel

        // Login button
        logIn = createStyledButton(LOGIN_BUTTON_TEXT, PRIMARY_BLUE);
        logIn.setMaximumSize(new Dimension(320, 45));
        logIn.setPreferredSize(new Dimension(320, 45));
        logIn.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Cancel button
        cancel = createStyledButton(CANCEL_BUTTON_TEXT, SECONDARY_GRAY);
        cancel.setMaximumSize(new Dimension(320, 45));
        cancel.setPreferredSize(new Dimension(320, 45));
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Sign up link
        JLabel signUpLabel = new JLabel(SIGNUP_BUTTON_TEXT);
        signUpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signUpLabel.setForeground(PRIMARY_BLUE);
        signUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.setBorder(new EmptyBorder(15, 0, 0, 0));

        panel.add(logIn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cancel);
        panel.add(signUpLabel);

        return panel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker().darker(), 1),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
        });

        return button;
    }

    private void setupActionListeners() {
        logIn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource().equals(logIn)) {
                            final LoginState currentState = loginViewModel.getState();

                            loginController.execute(
                                    currentState.getUsername(),
                                    currentState.getPassword(),
                                    currentState.getAdmin()
                            );
                        }
                    }
                }
        );


        cancel.addActionListener(this);

        usernameInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final LoginState currentState = loginViewModel.getState();
                currentState.setUsername(usernameInputField.getText());
                loginViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentListenerHelper();
            }
        });

        passwordInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final LoginState currentState = loginViewModel.getState();
                currentState.setPassword(new String(passwordInputField.getPassword()));
                loginViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentListenerHelper();
            }
        });
    }

    /**
     * React to a button click that results in evt.
     * @param evt the ActionEvent to react to
     */
    public void actionPerformed(ActionEvent evt) {
        viewManagerModel.pushView("sign up");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final LoginState state = (LoginState) evt.getNewValue();
        setFields(state);
        usernameErrorField.setText(state.getLoginError());
    }

    private void setFields(LoginState state) {
        usernameInputField.setText(state.getUsername());
        passwordInputField.setText(state.getPassword());
    }

    public String getViewName() {
        return VIEW_NAME;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
}
