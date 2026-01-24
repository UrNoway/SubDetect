package net.unelement.sd.image;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockArea {

    private boolean top;
    private boolean bottom;
    private boolean middle;

    private final List<ImageIcon> icons;

    public BlockArea(boolean top, boolean bottom, boolean middle) {
        this.top = top;
        this.bottom = bottom;
        this.middle = middle;

        icons = new ArrayList<>();

        ImageIcon iAreaB = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-b.png"))
        );
        ImageIcon iAreaT = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-t.png"))
        );
        ImageIcon iAreaM = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-m.png"))
        );
        ImageIcon iAreaBM = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-bm.png"))
        );
        ImageIcon iAreaTM = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-tm.png"))
        );
        ImageIcon iAreaBT = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-bt.png"))
        );
        ImageIcon iAreaA = new ImageIcon(
                Objects.requireNonNull(getClass()
                        .getResource("/images/zones-48-a.png"))
        );

        icons.add(iAreaA);
        icons.add(iAreaBT);
        icons.add(iAreaBM);
        icons.add(iAreaTM);
        icons.add(iAreaB);
        icons.add(iAreaT);
        icons.add(iAreaM);
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isBottom() {
        return bottom;
    }

    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    public boolean isMiddle() {
        return middle;
    }

    public void setMiddle(boolean middle) {
        this.middle = middle;
    }

    public ImageIcon getIcon(){
        if(top && bottom && middle) return icons.getFirst();    // 0 - A
        if(top && bottom) return icons.get(1);                  // 1 - BT
        if(bottom && middle) return icons.get(2);               // 2 - BM
        if(top && middle) return icons.get(3);                  // 3 - TM
        if(bottom) return icons.get(4);                         // 4 - B
        if(top) return icons.get(5);                            // 5 - T
        if(middle) return icons.get(6);                         // 6 - M
        return icons.getFirst();
    }

    public ImageIcon getIcon(boolean top, boolean bottom, boolean middle){
        if(top && bottom && middle) return icons.getFirst();    // 0 - A
        if(top && bottom) return icons.get(1);                  // 1 - BT
        if(bottom && middle) return icons.get(2);               // 2 - BM
        if(top && middle) return icons.get(3);                  // 3 - TM
        if(bottom) return icons.get(4);                         // 4 - B
        if(top) return icons.get(5);                            // 5 - T
        if(middle) return icons.get(6);                         // 6 - M
        return icons.getFirst();
    }
}
