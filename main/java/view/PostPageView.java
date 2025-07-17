package view;

import interface_adapter.post_page.PostPageState;
import interface_adapter.post_page.PostPageViewModel;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PostPageView extends JPanel implements ActionListener, PropertyChangeListener {
    private final String viewName = "post page";

    private final PostPageViewModel postPageViewModel;
    private final JButton postsButton;
    private final JButton searchButton;
    private final JButton backButton;

    public PostPageView(PostPageViewModel postPageViewModel) {
        this.postPageViewModel = postPageViewModel;
        this.postPageViewModel.addPropertyChangeListener(this);

        final JLabel title = new JLabel("Post Page Screen");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JPanel buttons = new JPanel();

        postsButton = new JButton("Posts");
        buttons.add(postsButton);
        postsButton.addActionListener(this);

        searchButton = new JButton("search");
        buttons.add(searchButton);
        searchButton.addActionListener(this);

        backButton = new JButton("Back");
        buttons.add(backButton);
        backButton.addActionListener(this);

        this.add(title);
        this.add(buttons);
    }

    public String getViewName() {
        return viewName;
    }

    /**
     * @param evt the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, evt.getActionCommand() + " clicked!");
    }

    /**
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}