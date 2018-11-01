package chn.wu.jianhui.speed_detection.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import chn.wu.jianhui.speed_detection.entity.TrackEntity;

public class SqlUtils extends SQLiteOpenHelper
{
    public SqlUtils(Context context) {
        super(context, "speed_detection", null, 2);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE track (track_id INTEGER PRIMARY KEY, create_time TIMESTAMP DEFAULT (datetime('now','localtime')))");
    }

    public List<TrackEntity> selectAllTrack()
    {
         List<TrackEntity> appEntities = new ArrayList<>();
         SQLiteDatabase db = getReadableDatabase();
         Cursor cursor = db.query("track", null,null,null,null,null,null);
         while(cursor.moveToNext())
         {
             appEntities.add(new TrackEntity(cursor.getInt(0), cursor.getString(1)));
         }
         db.close();
         return appEntities;
    }

    public long insertTrack(int trackt_id, String create_time)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("track_id", trackt_id);
        contentValues.put("create_time", create_time);
        long res = db.insert("track", null, contentValues);
        db.close();
        return res;
    }

    public long deleteTrack(int trackt_id)
    {
        SQLiteDatabase db = getReadableDatabase();
        long res = db.delete("track", "track_id=?", new String[]{trackt_id+""});
        db.close();
        return res;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
