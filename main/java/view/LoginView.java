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

    private final String viewName = "log in";
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManagerModel;

    private final JTextField usernameInputField = new JTextField(20);
    private final JLabel usernameErrorField = new JLabel();

    private final JPasswordField passwordInputField = new JPasswordField(20);
    private final JLabel passwordErrorField = new JLabel();

    private JButton logIn;
    private JButton cancel;
    private LoginController loginController;

    public LoginView(LoginViewModel loginViewModel, ViewManagerModel viewManagerModel) {

        this.loginViewModel = loginViewModel;
        this.viewManagerModel = viewManagerModel;
        this.loginViewModel.addPropertyChangeListener(this);

        // Set up the main panel with gradient background
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(240, 242, 245)); // Light gray background
        
        // Create the main content panel
        JPanel mainContentPanel = createMainContentPanel();
        this.add(mainContentPanel, BorderLayout.CENTER);

        // Set up action listeners
        setupActionListeners();
    }

    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Create the login card
        JPanel loginCard = createLoginCard();
        loginCard.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the card
        mainPanel.add(loginCard);

        return mainPanel;
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(400, 500));
        card.setPreferredSize(new Dimension(400, 500));

        // Title with modern styling
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Subtitle
        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.BLACK);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Username field
        JPanel usernamePanel = createInputPanel("Username", usernameInputField, usernameErrorField);
        
        // Password field
        JPanel passwordPanel = createInputPanel("Password", passwordInputField, passwordErrorField);

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
        logIn = createStyledButton("Sign In", new Color(0, 123, 255));
        logIn.setMaximumSize(new Dimension(320, 45));
        logIn.setPreferredSize(new Dimension(320, 45));
        logIn.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Cancel button
        cancel = createStyledButton("Cancel", new Color(108, 117, 125));
        cancel.setMaximumSize(new Dimension(320, 45));
        cancel.setPreferredSize(new Dimension(320, 45));
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Sign up link
        JLabel signUpLabel = new JLabel("Don't have an account? Click here to sign up");
        signUpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signUpLabel.setForeground(new Color(0, 123, 255));
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
        return viewName;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
}
