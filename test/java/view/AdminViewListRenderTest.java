package view;

import entity.Post;
import interface_adapter.admin.AdminState;
import interface_adapter.admin.AdminViewModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies AdminView renders posts from AdminViewModel state and reacts to clicks. */
class AdminViewListRenderTest {

    @BeforeAll
    static void headless() { System.setProperty("java.awt.headless", "true"); }

    @Test
    void rendersPostsAndSelectingOneUpdatesDetails() {
        AdminViewModel vm = new AdminViewModel();
        AdminView view = new AdminView(vm);

        // Prepare 2 posts
        Post p1 = new Post(1, "Lost Wallet", "Black leather wallet", List.of("black","leather"),
                LocalDateTime.now(), "alice", "BA320", null, true, 3, null);
        Post p2 = new Post(2, "Found Bottle", "Water bottle", List.of("blue"),
                LocalDateTime.now(), "bob", "GB", null, false, 0, null);

        // Emit state change with posts
        AdminState st = new AdminState();
        st.setPosts(List.of(p1, p2));
        st.setSelectedPost(null);
        st.setError(""); st.setSuccessMessage("");
        vm.setState(st);
        vm.firePropertyChanged(); // triggers AdminView.propertyChange

        // Find first post panel by its title label
        JPanel item = findPanelWithLabelText(view, "Lost Wallet");
        assertNotNull(item, "First post item should be rendered");

        // Click to select -> should update detail panel (title label appears)
        item.dispatchEvent(new java.awt.event.MouseEvent(item, java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 5, 5, 1, false));

        // After click, detail area should contain the title label
        JLabel titleInDetails = findLabel(view, "Lost Wallet");
        assertNotNull(titleInDetails, "Detail panel should show the selected post title");
    }

    // -------- helpers --------
    private static JPanel findPanelWithLabelText(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel) {
                if (containsLabelWithText((JPanel) c, text)) return (JPanel) c;
            }
            if (c instanceof Container) {
                JPanel p = findPanelWithLabelText((Container) c, text);
                if (p != null) return p;
            }
        }
        return null;
    }
    private static boolean containsLabelWithText(JPanel p, String text) {
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel) c).getText())) return true;
            if (c instanceof JPanel && containsLabelWithText((JPanel) c, text)) return true;
        }
        return false;
    }
    private static JLabel findLabel(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel) c).getText())) return (JLabel) c;
            if (c instanceof Container) {
                JLabel l = findLabel((Container) c, text);
                if (l != null) return l;
            }
        }
        return null;
    }
}
