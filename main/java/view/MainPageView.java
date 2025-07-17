package view;

import interface_adapter.main_page.MainPageState;
import interface_adapter.main_page.MainPageViewModel;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainPageView extends JPanel implements ActionListener, PropertyChangeListener {
    private final String viewName = "main page";

    private final MainPageViewModel mainPageViewModel;
    private final JButton postsButton;
    private final JButton dmButton;
    private final JButton accountButton;

    public MainPageView(MainPageViewModel mainPageViewModel) {
        this.mainPageViewModel = mainPageViewModel;
        this.mainPageViewModel.addPropertyChangeListener(this);

        final JLabel title = new JLabel("Main Page Screen");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JPanel buttons = new JPanel();

        postsButton = new JButton("Posts");
        buttons.add(postsButton);
        postsButton.addActionListener(this);

        dmButton = new JButton("DM");
        buttons.add(dmButton);
        dmButton.addActionListener(this);

        accountButton = new JButton("Account");
        buttons.add(accountButton);
        accountButton.addActionListener(this);

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