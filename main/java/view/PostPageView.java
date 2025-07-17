package view;

import interface_adapter.post_page.PostPageState;
import interface_adapter.post_page.PostPageViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PostPageView extends JPanel implements ActionListener, PropertyChangeListener {
    private final String viewName = "post page";
    private final PostPageViewModel postPageViewModel;
    private final JButton postButton;
    private final JList<String> postList;
    private final JButton searchButton;
    private final JButton backButton;
    private final JTextField searchField;
    private final JTextArea contentArea;
    private final JTextArea commentsArea;

    public PostPageView(PostPageViewModel postPageViewModel) {
        this.setLayout(new BorderLayout());
        this.postPageViewModel = postPageViewModel;
        // this.mainPageController = mainPageController;
        this.postPageViewModel.addPropertyChangeListener(this);

        // === Top Panel ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        postButton = new JButton("Post");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        backButton = new JButton("Back");

        topPanel.add(postButton);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(backButton);

        postButton.addActionListener(this);
        searchButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(topPanel, BorderLayout.NORTH);
        // === Center Panel with JSplitPane ===
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Post A");
        listModel.addElement("Post B");
        listModel.addElement("Post C");

        postList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(postList);

        contentArea = new JTextArea("Content shown here");
        commentsArea = new JTextArea("Comments go here");
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);

        contentArea.setEditable(false);
        commentsArea.setEditable(false);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        rightPanel.add(new JScrollPane(commentsArea), BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, rightPanel);
        splitPane.setDividerLocation(200);

        this.add(splitPane, BorderLayout.CENTER);

        // List selection listener
        postList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = postList.getSelectedValue();
                contentArea.setText("Content for " + selected);
                commentsArea.setText("Comments for " + selected);

    };
        })
    ;}
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