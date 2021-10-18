package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.PlaylistActivity;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.AllVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.FolderAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.HistoryAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.PlayListAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.FragmentPlayListBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;

import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment.foldersBinding;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.HistoryFragment.historyBinding;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment.fragmentVideosBinding;

public class PlayListFragment extends Fragment {

    public static FragmentPlayListBinding fragmentPlayListBinding;
    protected PlayListAdapter playListAdapter;
    MyClickHandlers myClickHandlers;
    ArrayList<PlayList> playList = new ArrayList<>();
    DBHelper dbHelper;

    private MoreOptionReceiver moreOptionReceiver;

    private RefreshReceiver refreshReceiver;

    public PlayListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_play_list, container, false);
        myClickHandlers = new MyClickHandlers(getContext());
        fragmentPlayListBinding.setOnClick(myClickHandlers);

        moreOptionReceiver = new MoreOptionReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(moreOptionReceiver,
                new IntentFilter("MORE_OPERATION_PLAY"));

        refreshReceiver = new RefreshReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshReceiver,
                new IntentFilter("REFRESH_PLAYLIST"));

        dbHelper = new DBHelper(getContext());

        fragmentPlayListBinding.rvAllVideos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        if (!dbHelper.checkIfExistPlayList("My Favourite")) {
            dbHelper.insertPlayList("My Favourite");
        }

        return fragmentPlayListBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        playList.clear();
        playList.addAll(dbHelper.getPlayList());
        playListAdapter = new PlayListAdapter("Playlist screen", playList, getActivity());
        playListAdapter.addItemClickListener(new PlayListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                getActivity().runOnUiThread(() -> {
                    if (FolderAdapter.selectedList.size() > 0) {
                        FolderAdapter.selectedList.clear();
                        FolderAdapter.selectedPathList.clear();
                        FoldersFragment.folderAdapter.notifyDataSetChanged();
                        foldersBinding.rvFolders.getAdapter().notifyDataSetChanged();
                    } else if (AllVideoAdapter.selectedList.size() > 0) {
                        AllVideoAdapter.selectedList.clear();
                        AllVideoAdapter.selectedPathList.clear();
                        VideosFragment.allVideoAdapter.notifyDataSetChanged();
                        fragmentVideosBinding.rvAllVideos.getAdapter().notifyDataSetChanged();
                    } else if (HistoryAdapter.selectedList.size() > 0) {
                        HistoryAdapter.selectedList.clear();
                        HistoryAdapter.selectedPathList.clear();
                        HistoryFragment.historyAdapter.notifyDataSetChanged();
                        historyBinding.rvAllVideos.getAdapter().notifyDataSetChanged();
                    }
                });
                Intent intent = new Intent(getActivity(), PlaylistActivity.class);
                intent.putExtra("Position", position);
                startActivity(intent);
            }
        });
        fragmentPlayListBinding.rvAllVideos.setAdapter(playListAdapter);
    }

    private void createPlaylist() {
        final Dialog dial = new Dialog(getActivity(), android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_rename);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LottieAnimationView deleteAnim = dial.findViewById(R.id.deleteAnim);
        deleteAnim.setVisibility(View.GONE);

        LottieAnimationView renameAnim = dial.findViewById(R.id.renameAnim);
        renameAnim.setVisibility(View.VISIBLE);

        TextView tvTitleDel = dial.findViewById(R.id.tvTitleDel);
        tvTitleDel.setText("Create Playlist");

        TextView no = dial.findViewById(R.id.no);
        TextView yes = dial.findViewById(R.id.yes);

        no.setText("Cancel");
        yes.setText("Create");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setHint("Playlist Name");

        yes.setOnClickListener(view -> {
            if (!dbHelper.checkIfExistPlayList(etRename.getText().toString().trim())) {
                boolean isCreate = dbHelper.insertPlayList(etRename.getText().toString().trim());
                Log.e("LLL_Create: ", String.valueOf(isCreate));
                playList.clear();
                if (isCreate) {
                    playList.addAll(dbHelper.getPlayList());
                    getActivity().runOnUiThread(() -> playListAdapter.notifyDataSetChanged());
                }
            } else {
                Toasty.info(getActivity(), "Already exists in the playlist", Toasty.LENGTH_SHORT).show();
            }

            dial.dismiss();
        });
        no.setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    private void renameFile() {
        final Dialog dial = new Dialog(getActivity(), android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_rename);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LottieAnimationView deleteAnim = dial.findViewById(R.id.deleteAnim);
        deleteAnim.setVisibility(View.GONE);

        LottieAnimationView renameAnim = dial.findViewById(R.id.renameAnim);
        renameAnim.setVisibility(View.VISIBLE);

        TextView del = dial.findViewById(R.id.yes);

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setText(playList.get(PlayListAdapter.mPositionSelected).getP_name());

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            dbHelper.renamePlayList(playList.get(PlayListAdapter.mPositionSelected).getP_name(), etRename.getText().toString());
            onResume();
            dial.dismiss();
        });
        dial.findViewById(R.id.no).setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    private void propertiesSinglefile() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialoge_properties);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LinearLayout llLocation, llDate, llContains, llTotalSize;
        llDate = dial.findViewById(R.id.llDate);
        llLocation = dial.findViewById(R.id.llLocation);
        llContains = dial.findViewById(R.id.llContains);
        llTotalSize = dial.findViewById(R.id.llTotalSize);

        TextView tvFile = dial.findViewById(R.id.tvFile);
        TextView tvTitle = dial.findViewById(R.id.tvTitle);
        TextView del = dial.findViewById(R.id.yes);
        TextView tvPath = dial.findViewById(R.id.tvPath);
        TextView tvCreatedAt = dial.findViewById(R.id.tvCreatedAt);
        TextView tvSize = dial.findViewById(R.id.tvSize);
        TextView tvCount = dial.findViewById(R.id.tvVideoCounts);
        TextView tvFormat = dial.findViewById(R.id.tvFormat);
        TextView tvLength = dial.findViewById(R.id.tvLength);
        TextView tvResolution = dial.findViewById(R.id.tvResolution);

        LinearLayout llMedia = dial.findViewById(R.id.llMedia);

        PlayList baseModel = playList.get(PlayListAdapter.mPositionSelected);

        llLocation.setVisibility(View.VISIBLE);
        llContains.setVisibility(View.VISIBLE);
        llDate.setVisibility(View.VISIBLE);
        llMedia.setVisibility(View.GONE);
        tvFile.setVisibility(View.GONE);

        tvTitle.setText("Properties");
        llLocation.setVisibility(View.GONE);
        llDate.setVisibility(View.VISIBLE);
        llTotalSize.setVisibility(View.GONE);

        tvTitle.setText(baseModel.getP_name());

        Date theDate = new Date(Long.parseLong(baseModel.getDate()));
        SimpleDateFormat spf = new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa");
        String date = spf.format(theDate);

        tvCreatedAt.setText(date);
        int size = dbHelper.getPlayListItems(baseModel.getP_id()).size();
        if (size <= 1)
            tvCount.setText(size + " Video");
        else
            tvCount.setText(size + " Videos");

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
    }

    private void deleteFolderMore() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_rename);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LottieAnimationView deleteAnim = dial.findViewById(R.id.deleteAnim);
        deleteAnim.setVisibility(View.VISIBLE);

        LottieAnimationView renameAnim = dial.findViewById(R.id.renameAnim);
        renameAnim.setVisibility(View.GONE);

        TextView title = dial.findViewById(R.id.tvTitleDel);
        title.setText("Are you sure you want to delete the Playlist?");

        TextView del = dial.findViewById(R.id.yes);
        del.setText("Delete");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setVisibility(View.GONE);

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            dbHelper.deletePlayList(playList.get(PlayListAdapter.mPositionSelected).getP_id());
            onResume();
            dial.dismiss();
        });

        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
        });

        dial.show();
    }

    private class MoreOptionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str = intent.getStringExtra("option");
            if ("Rename".equals(str)) {
                renameFile();
            } else if ("Property".equals(str)) {
                propertiesSinglefile();
            } else if ("deleteFile".equals(str)) {
                deleteFolderMore();
            }
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().runOnUiThread(() -> onResume());
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onCreateList(View v) {
            createPlaylist();
        }
    }
}