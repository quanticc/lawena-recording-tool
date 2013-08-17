
package vdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

public class DemoTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("lawena");

    public enum Columns {
        DEMONAME, MAP, PLAYER, TICKS, TIME, SERVER;
    }

    private List<Demo> list;

    public DemoTableModel() {
        list = new ArrayList<>();
    }

    public DemoTableModel(Path path) {
        this();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                path, "*.dem")) {
            int row = getRowCount();
            for (Path demopath : stream) {
                list.add(new Demo(demopath));
            }
            fireTableRowsInserted(row, getRowCount() - 1);
        } catch (IOException e) {
            log.log(Level.INFO, "Problem while scanning .dem files", e);
        }
        try {
            for (String line : Files.readAllLines(path.resolve("KillStreaks.txt"),
                    Charset.defaultCharset())) {
                if (!line.isEmpty()) {
                    KillStreak streak = new KillStreak(line);
                    log.finer("demo: " + streak.getDemoname() + ", type: "
                            + streak.getDescription() + ", tick: " + streak.getTick());
                    for (Demo demo : list) {
                        if (demo.getPath().getFileName().toString().equals(streak.getDemoname())) {
                            log.finer("adding streak: " + streak);
                            demo.getStreaks().add(streak);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.log(Level.FINER, "Problem while reading KillStreaks.txt", e);
        }
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return Columns.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Demo demo = list.get(rowIndex);
        Columns c = Columns.values()[columnIndex];
        switch (c) {
            case DEMONAME:
                return demo.getPath().getFileName();
            case MAP:
                return demo.getMapName();
            case PLAYER:
                return demo.getPlayerName();
            case SERVER:
                return demo.getServerName();
            case TICKS:
                return demo.getTickNumber();
            case TIME:
                return demo.getPlaybackTime();
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        Columns c = Columns.values()[column];
        switch (c) {
            case DEMONAME:
                return "Filename";
            case MAP:
                return "Map";
            case PLAYER:
                return "Player";
            case SERVER:
                return "Server";
            case TICKS:
                return "Total Ticks";
            case TIME:
                return "Total Time";
        }
        return super.getColumnName(column);
    }

    public void addDemo(Path path) {
        for (Demo demo : list) {
            if (demo.getPath().equals(path)) {
                log.finer(path + " was not added because it already exists in the list");
                return;
            }
        }
        try {
            int row = getRowCount();
            list.add(new Demo(path));
            fireTableRowsInserted(row, row);
        } catch (FileNotFoundException e) {
            log.fine(path + " was not added because the file does not exist");
        }
    }

    public void removeDemo(Path path) {
        int i = 0;
        for (Demo demo : list) {
            if (demo.getPath().equals(path)) {
                list.remove(i);
                fireTableRowsDeleted(i, i);
            }
            i++;
        }
    }

    public Demo getDemo(int row) {
        return list.get(row);
    }

}
