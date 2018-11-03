package chn.wu.jianhui.speed_detection.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chn.wu.jianhui.speed_detection.entity.TrackEntity;

public class SqlUtils extends SQLiteOpenHelper
{
    public SqlUtils(Context context)
    {
        super(context, "chn.jianhui.speed_detection.db", null, 2);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE track(track_id INTEGER PRIMARY KEY AUTOINCREMENT, track_data TEXT, start_time TIMESTAMP, end_time TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertTrack(String track_data, Date start_time, Date end_time)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("track_data", track_data);
        values.put("start_time", start_time.getTime());
        values.put("end_time", end_time.getTime());
        long res = db.insert("track", null, values);
        db.close();
        return res;
    }

    public long deleteTrack(int track_id)
    {
        SQLiteDatabase db = getWritableDatabase();
        long res = db.delete("track", "track_id=?", new String[]{track_id+""});
        db.close();
        return res;
    }

    public TrackEntity selectById(int track_id)
    {
        SQLiteDatabase db = getWritableDatabase();
        TrackEntity trackEntity = null;
        Cursor cursor = db.rawQuery("select * from track where track_id=?", new String[]{track_id+""});
        if (cursor.moveToNext())
        {
            trackEntity = new TrackEntity(cursor.getInt(0), cursor.getString(1), new Date(Long.valueOf(cursor.getString(2))),new Date(Long.valueOf(cursor.getString(3))));
        }
        cursor.close();
        db.close();
        return trackEntity;
    }

    public List<TrackEntity> selectAll()
    {
        SQLiteDatabase db = getWritableDatabase();
        List<TrackEntity> trackEntity = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from track", new String[]{});
        while(cursor.moveToNext())
        {
            trackEntity.add(new TrackEntity(cursor.getInt(0), cursor.getString(1), new Date(Long.valueOf(cursor.getString(2))),new Date(Long.valueOf(cursor.getString(3)))));
        }
        return trackEntity;
    }
}

