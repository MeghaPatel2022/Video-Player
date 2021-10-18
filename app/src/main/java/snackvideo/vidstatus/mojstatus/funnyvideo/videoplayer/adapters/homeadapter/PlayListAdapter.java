package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListPlaylistBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MyClassView> {

    public static int mPositionSelected = 0;
    ArrayList<PlayList> playList;
    Activity activity;
    String from;
    DBHelper dbHelper;
    private ItemClickListener mItemClickListener;

    public PlayListAdapter(String from, ArrayList<PlayList> playList, Activity activity) {
        this.from = from;
        this.playList = playList;
        this.activity = activity;
        dbHelper = new DBHelper(activity);
    }

    public void addItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListPlaylistBinding playlistBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_playlist, parent, false);
        return new MyClassView(playlistBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.playlistBinding.imgThumb.setClipToOutline(true);
        }

        PlayList playList1 = playList.get(position);
        ArrayList<BaseModel> playListItems = dbHelper.getPlayListItems(playList1.getP_id());

        if (playList1.getP_name().equals("My Favourite")) {
            Glide
                    .with(activity)
                    .load(R.drawable.ic_my_favourite)
                    .into(holder.playlistBinding.imgThumb);
            holder.playlistBinding.imgMore.setVisibility(View.GONE);
        } else {
            if (from.equals("Playlist screen")) {
                holder.playlistBinding.imgMore.setVisibility(View.VISIBLE);
            } else {
                holder.playlistBinding.imgMore.setVisibility(View.GONE);
            }
            if (playListItems.size() > 0) {
                Glide
                        .with(activity)
                        .load(playListItems.get(0).getBucketPath())
                        .into(holder.playlistBinding.imgThumb);
            } else {
                Glide
                        .with(activity)
                        .load(R.drawable.ic_heart_white)
                        .into(holder.playlistBinding.imgThumb);
            }
        }

        if (playList.size() >= 1)
            holder.playlistBinding.tvVidCount.setText(playListItems.size() + " Videos");
        else
            holder.playlistBinding.tvVidCount.setText(playListItems.size() + " Video");

        holder.playlistBinding.tvPlaylistName.setText(playList1.getP_name());

        holder.playlistBinding.getRoot().setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(position);
            }
        });

        holder.playlistBinding.imgMore.setOnClickListener(v -> {
            mPositionSelected = position;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(activity);
            Intent localIn;
            localIn = new Intent("MORE_OPTION");
            localIn.putExtra("fileName", playList.get(position).getP_name());
            localIn.putExtra("isFrom", "PlayList");
            lbm.sendBroadcast(localIn);
        });

    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    //Define your Interface method here
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public class MyClassView extends RecyclerView.ViewHolder {
        private final ListPlaylistBinding playlistBinding;

        public MyClassView(ListPlaylistBinding playlistBinding) {
            super(playlistBinding.getRoot());
            this.playlistBinding = playlistBinding;
        }
    }
}
