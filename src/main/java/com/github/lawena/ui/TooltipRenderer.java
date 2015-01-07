package com.github.lawena.ui;

import static com.github.lawena.util.Util.toPath;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.lawena.Messages;
import com.github.lawena.app.model.Resource;
import com.github.lawena.app.model.Settings;
import com.github.lawena.profile.Key;

public class TooltipRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;

  private Settings settings;

  public TooltipRenderer(Settings settings) {
    this.settings = settings;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    Resource cp = (Resource) value;
    Path tfpath = toPath(Key.gamePath.getValue(settings));
    Path path = cp.getPath();
    StringBuilder sb = new StringBuilder();
    sb.append("<html>" + Messages.getString("TooltipRenderer.filename") + "<b>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    sb.append(cp.getPath().getFileName());
    sb.append("</b><br>" + Messages.getString("TooltipRenderer.location")); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append((path.startsWith(tfpath) ? Messages.getString("TooltipRenderer.gamePath") //$NON-NLS-1$
        + tfpath.relativize(path) : path).toString());
    setToolTipText(sb.toString());
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }

}
