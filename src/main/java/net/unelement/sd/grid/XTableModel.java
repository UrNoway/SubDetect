package net.unelement.sd.grid;

import net.unelement.sd.subtitle.SrtSubtitles;
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object obj = null;

        switch(columnIndex){
            case 0 -> obj = subtitleEvents.get(rowIndex).isSelected();
            case 1 -> obj = subtitleEvents.get(rowIndex).getMicrosStart();
            case 2 -> obj = subtitleEvents.get(rowIndex).getMicrosEnd();
            case 3 -> obj = subtitleEvents.get(rowIndex).getText();
        }

        return obj;
    }

    @Override
    public void setValueAt(Object v, int rowIndex, int columnIndex) {
        SubtitleEvent event = subtitleEvents.get(rowIndex);

        switch(columnIndex){
            case 0 -> { if(v instanceof Boolean x) event.setSelected(x); }
            case 1 -> { if(v instanceof Long x) event.setMicrosStart(x); }
            case 2 -> { if(v instanceof Long x) event.setMicrosEnd(x); }
            case 3 -> { if(v instanceof String x) event.setText(x); }
        }

        subtitleEvents.set(rowIndex, event);
        fireTableCellUpdated(rowIndex, columnIndex);
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
