package view;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LabelTextPanel}.
 * These tests verify that the constructor correctly adds components to the panel.
 */
public class LabelTextPanelTest {

    @Test
    void constructorAddsLabelAndTextFieldInOrder() {
        JLabel label = new JLabel("Name:");
        JTextField textField = new JTextField("Default");

        // Create panel
        LabelTextPanel panel = new LabelTextPanel(label, textField);

        // Panel should contain exactly 2 components
        assertEquals(2, panel.getComponentCount(), "Panel should have exactly two components");

        // First component should be the same label instance passed in
        assertSame(label, panel.getComponent(0), "First component should be the provided JLabel");

        // Second component should be the same text field instance passed in
        assertSame(textField, panel.getComponent(1), "Second component should be the provided JTextField");
    }

    @Test
    void labelAndTextFieldHaveExpectedText() {
        JLabel label = new JLabel("Email:");
        JTextField textField = new JTextField("user@example.com");

        LabelTextPanel panel = new LabelTextPanel(label, textField);

        // Verify label text
        assertEquals("Email:", ((JLabel) panel.getComponent(0)).getText());

        // Verify text field text
        assertEquals("user@example.com", ((JTextField) panel.getComponent(1)).getText());
    }
}
