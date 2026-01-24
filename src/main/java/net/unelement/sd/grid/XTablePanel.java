package net.unelement.sd.grid;

import net.unelement.sd.subtitle.SubtitleEvent;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class XTablePanel extends JPanel {

    private final JTable table;
    private final XTableModel  model;

    public XTablePanel(){
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        model = new XTableModel();
        table.setModel(model);
        table.setDefaultRenderer(Long.class, new XTableRenderer());

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        updateColumnSize();
    }

    public void updateColumnSize(){
        final TableColumnModel cm = table.getColumnModel();

        for(int i=0; i<table.getColumnCount(); i++){
            switch(i){
                case 0 -> cm.getColumn(i).setPreferredWidth(70);
                case 1, 2 -> cm.getColumn(i).setPreferredWidth(110);
                case 3 -> cm.getColumn(i).setPreferredWidth(700);
            }
        }

        table.updateUI();
    }

    public void addSubtitle(SubtitleEvent event){
        model.getSubtitleEvents().add(event);
        table.updateUI();
    }

    public void replaceSubtitle(SubtitleEvent event, int row){
        model.replaceRow(row, event);
        table.updateUI();
    }

    public void removeSubtitle(int row){
        model.removeRow(row);
        table.updateUI();
    }

    public void clearSubtitles(){
        model.clear();
        table.updateUI();
    }

    public List<SubtitleEvent> getSubtitles(){
        return model.getSubtitleEvents();
    }

}
