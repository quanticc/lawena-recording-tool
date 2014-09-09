package com.github.lawena.util;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ScrollingDocumentListener implements DocumentListener {

  private JComponent component;
  private JScrollPane scrollPane;

  public ScrollingDocumentListener(JComponent component, JScrollPane scrollPane) {
    this.component = component;
    this.scrollPane = scrollPane;
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
    JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
    boolean scrollBarAtBottom = isScrollBarFullyExtended(scrollBar);
    boolean scrollLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
    if (scrollBarAtBottom && !scrollLock) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              scrollToBottom(component);
            }
          });
        }
      });
    }
  }

  private static boolean isScrollBarFullyExtended(JScrollBar vScrollBar) {
    BoundedRangeModel model = vScrollBar.getModel();
    return (model.getExtent() + model.getValue()) == model.getMaximum();
  }

  private static void scrollToBottom(JComponent component) {
    Rectangle visibleRect = component.getVisibleRect();
    visibleRect.y = component.getHeight() - visibleRect.height;
    component.scrollRectToVisible(visibleRect);
  }
}
