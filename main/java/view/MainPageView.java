package view;

import interface_adapter.main_page.MainPageViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainPageView extends JPanel implements ActionListener, PropertyChangeListener {
    private final String viewName = "main page";

    private final MainPageViewModel mainPageViewModel;
    // private final MainPageController mainPageController;
    private final JButton postsButton;
    private final JButton dmButton;
    private final JButton accountButton;

    public MainPageView(MainPageViewModel mainPageViewModel) {
        this.mainPageViewModel = mainPageViewModel;
        // this.mainPageController = mainPageController;
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

//    /**
//     * @param evt the event to be processed
//     */
//    @Override
//    public void actionPerformed(ActionEvent evt) {
//        JOptionPane.showMessageDialog(this, evt.getActionCommand() + " clicked!");
//    }

    /**
     * @param evt the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, evt.getActionCommand() + " clicked!");

        // if (evt.getSource().equals(postsButton)) {
        // Navigate to posts page
        // mainPageController.navigateToPostsPage();
    }
//
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
