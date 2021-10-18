package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.AlbumActivity;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListFoldersBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;

import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.MainActivity.mainBinding;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyClassView> implements Filterable {

    public static ArrayList<BaseModel> selectedList = new ArrayList<>();
    public static ArrayList<String> selectedPathList = new ArrayList<>();
    ArrayList<BaseModel> videoFolders;
    ArrayList<BaseModel> filterList;
    Activity activity;

    public FolderAdapter(ArrayList<BaseModel> videoFolders, Activity activity) {
        this.activity = activity;
        this.videoFolders = videoFolders;
        this.filterList = videoFolders;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListFoldersBinding foldersBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_folders, parent, false);
        return new MyClassView(foldersBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.MyClassView holder, int position) {
        holder.foldersBinding.tvAlbumName.setText(videoFolders.get(position).getBucketName());
        holder.foldersBinding.tvAlbumSize.setText(videoFolders.get(position).getSize() + " Videos");

        if (selectedPathList.size() > 0) {
            mainBinding.imgSearch.setVisibility(View.GONE);
            mainBinding.imgMore.setVisibility(View.VISIBLE);

            holder.foldersBinding.imgFolder.setSelected(selectedPathList.contains(String.valueOf(position)));
        } else {
            mainBinding.imgSearch.setVisibility(View.VISIBLE);
            mainBinding.imgMore.setVisibility(View.GONE);

            holder.foldersBinding.imgFolder.setSelected(false);
        }

        holder.foldersBinding.getRoot().setOnClickListener(v -> {
            if (selectedPathList.size() > 0) {
                if (holder.foldersBinding.imgFolder.isSelected()) {
                    holder.foldersBinding.imgFolder.setSelected(false);
                    if (selectedPathList.contains(String.valueOf(position))) {
                        selectedPathList.remove(String.valueOf(position));
                        for (int i = 0; i < selectedList.size(); i++) {
                            BaseModel baseModel = selectedList.get(i);
                            if (baseModel.getBucketId().equals(videoFolders.get(position).getBucketId())) {
                                selectedList.remove(i);
                                break;
                            }
                        }

                        if (selectedPathList.size() <= 0) {
                            holder.foldersBinding.imgFolder.setVisibility(View.GONE);
                            holder.foldersBinding.imgFolder.setVisibility(View.VISIBLE);
                            notifyDataSetChanged();
                        }
                    }
                } else {
                    AllVideoAdapter.selectedPathList.clear();
                    AllVideoAdapter.selectedList.clear();
                    activity.runOnUiThread(() -> {
                        VideosFragment.allVideoAdapter.notifyDataSetChanged();
                        notifyDataSetChanged();
                    });

                    selectedPathList.add(String.valueOf(position));
                    selectedList.add(videoFolders.get(position));
                    holder.foldersBinding.imgFolder.setSelected(true);
                }
                activity.runOnUiThread(() -> {
                    if (FolderAdapter.selectedList.size() > 0) {
                        mainBinding.llRename.setVisibility(View.GONE);
                    } else {
                        mainBinding.llRename.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                Intent intent = new Intent(activity, AlbumActivity.class);
                intent.putExtra("bucketId", videoFolders.get(position).getBucketId());
                activity.startActivity(intent);
            }
        });

        holder.foldersBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (selectedPathList.size() > 0) {
                    return false;
                } else {
                    AllVideoAdapter.selectedPathList.clear();
                    AllVideoAdapter.selectedList.clear();
                    activity.runOnUiThread(() -> VideosFragment.allVideoAdapter.notifyDataSetChanged());

                    selectedPathList.add(String.valueOf(position));
                    selectedList.add(videoFolders.get(position));

                    activity.runOnUiThread(() -> {
                        if (FolderAdapter.selectedList.size() > 1) {
                            mainBinding.llRename.setVisibility(View.GONE);
                        } else {
                            mainBinding.llRename.setVisibility(View.VISIBLE);
                        }
                    });

                    holder.foldersBinding.imgFolder.setSelected(!holder.foldersBinding.imgFolder.isSelected());
                    notifyDataSetChanged();
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoFolders.size();
    }

    public void addAll(ArrayList<BaseModel> videoFoldersAll) {
        videoFolders.clear();
        videoFolders.addAll(videoFoldersAll);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    videoFolders = filterList;
                } else {
                    ArrayList<BaseModel> filteredList = new ArrayList<>();
                    for (BaseModel row : videoFolders) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        String foldername = row.getBucketName().substring(row.getBucketName().lastIndexOf("/") + 1);
                        if (foldername.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    videoFolders = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = videoFolders;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                videoFolders = (ArrayList<BaseModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyClassView extends RecyclerView.ViewHolder {
        private final ListFoldersBinding foldersBinding;

        public MyClassView(ListFoldersBinding foldersBinding) {
            super(foldersBinding.getRoot());
            this.foldersBinding = foldersBinding;
        }
    }
}
