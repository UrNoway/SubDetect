package net.unelement.sd.subtitle;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SrtSubtitles {

    private final List<SubtitleEvent> subtitleEvents;

    public SrtSubtitles(){
        subtitleEvents = new ArrayList<>();
    }

    public List<SubtitleEvent> getSubtitleEvents() {
        return subtitleEvents;
    }

    public void writeSRT(String path){
        try(PrintWriter pw = new PrintWriter(path, StandardCharsets.UTF_8)){
            for(int i=0; i<subtitleEvents.size(); i++){
                SubtitleEvent event = subtitleEvents.get(i);

                pw.println(++i);
                pw.printf("%s --> %s\n", getTime(event.getMicrosStart()), event.getMicrosEnd());
                pw.println(event.getText());
                pw.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTime(long microseconds){
        long msTime = microseconds % 1000;
        int hh = (int)(msTime / 3600000d);
        int mm = (int)((msTime - 3600000d * hh) / 60000d);
        int ss = (int)((msTime - 3600000d * hh - 60000d * mm) / 1000d);
        int ms = (int)(msTime - 3600000d * hh - 60000d * mm - 1000d * ss);

        return String.format("%02d:%02d:%02d,%03d", hh, mm, ss, ms);
    }
}
