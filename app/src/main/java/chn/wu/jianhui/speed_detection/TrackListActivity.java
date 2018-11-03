package chn.wu.jianhui.speed_detection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import chn.wu.jianhui.speed_detection.entity.TrackEntity;
import chn.wu.jianhui.speed_detection.utils.SqlUtils;

public class TrackListActivity extends Activity
{
    private List<TrackEntity> trackEntities;
    private MyBaseAdapter mAdapter = new MyBaseAdapter();
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_viewlist);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        listView = findViewById(R.id.track_viewlist);
        //获取数据
        trackEntities = new SqlUtils(getApplicationContext()).selectAll();
        if (trackEntities.size() == 0)
            findViewById(R.id.track_nodata).setVisibility(View.VISIBLE);
       else
        {
            findViewById(R.id.track_nodata).setVisibility(View.GONE);
            listView.setAdapter(mAdapter);
        }

    }

    class MyBaseAdapter extends BaseAdapter implements View.OnClickListener
    {
        private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private SimpleDateFormat fmt_s = new SimpleDateFormat("HH:mm:ss");
        //得到item的总数
        @Override
        public int getCount() {
            //返回ListView Item条目的总数
            return trackEntities.size();
        }
        //得到Item代表的对象
        @Override
        public Object getItem(int position) {
            //返回ListView Item条目代表的对象
            return trackEntities.get(position);
        }
        //得到Item的id
        @Override
        public long getItemId(int position) {
            //返回ListView Item的id
            return position;
        }
        //得到Item的View视图
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.track_viewitem, parent,false);
                holder = new ViewHolder();
                holder.trackitem_id = (TextView)convertView.findViewById(R.id.trackitem_id);
                holder.trackitem_name = (TextView) convertView.findViewById(R.id.trackitem_name);
                holder.trackitem_time = (TextView) convertView.findViewById(R.id.trackitem_time);
                holder.trackitem_info = (Button) convertView.findViewById(R.id.tracklist_detail);
                holder.trackitem_delete = (Button) convertView.findViewById(R.id.tracklist_delete);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.trackitem_id.setText((position + 1) + "");
            holder.trackitem_name.setText(fmt.format(trackEntities.get(position).getStart_time()));
            holder.trackitem_time.setText("持续时间："+ fmt_s.format(trackEntities.get(position).getEnd_time().compareTo(trackEntities.get(position).getStart_time()) - 8*1000*60*60));
            holder.trackitem_delete.setTag(position);
            holder.trackitem_delete.setOnClickListener(this);
            holder.trackitem_info.setTag(position);
            holder.trackitem_info.setOnClickListener(this);
            return convertView;
        }

        class ViewHolder
        {
            private TextView trackitem_id;
            private TextView trackitem_name;
            private TextView trackitem_time;
            private Button trackitem_info;
            private Button trackitem_delete;
        }

        @Override
        public void onClick(View v) {
            int tag = (int)v.getTag();
            switch (v.getId()){
                case R.id.tracklist_detail:
                    Intent intentmap = new Intent(TrackListActivity.this, MapActivity.class);
                    intentmap.putExtra("trackData", trackEntities.get(tag).getTrack_data());
                    intentmap.putExtra("start_time", trackEntities.get(tag).getStart_time());
                    intentmap.putExtra("end_time", trackEntities.get(tag).getEnd_time());
                    startActivity(intentmap);
                    break;
                case R.id.tracklist_delete:
                    if(new SqlUtils(getApplicationContext()).deleteTrack(trackEntities.get(tag).getTrack_id()) != 0)
                    {
                        Toast.makeText(TrackListActivity.this, "删除成功", Toast.LENGTH_SHORT);
                        trackEntities = new SqlUtils(getApplicationContext()).selectAll();
                        TrackListActivity.this.listView.setAdapter(TrackListActivity.this.mAdapter);
                    }
                    else
                        Toast.makeText(TrackListActivity.this, "删除失败", Toast.LENGTH_SHORT);
                    break;
            }

        }
    }
}
