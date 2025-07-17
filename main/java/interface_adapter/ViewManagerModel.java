package interface_adapter;

import javax.swing.JFrame;
import java.util.Stack;

/**
 * Model for the View Manager. Its state is the name of the View which
 * is currently active. An initial state of "" is used.
 */
public class ViewManagerModel extends ViewModel<String> {

    private final Stack<String> navigationStack = new Stack<>();
    private JFrame mainFrame;

    public ViewManagerModel() {
        super("view manager");
        this.setState("");
    }

    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
    }

    public void pushView(String viewName) {
        if (getState() != null && !getState().isEmpty()) {
            navigationStack.push(getState());
        }
        setState(viewName);
        firePropertyChanged();
    }

    public void popViewOrClose() {
        if (!navigationStack.isEmpty()) {
            setState(navigationStack.pop());
            firePropertyChanged();
        } else if (mainFrame != null) {
            mainFrame.dispose();
        }
    }
}
