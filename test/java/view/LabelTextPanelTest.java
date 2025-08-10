package view;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LabelTextPanel.
 * This is a small, stable component that gives easy line coverage.
 */
class LabelTextPanelTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void addsLabelAndTextField() {
        JLabel lbl = new JLabel("Search");
        JTextField tf = new JTextField(10);

        LabelTextPanel panel = new LabelTextPanel(lbl, tf);

        assertEquals(2, panel.getComponentCount());
        assertSame(lbl, panel.getComponent(0));
        assertSame(tf, panel.getComponent(1));
    }
}
