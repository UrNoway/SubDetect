package net.unelement.sd.image;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class BlockAreaRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        if(value instanceof BlockArea ba) {
            setIcon(ba.getIcon());
            setBorder(new LineBorder(Color.lightGray, 1));
        }

        return this;
    }
}
