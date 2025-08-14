package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppBuilder builder = new AppBuilder();
            builder
                    .addSignupView()
                    .addLoginView()
                    .addLoggedInView()
                    .addAdminLoggedInView()
                    .addSearchView()
                    .addAdvancedSearchView()
                    .addDashboardView()
                    .addAdminView()
                    .addAccountView()
                    .addDMsView()
                    .addDeleteUserView()
                    .addSignupUseCase()
                    .addLoginUseCase()
                    .addChangePasswordUseCase()
                    .addLogoutUseCase()
                    .addSearchUseCase()
                    .addDashboardUseCase()
                    .addAdminUseCase()
                    .addChangeUsernameUseCase()
                    .addDeletePostUseCase()
                    .addDeleteUserUseCase()
                    .addFuzzySearchView()
                    .addDMsUseCase();

            JFrame application = builder.build();

            // Add shutdown hook to properly close Firebase connections
            application.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Shutting down application...");
                    try {
                        data_access.FirebaseConfig.shutdown();
                    } catch (Exception ex) {
                        System.err.println("Error during shutdown: " + ex.getMessage());
                    }
                    System.exit(0);
                }
            });

            application.setSize(1000, 700);
            application.setVisible(true);
        });
    }
}
