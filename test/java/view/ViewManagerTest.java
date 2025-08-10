package view;

import interface_adapter.ViewManagerModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ViewManager.
 * We verify that only "state" events trigger a CardLayout switch.
 */
class ViewManagerTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void propertyChange_switchesCardsByName_onStateEvents() {
        CardLayout layout = new CardLayout();
        JPanel root = new JPanel(layout);
        JPanel login = new JPanel();
        JPanel signup = new JPanel();

        // NOTE: LoginView's viewName is "log in"
        root.add(login, "log in");
        root.add(signup, "sign up");
        layout.show(root, "log in");
        assertTrue(login.isVisible());

        ViewManagerModel model = mock(ViewManagerModel.class);
        ViewManager vm = new ViewManager(root, layout, model);
        verify(model).addPropertyChangeListener(any(PropertyChangeListener.class));

        // Switch to "sign up"
        vm.propertyChange(new PropertyChangeEvent(this, "state", "log in", "sign up"));
        assertTrue(signup.isVisible());
        assertFalse(login.isVisible());

        // Back to "log in"
        vm.propertyChange(new PropertyChangeEvent(this, "state", "sign up", "log in"));
        assertTrue(login.isVisible());
        assertFalse(signup.isVisible());
    }

    @Test
    void ignoresNonStateEvents() {
        CardLayout layout = new CardLayout();
        JPanel root = new JPanel(layout);
        JPanel a = new JPanel();
        JPanel b = new JPanel();
        root.add(a, "A");
        root.add(b, "B");
        layout.show(root, "A");

        ViewManager vm = new ViewManager(root, layout, mock(ViewManagerModel.class));
        vm.propertyChange(new PropertyChangeEvent(this, "other", null, "B"));

        assertTrue(a.isVisible());
        assertFalse(b.isVisible());
    }
}
