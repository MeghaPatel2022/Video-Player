package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter;

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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.AllVideoPlayer;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListAllVideosBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.AlbumActivity.albumBinding;

public class AlbumVideoAdapter extends RecyclerView.Adapter<AlbumVideoAdapter.MyClassView> implements Filterable {

    public static ArrayList<BaseModel> selectedList = new ArrayList<>();
    public static ArrayList<String> selectedPathList = new ArrayList<>();
    public static int mPositionSelected = 0;
    ArrayList<BaseModel> videoList;
    ArrayList<BaseModel> filterList;
    FragmentActivity activity;
    private MoreClickListener moreClickListener;

    public AlbumVideoAdapter(ArrayList<BaseModel> videoList, FragmentActivity activity) {
        this.videoList = videoList;
        this.filterList = videoList;
        this.activity = activity;
        mPositionSelected = 0;
        videoList.clear();
    }

    public void addItemClickListener(MoreClickListener listener) {
        moreClickListener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ListAllVideosBinding allVideosBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_all_videos, parent, false);
        return new MyClassView(allVideosBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AlbumVideoAdapter.MyClassView holder, int position) {
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
            albumBinding.imgSearch.setVisibility(View.GONE);
            albumBinding.imgMore.setVisibility(View.VISIBLE);

            holder.allVideosBinding.imgSelect.setVisibility(View.VISIBLE);
            holder.allVideosBinding.imgMore.setVisibility(View.GONE);
            holder.allVideosBinding.imgSelect.setSelected(selectedPathList.contains(String.valueOf(position)));
        } else {
            if (FolderAdapter.selectedPathList.size() > 0) {
                holder.allVideosBinding.imgSelect.setVisibility(View.GONE);
                albumBinding.imgSearch.setVisibility(View.GONE);
                albumBinding.imgMore.setVisibility(View.VISIBLE);
            } else {
                albumBinding.imgSearch.setVisibility(View.VISIBLE);
                albumBinding.imgMore.setVisibility(View.GONE);
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
                    activity.runOnUiThread(() -> FoldersFragment.folderAdapter.notifyDataSetChanged());

                    selectedPathList.add(String.valueOf(position));
                    selectedList.add(videoList.get(position));

                    holder.allVideosBinding.imgSelect.setSelected(true);
                    notifyDataSetChanged();
                }
                activity.runOnUiThread(() -> {
                    if (AlbumVideoAdapter.selectedPathList.size() > 1) {
                        albumBinding.llRename.setVisibility(View.GONE);
                    } else {
                        albumBinding.llRename.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                if (!Util.isAdded) {
                    AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                    allVideoPlayer.addAll(videoList);
                    Constant.backFrom = "Video";
                    Intent intent = new Intent(activity, AllVideoPlayer.class);
                    intent.putExtra("position", position);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                } else {
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
                        albumBinding.imgSearch.setVisibility(View.GONE);
                        FolderAdapter.selectedList.clear();
                        FolderAdapter.selectedPathList.clear();
                        activity.runOnUiThread(() -> FoldersFragment.folderAdapter.notifyDataSetChanged());

                        selectedPathList.add(String.valueOf(position));
                        selectedList.add(videoList.get(position));
                        holder.allVideosBinding.imgSelect.setSelected(true);
                        notifyDataSetChanged();
                    }
                }
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

                albumBinding.imgSearch.setVisibility(View.GONE);
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
        videoList.remove(position);
        filterList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void clear() {
        videoList = new ArrayList<>();
        filterList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void add(int i, BaseModel videoRole) {
        videoList.add(videoRole);
        filterList.add(videoRole);
        notifyItemChanged(i);
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
