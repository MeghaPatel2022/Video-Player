package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.AllVideoPlayer;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.MainActivity;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListAllVideosBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListRecentVideoBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    public static ArrayList<BaseModel> selectedList = new ArrayList<>();
    public static ArrayList<String> selectedPathList = new ArrayList<>();

    String from = "Recent";

    ArrayList<BaseModel> videoList = new ArrayList<>();
    Activity activity;
    DBHelper dbHelper;

    public HistoryAdapter(String from, Activity activity) {
        this.activity = activity;
        this.from = from;
        dbHelper = new DBHelper(activity);
        videoList.clear();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (from.equals("History")) {
            ListAllVideosBinding allVideosBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_all_videos, parent, false);
            viewHolder = new HistoryClassView(allVideosBinding);
        } else {
            ListRecentVideoBinding recentVideoBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_recent_video, parent, false);
            viewHolder = new RecentClassView(recentVideoBinding);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, int position) {

        if (from.equals("History")) {

            HistoryClassView holder = (HistoryClassView) holder1;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.allVideosBinding.imgThumb.setClipToOutline(true);
            }

            holder.allVideosBinding.imgMore.setVisibility(View.GONE);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoList.get(position).getBucketPath());
            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            holder.allVideosBinding.tvVidTime.setText(Util.generateTime(new File(videoList.get(position).getBucketPath())));

            retriever.release();

            Glide.with(activity)
                    .load(videoList.get(position).getBucketPath())
                    .into(holder.allVideosBinding.imgThumb);

            File file = new File(videoList.get(position).getBucketPath());

            holder.allVideosBinding.tvVidName.setText(videoList.get(position).getName());
            holder.allVideosBinding.tvSize.setText(Constant.getSize(file.length()));

            if (selectedPathList.size() > 0) {
                holder.allVideosBinding.imgSelect.setVisibility(View.VISIBLE);
                holder.allVideosBinding.imgSelect.setSelected(selectedPathList.contains(String.valueOf(position)));
            } else {
                holder.allVideosBinding.imgSelect.setVisibility(View.GONE);
                MainActivity.mainBinding.cvDeleteHis.setVisibility(View.GONE);
            }

            holder.allVideosBinding.getRoot().setOnClickListener(v -> {
                if (selectedPathList.size() > 0) {
                    if (holder.allVideosBinding.imgSelect.isSelected()) {
                        holder.allVideosBinding.imgSelect.setSelected(false);
                        if (selectedPathList.contains(String.valueOf(position))) {
                            selectedPathList.remove(videoList.get(position).getBucketPath());

                            for (int i = 0; i < selectedList.size(); i++) {
                                BaseModel baseModel = selectedList.get(i);
                                if (baseModel.getBucketPath().equals(videoList.get(position).getBucketPath())) {
                                    selectedList.remove(i);
                                    break;
                                }
                            }

                            if (selectedPathList.size() <= 0) {
                                holder.allVideosBinding.imgSelect.setVisibility(View.GONE);
                                notifyDataSetChanged();
                            }
                        }
                    } else {
                        selectedPathList.add(String.valueOf(position));
                        selectedList.add(videoList.get(position));
                        holder.allVideosBinding.imgSelect.setSelected(true);
                    }
                    MainActivity.mainBinding.cvDeleteHis.setVisibility(View.VISIBLE);
                    MainActivity.mainBinding.tvDelCount.setText(selectedPathList.size() + " Selected");
                } else {
                    Constant.backFrom = "History";
                    AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                    allVideoPlayer.addAll(videoList);
                    Intent intent = new Intent(activity, AllVideoPlayer.class);
                    intent.putExtra("position", position);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
            });

            holder.allVideosBinding.getRoot().setOnLongClickListener(v -> {
                if (selectedPathList.size() > 0) {
                    return false;
                } else {
                    selectedPathList.add(String.valueOf(position));
                    selectedList.add(videoList.get(position));

                    MainActivity.mainBinding.cvDeleteHis.setVisibility(View.VISIBLE);
                    MainActivity.mainBinding.tvDelCount.setText(selectedPathList.size() + " Selected");

                    holder.allVideosBinding.imgSelect.setVisibility(View.VISIBLE);
                    holder.allVideosBinding.imgSelect.setSelected(!holder.allVideosBinding.imgSelect.isSelected());
                    notifyDataSetChanged();
                }
                return true;
            });

        } else {
            RecentClassView holder = (RecentClassView) holder1;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.recentVideoBinding.imgVideoThumb.setClipToOutline(true);
            }

            Glide.with(activity)
                    .load(videoList.get(position).getBucketPath())
                    .into(holder.recentVideoBinding.imgVideoThumb);

            holder.recentVideoBinding.tvVidName.setText(videoList.get(position).getName());

            holder.recentVideoBinding.getRoot().setOnClickListener(v -> {
                Constant.backFrom = "Recent";
                AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                allVideoPlayer.addAll(videoList);
                Intent intent = new Intent(activity, AllVideoPlayer.class);
                intent.putExtra("position", position);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            });
        }
    }

    public void addAll(ArrayList<BaseModel> videoListAll) {
        videoList.clear();
        videoList.addAll(videoListAll);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (from.equals("History"))
            size = videoList.size();
        else
            size = Math.min(videoList.size(), 10);
        return size;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class HistoryClassView extends RecyclerView.ViewHolder {

        private final ListAllVideosBinding allVideosBinding;

        public HistoryClassView(ListAllVideosBinding allVideosBinding) {
            super(allVideosBinding.getRoot());
            this.allVideosBinding = allVideosBinding;
        }
    }

    public class RecentClassView extends RecyclerView.ViewHolder {

        private final ListRecentVideoBinding recentVideoBinding;

        public RecentClassView(ListRecentVideoBinding recentVideoBinding) {
            super(recentVideoBinding.getRoot());
            this.recentVideoBinding = recentVideoBinding;
        }
    }
}
