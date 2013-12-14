
package util;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Mike Clark
 * @see http://stackoverflow.com/a/4047794
 */
public class ScrollingDocumentListener implements DocumentListener {

    private JScrollPane jScrollPane;
    private JTextArea jTextArea;

    public ScrollingDocumentListener(JScrollPane jScrollPane, JTextArea jTextArea) {
        this.jScrollPane = jScrollPane;
        this.jTextArea = jTextArea;
    }

    public void changedUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    public void insertUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    public void removeUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    private void maybeScrollToBottom() {
        JScrollBar scrollBar = jScrollPane.getVerticalScrollBar();
        boolean scrollBarAtBottom = isScrollBarFullyExtended(scrollBar);
        boolean scrollLock = Toolkit.getDefaultToolkit()
                .getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
        if (scrollBarAtBottom && !scrollLock) {
            // Push the call to "scrollToBottom" back TWO PLACES on the
            // AWT-EDT queue so that it runs *after* Swing has had an
            // opportunity to "react" to the appending of new text:
            // this ensures that we "scrollToBottom" only after a new
            // bottom has been recalculated during the natural
            // revalidation of the GUI that occurs after having
            // appending new text to the JTextArea.
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            scrollToBottom(jTextArea);
                        }
                    });
                }
            });
        }
    }

    public static boolean isScrollBarFullyExtended(JScrollBar vScrollBar) {
        BoundedRangeModel model = vScrollBar.getModel();
        return (model.getExtent() + model.getValue()) == model.getMaximum();
    }

    public static void scrollToBottom(JComponent component) {
        Rectangle visibleRect = component.getVisibleRect();
        visibleRect.y = component.getHeight() - visibleRect.height;
        component.scrollRectToVisible(visibleRect);
    }
}
