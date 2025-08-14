package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Component;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppBuilderTest {
    private AppBuilder appBuilder;

    @BeforeEach
    void setUp() {
        appBuilder = new AppBuilder();
    }

    @Test
    @DisplayName("Test basic builder initialization")
    void testInitialization() {
        assertNotNull(appBuilder, "AppBuilder should be initialized");
    }

    @Test
    @DisplayName("Test complete build with all components")
    void testCompleteBuild() {
        JFrame app = appBuilder
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
                .addChangeUsernameUseCase()
                .addDMsUseCase()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertEquals("Login Example", app.getTitle(), "Frame title should be set correctly");
        assertTrue(containsViewWithName(app, "sign up"), "Should contain signup view");
        assertTrue(containsViewWithName(app, "log in"), "Should contain login view");
    }

    @Test
    @DisplayName("Test admin components addition")
    void testAdminComponentsAddition() {
        JFrame app = appBuilder
                .addSignupView()  // Add this line to initialize signup view
                .addAdminView()
                .addAdminLoggedInView()
                .addAdminUseCase()
                .addDeletePostUseCase()
                .addDeleteUserView()
                .addDeleteUserUseCase()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertTrue(containsViewWithName(app, "admin"), "Should contain admin view");
        assertTrue(containsViewWithName(app, "admin logged in"), "Should contain admin logged in view");
    }

    @Test
    @DisplayName("Test search components addition")
    void testSearchComponentsAddition() {
        JFrame app = appBuilder
                .addSignupView()
                .addSearchView()            // Add this line to create the search view
                .addAdvancedSearchView()    // Add this line to create the advanced search view
                .addFuzzySearchView()       // Add this line to create the fuzzy search view
                .addAdminView()
                .addAdminLoggedInView()
                .addAdminUseCase()
                .addDeletePostUseCase()
                .addDeleteUserView()
                .addDeleteUserUseCase()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertTrue(containsViewWithName(app, "search"), "Should contain search view");
        assertTrue(containsViewWithName(app, "advanced search"), "Should contain advanced search view");
        assertTrue(containsViewWithName(app, "fuzzy search"), "Should contain fuzzy search view");
    }

    @Test
    @DisplayName("Test view manager setup")
    void testViewManagerSetup() {
        JFrame app = appBuilder
                .addSignupView()
                .addLoginView()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertEquals(JFrame.EXIT_ON_CLOSE, app.getDefaultCloseOperation(),
                "Default close operation should be set");
    }

    @Test
    @DisplayName("Test DMs setup")
    void testDMsSetup() {
        JFrame app = appBuilder
                .addSignupView()  // Initialize signup view
                .addAdminView()
                .addAdminLoggedInView()
                .addDMsView()      // Add this line to initialize DMs view
                .addAdminUseCase()
                .addDeletePostUseCase()
                .addDeleteUserView()
                .addDeleteUserUseCase()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertTrue(containsViewWithName(app, "dms"), "Should contain DMs view");
    }

    @Test
    @DisplayName("Test builder pattern fluent interface")
    void testBuilderPattern() {
        AppBuilder builder = appBuilder.addSignupView();
        assertSame(builder, appBuilder, "Builder methods should return same instance");
    }

    @Test
    @DisplayName("Test view dependencies")
    void testViewDependencies() {
        // Testing that views that depend on each other are properly linked
        JFrame app = appBuilder
                .addDashboardView()
                .addLoggedInView()
                .addDashboardUseCase()
                .build();

        assertNotNull(app, "Built application should not be null");
        assertTrue(containsViewWithName(app, "dashboard"), "Should contain dashboard view");
    }

    // Helper method to check if a component with a specific name exists in the frame
    private boolean containsViewWithName(JFrame frame, String viewName) {
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] panelComponents = ((JPanel) component).getComponents();
                for (Component panelComponent : panelComponents) {
                    if (panelComponent.getName() != null &&
                            panelComponent.getName().toLowerCase().contains(viewName.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}