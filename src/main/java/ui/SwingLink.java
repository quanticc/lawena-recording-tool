package ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;

import util.LinkRunner;

/**
 * @see <a href="http://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel">Stack
 *      Overflow</a>
 */
public class SwingLink extends JLabel {

  private static final long serialVersionUID = 1L;

  private String text;
  private URI uri;

  public SwingLink(String text, URI uri) {
    super();
    setup(text, uri);
  }

  public SwingLink(String text, String uri) {
    super();
    URI oURI;
    try {
      oURI = new URI(uri);
    } catch (URISyntaxException e) {
      // converts to runtime exception for ease of use
      // if you cannot be sure at compile time that your
      // uri is valid, construct your uri manually and
      // use the other constructor.
      throw new RuntimeException(e);
    }
    setup(text, oURI);
  }

  public void setup(String t, URI u) {
    text = t;
    uri = u;
    setText(text);
    setToolTipText(uri.toString());
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        open(uri);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        setText(text, false);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setText(text, true);
      }
    });
  }

  @Override
  public void setText(String text) {
    setText(text, true);
  }

  public void setText(String text, boolean ul) {
    String link = ul ? "<u>" + text + "</u>" : text;
    super.setText("<html><span style=\"color: #000099;\">" + link + "</span></html>");
    this.text = text;
  }

  public String getRawText() {
    return text;
  }

  private static void open(URI uri) {
    new LinkRunner(uri).execute();
  }
}
