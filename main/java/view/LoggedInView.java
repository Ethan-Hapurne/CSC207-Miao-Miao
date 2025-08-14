package view;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import java.awt.*;

import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.LoggedInState;
import interface_adapter.change_password.LoggedInViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.ViewManagerModel;
import interface_adapter.dashboard.DashboardController;
import view.DMsView;

/**
 * The View for when the user is logged into the program.
 */
public class LoggedInView extends JPanel implements PropertyChangeListener {

    private final String viewName = "logged in";
    private final LoggedInViewModel loggedInViewModel;
    private final JLabel passwordErrorField = new JLabel();
    private ChangePasswordController changePasswordController;
    private LogoutController logoutController;
    private ViewManagerModel viewManagerModel;
    private DashboardController dashboardController;
    private DMsView dmsView;

    private JLabel username;

    private JButton dashboardButton;
    private JButton accountButton;
    private JButton dmsButton;
    private JButton fuzzySearchButton;

    public LoggedInView(LoggedInViewModel loggedInViewModel, ViewManagerModel viewManagerModel) {
        this.loggedInViewModel = loggedInViewModel;
        this.viewManagerModel = viewManagerModel;
        this.loggedInViewModel.addPropertyChangeListener(this);

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

        // Create the welcome card
        JPanel welcomeCard = createWelcomeCard();
        mainPanel.add(welcomeCard);

        return mainPanel;
    }

    private JPanel createWelcomeCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(500, 400));
        card.setPreferredSize(new Dimension(500, 400));

        // Title with modern styling
        JLabel title = new JLabel("Welcome Back!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Username info
        JLabel usernameInfo = new JLabel("Currently logged in: ");
        usernameInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameInfo.setForeground(Color.BLACK);
        usernameInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        username = new JLabel();
        username.setFont(new Font("Segoe UI", Font.BOLD, 16));
        username.setForeground(new Color(0, 123, 255));
        username.setAlignmentX(Component.CENTER_ALIGNMENT);
        username.setBorder(new EmptyBorder(5, 0, 30, 0));

        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();

        // Add components to card
        card.add(title);
        card.add(usernameInfo);
        card.add(username);
        card.add(buttonsPanel);

        return card;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 200));

        // Create a grid of buttons (2x2)
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonGrid.setOpaque(false);
        buttonGrid.setMaximumSize(new Dimension(600, 150));

        // Style the buttons
        dashboardButton = createStyledButton("Dashboard", new Color(0, 123, 255));
        accountButton = createStyledButton("Account", new Color(255, 193, 7));
        dmsButton = createStyledButton("DMs", new Color(220, 53, 69));
        fuzzySearchButton = createStyledButton("Fuzzy Search", new Color(40, 167, 69));

        // Set button sizes
        Dimension buttonSize = new Dimension(180, 50);
        dashboardButton.setPreferredSize(buttonSize);
        accountButton.setPreferredSize(buttonSize);
        dmsButton.setPreferredSize(buttonSize);

        buttonGrid.add(dashboardButton);
        buttonGrid.add(accountButton);
        buttonGrid.add(dmsButton);
        buttonGrid.add(fuzzySearchButton);

        panel.add(buttonGrid);

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
        dashboardButton.addActionListener(evt -> {
            if (evt.getSource().equals(dashboardButton)) {
                // SESSION CHANGE: Set current user in DashboardController before switching to dashboard
                final LoggedInState currentState = loggedInViewModel.getState();
                if (dashboardController != null) {
                    dashboardController.setCurrentUser(currentState.getUsername());
                }
                viewManagerModel.pushView("dashboard");
            }
        });

        accountButton.addActionListener(evt -> {
            if (evt.getSource().equals(accountButton)) {
                viewManagerModel.pushView("account");
            }
        });

        dmsButton.addActionListener(evt -> {
            if (evt.getSource().equals(dmsButton)) {
                if (dmsView != null) {
                    dmsView.setCurrentUsername(username.getText());
                }
                viewManagerModel.pushView("dms");
            }
        });

        fuzzySearchButton.addActionListener(evt -> {
            if (evt.getSource().equals(fuzzySearchButton)) {
                viewManagerModel.pushView("fuzzy search");
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            final LoggedInState state = (LoggedInState) evt.getNewValue();
            username.setText(state.getUsername());
        }
        else if (evt.getPropertyName().equals("password")) {
            final LoggedInState state = (LoggedInState) evt.getNewValue();
        }

    }

    public String getViewName() {
        return viewName;
    }

    public void setChangePasswordController(ChangePasswordController changePasswordController) {
        this.changePasswordController = changePasswordController;
    }

    public void setLogoutController(LogoutController logoutController) {
        this.logoutController = logoutController;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setDMsView(DMsView dmsView) {
        this.dmsView = dmsView;
    }
}
