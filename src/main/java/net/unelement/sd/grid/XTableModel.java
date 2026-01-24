package net.unelement.sd.grid;

import net.unelement.sd.subtitle.SubtitleEvent;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class XTableModel extends DefaultTableModel {

    private final List<SubtitleEvent> subtitleEvents;

    public XTableModel(){
        subtitleEvents = new ArrayList<>();
    }

    public List<SubtitleEvent> getSubtitleEvents() {
        return subtitleEvents;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return subtitleEvents == null ? 0 : subtitleEvents.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return subtitleEvents.get(rowIndex);
    }

    @Override
    public String getColumnName(int column) {
        switch(column){
            case 0 -> { return "Selected"; }
            case 1 -> { return "Start"; }
            case 2 -> { return "End"; }
            case 3 -> { return "Subtitle"; }
            default -> { return "?"; }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex){
            case 0 -> { return Boolean.class; }
            case 1, 2 -> { return Long.class; }
            case 3 -> { return String.class; }
            default -> { return super.getColumnClass(columnIndex); }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex){
            case 0 -> { return true; }
            case 1, 2, 3 -> { return false; }
            default -> { return super.isCellEditable(rowIndex, columnIndex); }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(aValue instanceof SubtitleEvent event){
            if(subtitleEvents.size() > rowIndex){
                subtitleEvents.add(event);
            }else{
                subtitleEvents.set(rowIndex, event);
            }
        }
    }

    public void removeAtRow(int row){
        subtitleEvents.remove(row);
    }

    public void clear(){
        subtitleEvents.clear();
    }

    public void replaceRow(int row, SubtitleEvent event){
        subtitleEvents.set(row, event);
    }
}
