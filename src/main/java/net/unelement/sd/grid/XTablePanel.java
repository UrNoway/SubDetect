package net.unelement.sd.grid;

import net.unelement.sd.subtitle.SubtitleEvent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class XTablePanel extends JPanel {

    private final JTable table;
    private final JScrollPane scrollPane;
    private final XTableModel  model;

    public XTablePanel(){
        table = new JTable();
        scrollPane = new JScrollPane(table);

        model = new XTableModel();
        table.setModel(model);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addSubtitle(SubtitleEvent event){
        model.getSubtitleEvents().add(event);
    }

    public void replaceSubtitle(SubtitleEvent event, int row){
        model.replaceRow(row, event);
    }

    public void removeSubtitle(int row){
        model.removeRow(row);
    }

    public void clearSubtitles(){
        model.clear();
    }

    public List<SubtitleEvent> getSubtitles(){
        return model.getSubtitleEvents();
    }

}
