package view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;
import interface_adapter.ViewManagerModel;

/**
 * The View for the Signup Use Case.
 */
public class SignupView extends JPanel implements ActionListener, PropertyChangeListener {
    private final String viewName = "sign up";

    private final SignupViewModel signupViewModel;
    private final ViewManagerModel viewManagerModel;
    private final JTextField usernameInputField = new JTextField(20);
    private final JPasswordField passwordInputField = new JPasswordField(20);
    private final JPasswordField repeatPasswordInputField = new JPasswordField(20);
    private SignupController signupController;
    // ... existing fields ...
    private final JTextField adminCodeField = new JTextField(20);
    private final JCheckBox isAdminCheckBox = new JCheckBox("Sign up as Admin");
    private static final String ADMIN_CODE = "csc207";

    private JButton signUp;
    private JButton cancel;
    private JButton toLogin;

    public SignupView(SignupViewModel signupViewModel, ViewManagerModel viewManagerModel) {
        this.signupViewModel = signupViewModel;
        this.viewManagerModel = viewManagerModel;
        signupViewModel.addPropertyChangeListener(this);

        // Set up the main panel with modern styling
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(248, 249, 250)); // Light gray background

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

        // Create the signup card
        JPanel signupCard = createSignupCard();
        signupCard.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the card
        mainPanel.add(signupCard);

        return mainPanel;
    }

    private JPanel createSignupCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(400, 600));
        card.setPreferredSize(new Dimension(400, 600));

        // Title with modern styling
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Subtitle
        JLabel subtitle = new JLabel("Join our community today");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.BLACK);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Username field
        JPanel usernamePanel = createInputPanel("Username", usernameInputField);
        
        // Password field
        JPanel passwordPanel = createInputPanel("Password", passwordInputField);
        
        // Repeat password field
        JPanel repeatPasswordPanel = createInputPanel("Confirm Password", repeatPasswordInputField);

        // Create admin section panel
        JPanel adminPanel = createAdminPanel();
        
        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();

        // Add components to card
        card.add(title);
        card.add(subtitle);
        card.add(usernamePanel);
        card.add(Box.createVerticalStrut(20));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(repeatPasswordPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(adminPanel);           // Add admin panel
        card.add(Box.createVerticalStrut(30));
        card.add(buttonsPanel);

        return card;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 100));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the entire panel

        // Admin checkbox
        isAdminCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        isAdminCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the checkbox
        
        // Admin code field panel
        JPanel adminCodePanel = createInputPanel("Admin Code", adminCodeField);
        adminCodePanel.setVisible(false);  // Initially hidden

        // Add listener to checkbox to control adminCodePanel visibility
        isAdminCheckBox.addActionListener(e -> {
            adminCodePanel.setVisible(isAdminCheckBox.isSelected());
            panel.revalidate();
            panel.repaint();
        });

        panel.add(isAdminCheckBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(adminCodePanel);

        return panel;
    }

    private void toggleAdminCodeField() {
        // Find the admin code panel and toggle its visibility
        Component[] components = this.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] cardComponents = ((JPanel) component).getComponents();
                for (Component cardComponent : cardComponents) {
                    if (cardComponent instanceof JPanel) {
                        Component[] panelComponents = ((JPanel) cardComponent).getComponents();
                        for (Component panelComponent : panelComponents) {
                            if (panelComponent instanceof JPanel && 
                                ((JPanel) panelComponent).getComponent(0) instanceof JLabel && 
                                ((JLabel) ((JPanel) panelComponent).getComponent(0)).getText().equals("Admin Code")) {
                                panelComponent.setVisible(isAdminCheckBox.isSelected());
                                this.revalidate();
                                this.repaint();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private JPanel createInputPanel(String labelText, JTextField inputField) {
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

        panel.add(label);
        panel.add(inputField);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 150));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the entire panel

        // Sign up button
        signUp = createStyledButton("Create Account", new Color(40, 167, 69));
        signUp.setMaximumSize(new Dimension(320, 45));
        signUp.setPreferredSize(new Dimension(320, 45));
        signUp.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // To login button
        toLogin = createStyledButton("Back to Login", new Color(0, 123, 255));
        toLogin.setMaximumSize(new Dimension(320, 45));
        toLogin.setPreferredSize(new Dimension(320, 45));
        toLogin.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Cancel button
        cancel = createStyledButton("Cancel", new Color(108, 117, 125));
        cancel.setMaximumSize(new Dimension(320, 45));
        cancel.setPreferredSize(new Dimension(320, 45));
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        panel.add(signUp);
        panel.add(Box.createVerticalStrut(10));
        panel.add(toLogin);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cancel);

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
        signUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(signUp)) {
                    SignupState currentState = signupViewModel.getState();
                    
                    // Check admin code if trying to sign up as admin
                    if (isAdminCheckBox.isSelected()) {
                        String enteredCode = adminCodeField.getText();
                        if (!ADMIN_CODE.equals(enteredCode)) {
                            JOptionPane.showMessageDialog(
                                SignupView.this,
                                "Invalid admin code.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }
                    }

                    // This creates an anonymous subclass of ActionListener and instantiates it.
                    String password = currentState.getPassword();
                    if (password == null || password.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            SignupView.this,
                            "Password cannot be blank.",
                            "Invalid Password",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    String strength = getPasswordStrength(password);
                    if ("weak".equals(strength)) {
                        int result = JOptionPane.showConfirmDialog(
                            SignupView.this,
                            "Your password is weak. Do you want to use it anyway?",
                            "Weak Password",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (result != JOptionPane.YES_OPTION) {
                            return;
                        }
                    } else if ("medium".equals(strength)) {
                        Object[] options = {"Use Password", "Change Password"};
                        int result = JOptionPane.showOptionDialog(
                            SignupView.this,
                            "Your password is medium strength. Do you want to reconsider?",
                            "Medium Password",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                        );
                        if (result != 0) { // 0 = Use Password, 1 = Change Password
                            return;
                        }
                    } else if ("strong".equals(strength)) {
                        JOptionPane.showMessageDialog(
                            SignupView.this,
                            "Your password is strong! Good job!",
                            "Strong Password",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    signupController.execute(
                        currentState.getUsername(),
                        currentState.getPassword(),
                        currentState.getRepeatPassword(),
                        isAdminCheckBox.isSelected()  // Pass admin status
                    );
                }
            }
        });

        toLogin.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        signupController.switchToLoginView();
                    }
                }
        );

        cancel.addActionListener(this);

        addUsernameListener();
        addPasswordListener();
        addRepeatPasswordListener();
    }

    private void addUsernameListener() {
        usernameInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setUsername(usernameInputField.getText());
                signupViewModel.setState(currentState);
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

    private void addPasswordListener() {
        passwordInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setPassword(new String(passwordInputField.getPassword()));
                signupViewModel.setState(currentState);
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

    private void addRepeatPasswordListener() {
        repeatPasswordInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setRepeatPassword(new String(repeatPasswordInputField.getPassword()));
                signupViewModel.setState(currentState);
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

    private String getPasswordStrength(String password) {
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");
        if ((hasLetter && !hasDigit && !hasSpecial) || (!hasLetter && hasDigit && !hasSpecial)) {
            return "weak";
        } else if (hasLetter && hasDigit && !hasSpecial) {
            return "medium";
        } else if (hasLetter && hasDigit && hasSpecial) {
            return "strong";
        } else {
            return "weak";
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        viewManagerModel.popViewOrClose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final SignupState state = (SignupState) evt.getNewValue();
        if (state.getUsernameError() != null) {
            JOptionPane.showMessageDialog(this, state.getUsernameError());
        }
    }

    public String getViewName() {
        return viewName;
    }

    public void setSignupController(SignupController controller) {
        this.signupController = controller;
    }
}