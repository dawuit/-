package chn.wu.jianhui.speed_detection.entity;

import java.util.Date;

public class TrackEntity
{
    private Integer track_id;
    //JSON数据
    private String track_data;
    private Date start_time;
    private Date end_time;


    public TrackEntity(Integer track_id, String track_data, Date start_time, Date end_time) {
        this.track_id = track_id;
        this.track_data = track_data;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public TrackEntity() {
    }

    public Integer getTrack_id() {
        return track_id;
    }

    public void setTrack_id(Integer track_id) {
        this.track_id = track_id;
    }

    public String getTrack_data() {
        return track_data;
    }

    public void setTrack_data(String track_data) {
        this.track_data = track_data;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }
}
