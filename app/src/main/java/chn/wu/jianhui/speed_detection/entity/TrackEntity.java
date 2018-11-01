package chn.wu.jianhui.speed_detection.entity;


public class TrackEntity
{
    private Integer track_id;
    private String create_time;

    public Integer getTrack_id() {
        return track_id;
    }

    public void setTrack_id(Integer track_id) {
        this.track_id = track_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public TrackEntity(Integer track_id, String create_time) {
        this.track_id = track_id;
        this.create_time = create_time;
    }

    public TrackEntity() {
    }
}
