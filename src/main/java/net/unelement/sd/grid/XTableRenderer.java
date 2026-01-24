package net.unelement.sd.grid;

import net.unelement.sd.subtitle.SrtSubtitles;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class XTableRenderer extends JLabel implements TableCellRenderer {

    public XTableRenderer() {

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        if(value instanceof Long x){
            setText(Long.toString(x / 1_000L));
        }

        return this;
    }
}
