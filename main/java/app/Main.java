package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppBuilder builder = new AppBuilder();
            builder
                    .addSignupView()
                    .addLoginView()
                    .addLoggedInView()
                    .addSearchView()
                    .addDashboardView()
                    .addAccountView()
                    .addDMsView()
                    .addSignupUseCase()
                    .addLoginUseCase()
                    .addChangePasswordUseCase()
                    .addLogoutUseCase()
                    .addSearchUseCase()
                    .addDashboardUseCase()
                    .addChangeUsernameUseCase();

            JFrame app = builder.build();
            app.setSize(1000, 700); // 可选：设定窗口大小
            app.setVisible(true);
        });
    }
}
