package tutorial.android.bkav.com.facebookclone;

/**
 * Created by PHONG on 3/28/2018.
 */

public class Posts {
    public String uid;
    public String time;
    public String date;
    public String postimage;
    public String description;
    public String user_image;
    public String user_name;

    public Posts() {
    }

    public Posts(String uid, String time, String date, String postimage, String description, String user_image, String user_name) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.user_name = user_name;
        this.user_image = user_image;
    }


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

}
