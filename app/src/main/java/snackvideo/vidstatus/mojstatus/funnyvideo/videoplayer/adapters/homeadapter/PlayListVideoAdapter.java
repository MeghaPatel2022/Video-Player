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
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.PlaylistActivity;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListAllVideosBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

public class PlayListVideoAdapter extends RecyclerView.Adapter<PlayListVideoAdapter.MyClassView> implements Filterable {

    public static ArrayList<BaseModel> selectedList = new ArrayList<>();
    public static ArrayList<String> selectedPathList = new ArrayList<>();
    public static int mPositionSelected = 0;
    ArrayList<BaseModel> videoList;
    ArrayList<BaseModel> filterList;
    Activity activity;
    DBHelper dbHelper;
    private MoreClickListener moreClickListener;

    public PlayListVideoAdapter(ArrayList<BaseModel> videoList, Activity activity) {
        this.videoList = videoList;
        this.filterList = videoList;
        this.activity = activity;
        dbHelper = new DBHelper(activity);
    }

    public void addItemClickListener(MoreClickListener listener) {
        moreClickListener = listener;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListAllVideosBinding allVideosBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_all_videos, parent, false);
        return new MyClassView(allVideosBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListVideoAdapter.MyClassView holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.allVideosBinding.imgThumb.setClipToOutline(true);
        }

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
            PlaylistActivity.playlistBinding.imgSearch.setVisibility(View.GONE);
            PlaylistActivity.playlistBinding.imgMore.setVisibility(View.VISIBLE);

            holder.allVideosBinding.imgSelect.setVisibility(View.VISIBLE);
            holder.allVideosBinding.imgMore.setVisibility(View.GONE);
            holder.allVideosBinding.imgSelect.setSelected(selectedPathList.contains(String.valueOf(position)));
        } else {
            if (FolderAdapter.selectedPathList.size() > 0) {
                PlaylistActivity.playlistBinding.imgSearch.setVisibility(View.GONE);
                PlaylistActivity.playlistBinding.imgMore.setVisibility(View.VISIBLE);
            } else {
                PlaylistActivity.playlistBinding.imgSearch.setVisibility(View.VISIBLE);
                PlaylistActivity.playlistBinding.imgMore.setVisibility(View.GONE);
            }

            holder.allVideosBinding.imgSelect.setVisibility(View.GONE);
            holder.allVideosBinding.imgMore.setVisibility(View.VISIBLE);
        }

        holder.allVideosBinding.getRoot().setOnClickListener(v -> {
            if (selectedPathList.size() > 0) {
                if (holder.allVideosBinding.imgSelect.isSelected()) {
                    holder.allVideosBinding.imgSelect.setSelected(false);
                    if (selectedPathList.contains(String.valueOf(position))) {
                        selectedPathList.remove(String.valueOf(position));
                        selectedList.remove(videoList.get(position));
                        if (selectedPathList.size() <= 0) {
                            holder.allVideosBinding.imgSelect.setVisibility(View.GONE);
                            holder.allVideosBinding.imgMore.setVisibility(View.VISIBLE);
                            notifyDataSetChanged();
                        }
                    }
                } else {
                    FolderAdapter.selectedList.clear();
                    FolderAdapter.selectedPathList.clear();
                    activity.runOnUiThread(() -> {
                        FoldersFragment.folderAdapter.notifyDataSetChanged();
                    });

                    selectedPathList.add(String.valueOf(position));
                    selectedList.add(videoList.get(position));
                    holder.allVideosBinding.imgSelect.setSelected(true);
                    notifyDataSetChanged();
                }

            } else {
                AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                allVideoPlayer.addAll(videoList);
                Constant.backFrom = "Video";
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
                FolderAdapter.selectedList.clear();
                FolderAdapter.selectedPathList.clear();
                activity.runOnUiThread(() -> {
                    FoldersFragment.folderAdapter.notifyDataSetChanged();
                    notifyDataSetChanged();
                });

                selectedPathList.add(String.valueOf(position));
                selectedList.add(videoList.get(position));
                holder.allVideosBinding.imgMore.setVisibility(View.GONE);
                holder.allVideosBinding.imgSelect.setVisibility(View.VISIBLE);
                holder.allVideosBinding.imgSelect.setSelected(!holder.allVideosBinding.imgSelect.isSelected());
                notifyDataSetChanged();
            }
            return true;
        });

        holder.allVideosBinding.imgMore.setOnClickListener(v -> {
            mPositionSelected = position;
            if (moreClickListener != null) {
                moreClickListener.onItemClick(position);
            }
        });
    }

    public void removeItem(int position) {
        dbHelper.removePlayListItems(videoList.get(position).getP_id(), videoList.get(position).getBucketPath());
        videoList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void removeItem1(int position) {
        dbHelper.removePlayListItems(videoList.get(position).getP_id(), videoList.get(position).getBucketPath());
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    videoList = filterList;
                } else {
                    ArrayList<BaseModel> filteredList = new ArrayList<>();
                    for (BaseModel row : videoList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        String foldername = row.getName().substring(row.getName().lastIndexOf("/") + 1);
                        if (foldername.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    videoList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = videoList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                videoList = (ArrayList<BaseModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    //Define your Interface method here
    public interface MoreClickListener {
        void onItemClick(int position);
    }

    public class MyClassView extends RecyclerView.ViewHolder {
        private final ListAllVideosBinding allVideosBinding;

        public MyClassView(ListAllVideosBinding allVideosBinding) {
            super(allVideosBinding.getRoot());
            this.allVideosBinding = allVideosBinding;
        }
    }
}
