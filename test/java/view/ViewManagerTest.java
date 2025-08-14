package view;

import interface_adapter.ViewManagerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ViewManager}.
 *
 * Notes:
 * - We do not use Mockito (ByteBuddy issues on newer JDKs). Instead we use real Swing components
 *   and the real {@link ViewManagerModel}.
 * - CardLayout does not expose the "current card", so we assert visibility of the panels.
 */
public class ViewManagerTest {

    private static final String LOGIN = "log in";
    private static final String SIGNUP = "sign up";

    private JPanel root;            // the container managed by CardLayout
    private CardLayout cardLayout;  // the layout under test
    private JPanel loginPanel;      // card #1
    private JPanel signupPanel;     // card #2

    private ViewManagerModel model;
    private ViewManager manager;

    @BeforeEach
    void setUp() throws Exception {
        // Build the Swing UI on the EDT to avoid race conditions.
        runOnEdt(() -> {
            cardLayout = new CardLayout();
            root = new JPanel(cardLayout);

            // Prepare two simple cards.
            loginPanel = new JPanel();
            signupPanel = new JPanel();

            root.add(loginPanel, LOGIN);
            root.add(signupPanel, SIGNUP);

            // Model + manager wiring.
            model = new ViewManagerModel();
            manager = new ViewManager(root, cardLayout, model);
        });
    }

    @Test
    void pushView_showsRequestedCard() throws Exception {
        runOnEdt(() -> {
            // When: push the LOGIN view
            model.pushView(LOGIN);

            // Then: LOGIN card is visible, SIGNUP is not
            assertTrue(loginPanel.isVisible(), "Login card should be visible after pushView(LOGIN)");
            assertFalse(signupPanel.isVisible(), "Signup card should be hidden when LOGIN is visible");

            // When: push the SIGNUP view
            model.pushView(SIGNUP);

            // Then: SIGNUP card is visible, LOGIN is not
            assertTrue(signupPanel.isVisible(), "Signup card should be visible after pushView(SIGNUP)");
            assertFalse(loginPanel.isVisible(), "Login card should be hidden when SIGNUP is visible");
        });
    }

    @Test
    void popViewOrClose_returnsToPreviousCard() throws Exception {
        runOnEdt(() -> {
            // Given: navigate to LOGIN, then to SIGNUP
            model.pushView(LOGIN);
            model.pushView(SIGNUP);
            assertTrue(signupPanel.isVisible(), "Precondition: SIGNUP must be visible");

            // When: pop the view stack
            model.popViewOrClose();

            // Then: back to LOGIN
            assertTrue(loginPanel.isVisible(), "Login card should be visible after popping from SIGNUP");
            assertFalse(signupPanel.isVisible(), "Signup card should be hidden after popping");
        });
    }

    /**
     * Utility to run code on the Swing Event Dispatch Thread and wait for completion.
     * This keeps layout/visibility changes deterministic for assertions.
     */
    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }
}
