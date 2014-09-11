package net.tomahawk;

import java.io.File;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionsFilter extends FileFilter {

  private static final Logger log = LoggerFactory.getLogger(ExtensionsFilter.class);

  static final char SPACE = ' ';
  static final char ASTERISK = '*';
  static final char SEMICOLON = ';';
  static final char VBAR = '|';
  static final char OPEN_PAR = '(';
  static final char CLOSE_PAR = ')';
  static final String POINT = ".";
  List<String> extensions;
  String shortDescription;
  String description;

  // constructor 1 (from a List)
  public ExtensionsFilter(String shortDescription, List<String> extensions) {
    this.extensions = extensions;
    this.shortDescription = shortDescription;
    StringBuilder desc = new StringBuilder();
    desc.append(shortDescription);
    desc.append(SPACE);
    desc.append(OPEN_PAR);
    for (String ext : extensions) {
      desc.append(ASTERISK);
      desc.append(POINT);
      desc.append(ext);
      desc.append(SEMICOLON);
    }
    description = desc.substring(0, desc.length() - 1) + CLOSE_PAR;
  }

  // key: the accept method
  public boolean accept(File f) {
    if (f.isDirectory())
      return true;
    if (extensions == null)
      return true;
    if (extensions.isEmpty())
      return true;
    String filename = f.getName().toLowerCase();
    for (int i = 0; i < extensions.size(); i++) {
      String ext = extensions.get(i);
      if (filename.endsWith(POINT + ext))
        return true;
    }
    return false;
  }

  // description
  public String getDescription() {
    return description;
  }

  // get the filter string for native filedialog
  // desc0|*.ext00;...;*.ext0n|desc1|*.ext10;...;*.ext1n...
  public static String getNativeString(ExtensionsFilter... filters) {
    StringBuilder filterString = new StringBuilder("");
    for (ExtensionsFilter extensionsFilter : filters) {
      filterString.append(extensionsFilter.description);
      filterString.append(VBAR);
      java.util.List<String> exts = extensionsFilter.extensions;
      int count = exts.size();
      for (int j = 0; j < count - 1; j++) {
        filterString.append(ASTERISK);
        filterString.append(POINT);
        filterString.append(exts.get(j));
        filterString.append(SEMICOLON);
      }
      filterString.append(ASTERISK);
      filterString.append(POINT);
      filterString.append(exts.get(count - 1));
      filterString.append(VBAR);
    }
    log.trace("Native filter string: {}", filterString);
    return filterString.toString();
  }

}
