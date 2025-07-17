package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import interface_adapter.post_page.PostPageViewModel;
import interface_adapter.post_page.PostPageState;

public class PostPageView extends JPanel implements ActionListener {

    private final JButton postButton;
    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton backButton;

    private final JList<String> postList;
    private final JTextArea contentArea;
    private final JTextArea commentsArea;

    public PostPageView() {
        this.setLayout(new BorderLayout());

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

        postList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = postList.getSelectedValue();
                contentArea.setText("Content for " + selected);
                commentsArea.setText("Comments for " + selected);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Post":
                JOptionPane.showMessageDialog(this, "Post clicked");
                break;
            case "Search":
                String query = searchField.getText();
                JOptionPane.showMessageDialog(this, "Searching for: " + query);
                break;
            case "Back":
                JOptionPane.showMessageDialog(this, "Back to main page");
                break;
        }
    }
}
