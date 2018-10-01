package tutorial.android.bkav.com.facebookclone;

import android.os.Message;

/**
 * Created by PHONG on 3/31/2018.
 */

public class Messages {

    private String from;
    private String message;
    private boolean seen;
    private long time;
    private String type;

    public Messages() {

    }

    public Messages(String from, String messgage, boolean seen, long time, String type) {
        this.from = from;
        this.message = messgage;
        this.seen = seen;
        this.time = time;
        this.type = type;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String messgage) {
        this.message = messgage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
