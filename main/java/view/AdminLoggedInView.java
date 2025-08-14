package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import interface_adapter.adminloggedIn.AdminLoggedInState;
import interface_adapter.adminloggedIn.AdminLoggedInViewModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.dashboard.DashboardController;
import interface_adapter.logout.LogoutController;
import interface_adapter.ViewManagerModel;

/**
 * Admin Logged-In View: visually aligned with the standard Logged-In view,
 * while keeping extra admin features.
 */
public class AdminLoggedInView extends JPanel implements PropertyChangeListener {

    private final String viewName = "admin logged in";
    private final AdminLoggedInViewModel adminloggedInViewModel;

    // Keep for compatibility with existing wiring (even if unused here)
    private final JLabel passwordErrorField = new JLabel();
    private ChangePasswordController changePasswordController;
    private LogoutController logoutController;
    private final ViewManagerModel viewManagerModel;
    private DMsView dmsView;

    private final JLabel username;

    // Navigation buttons (user features)
    private final JButton dashboardButton = new JButton("Dashboard");
    private final JButton searchButton = new JButton("Search");
    private final JButton accountButton = new JButton("Account");
    private final JButton dmsButton = new JButton("DMs");

    // Extra admin features
    private final JButton adminButton = new JButton("AdminDashboard");
    private final JButton deleteUsersButton = new JButton("Delete Users");

    public AdminLoggedInView(AdminLoggedInViewModel adminloggedInViewModel, ViewManagerModel viewManagerModel) {
        this.adminloggedInViewModel = adminloggedInViewModel;
        this.viewManagerModel = viewManagerModel;
        this.adminloggedInViewModel.addPropertyChangeListener(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setBackground(Color.WHITE);

        // "Welcome" header card to mirror the regular Logged-In view style
        final JPanel welcomeCard = new JPanel();
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(16, 16, 16, 16)
        ));
        welcomeCard.setBackground(Color.WHITE);

        final JLabel title = new JLabel("Welcome");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        final JLabel usernameInfo = new JLabel("Currently logged in:");
        usernameInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameInfo.setForeground(new Color(90, 90, 90));

        username = new JLabel();
        username.setAlignmentX(Component.LEFT_ALIGNMENT);
        username.setFont(username.getFont().deriveFont(Font.PLAIN, 14f));

        welcomeCard.add(title);
        welcomeCard.add(Box.createVerticalStrut(8));
        welcomeCard.add(usernameInfo);
        welcomeCard.add(Box.createVerticalStrut(4));
        welcomeCard.add(username);

        // Primary actions row (mirrors Logged-In view buttons layout)
        final JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsRow.setOpaque(false);

        stylePrimaryButton(dashboardButton);
        stylePrimaryButton(accountButton);
        stylePrimaryButton(dmsButton);
        stylePrimaryButton(adminButton);
        stylePrimaryButton(deleteUsersButton);


        buttonsRow.add(dashboardButton);
        buttonsRow.add(accountButton);
        buttonsRow.add(dmsButton);
        buttonsRow.add(adminButton);
        buttonsRow.add(deleteUsersButton);

        // Wire navigation
        dashboardButton.addActionListener(evt -> viewManagerModel.pushView("dashboard"));
        searchButton.addActionListener(evt -> viewManagerModel.pushView("advanced_search"));
        accountButton.addActionListener(evt -> viewManagerModel.pushView("account"));
        dmsButton.addActionListener(evt -> {
                    if (dmsView != null) {
                        dmsView.setCurrentUsername(username.getText());
                    }
                    viewManagerModel.pushView("dms");
        });
        adminButton.addActionListener(evt -> viewManagerModel.pushView("admin"));
        deleteUsersButton.addActionListener(evt -> viewManagerModel.pushView("delete users"));

        // Compose the page
        add(welcomeCard);
        add(Box.createVerticalStrut(16));
        add(buttonsRow);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        button.setPreferredSize(new Dimension(140, 36));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        button.setPreferredSize(new Dimension(140, 36));
    }

    private void styleDestructiveButton(JButton button) {
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBackground(new Color(244, 67, 54));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        button.setPreferredSize(new Dimension(140, 36));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName())) {
            final AdminLoggedInState state = (AdminLoggedInState) evt.getNewValue();
            username.setText(state.getUsername());
        } else if ("password".equals(evt.getPropertyName())) {
            // Intentionally kept to match interface; nothing to do for UI here.
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

    public void setDMsView(DMsView dmsView) {
        this.dmsView = dmsView;
    }
}