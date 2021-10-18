package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.AllVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.FolderAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.PlayListVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ActivityPlaylistBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment.fragmentVideosBinding;

public class PlaylistActivity extends AppCompatActivity {

    public static ActivityPlaylistBinding playlistBinding;
    protected int position = 0;
    protected ArrayList<PlayList> playLists = new ArrayList<>();
    protected ArrayList<BaseModel> playItemsList = new ArrayList<>();
    MyClickHandlers myClickHandlers;
    ArrayList<PlayList> playList = new ArrayList<>();
    DBHelper dbHelper;
    BottomSheetBehavior moreVideoOptionBehaviour;
    private PlayListVideoAdapter playListVideoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistBinding = DataBindingUtil.setContentView(PlaylistActivity.this, R.layout.activity_playlist);
        myClickHandlers = new MyClickHandlers(PlaylistActivity.this);
        playlistBinding.setOnClick(myClickHandlers);

        position = getIntent().getIntExtra("Position", 0);
        dbHelper = new DBHelper(PlaylistActivity.this);

        playLists = dbHelper.getPlayList();
        playItemsList = dbHelper.getPlayListItems(playLists.get(position).getP_id());

        if (playItemsList.size() > 0) {
            playlistBinding.layoutMain.setVisibility(View.VISIBLE);
            playlistBinding.rlNoData.setVisibility(View.GONE);
        } else {
            playlistBinding.layoutMain.setVisibility(View.GONE);
            playlistBinding.rlNoData.setVisibility(View.VISIBLE);
        }

        playlistBinding.tvTitle.setText(playLists.get(position).getP_name());
        if (playItemsList.size() <= 1)
            playlistBinding.tvVideoCounts.setText(playItemsList.size() + " video");
        else
            playlistBinding.tvVideoCounts.setText(playItemsList.size() + " videos");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PlaylistActivity.this, RecyclerView.VERTICAL, false);
        playlistBinding.rvAllVideos.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(PlaylistActivity.this,
                linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.line_divider);
        dividerItemDecoration.setDrawable(verticalDivider);
        fragmentVideosBinding.rvAllVideos.addItemDecoration(dividerItemDecoration);

        playListVideoAdapter = new PlayListVideoAdapter(playItemsList, PlaylistActivity.this);
        playlistBinding.rvAllVideos.setAdapter(playListVideoAdapter);

        playListVideoAdapter.addItemClickListener(position -> {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        playlistBinding.imgPlay.setOnClickListener(v -> {
            playItemsList.clear();
            playItemsList = dbHelper.getPlayListItems(playLists.get(position).getP_id());
            if (playItemsList.size() > 0) {
                AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                allVideoPlayer.addAll(playItemsList);
                Constant.backFrom = "Playlist";
                Intent intent = new Intent(PlaylistActivity.this, AllVideoPlayer.class);
                intent.putExtra("position", 0);
                startActivity(intent);
            } else {
                Toasty.info(PlaylistActivity.this, "There is no video in this playlist.", Toasty.LENGTH_SHORT).show();
            }
        });

        moreVideoOptionBehaviour = BottomSheetBehavior.from(playlistBinding.llMorePopup);

        EditText searchEditText = playlistBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));

        Typeface myFont = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myFont = getResources().getFont(R.font.segoeui);
            searchEditText.setTypeface(myFont);
        }

        int colorInt = getResources().getColor(R.color.white);
        ColorStateList csl = ColorStateList.valueOf(colorInt);

        ImageView searchIcon = playlistBinding.searchFile.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchIcon.setImageTintList(csl);
        }

        playlistBinding.imgSearch.setOnClickListener(v -> {
            if (playlistBinding.rlSearch.getVisibility() == View.VISIBLE) {
                Util.collapse(playlistBinding.rlSearch);
            } else {
                Util.expand(playlistBinding.rlSearch);
            }
        });

        playlistBinding.searchFile.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                playListVideoAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                playListVideoAdapter.getFilter().filter(query);
                return true;
            }
        });

        playlistBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }


    @Override
    public void onBackPressed() {
        if (PlayListVideoAdapter.selectedList.size() > 0) {
            PlayListVideoAdapter.selectedList.clear();
            PlayListVideoAdapter.selectedPathList.clear();
            playListVideoAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    private void propertySingleFile() {
        final Dialog dial = new Dialog(PlaylistActivity.this, android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialoge_properties);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LinearLayout llLocation, llDate, llContains;
        llDate = dial.findViewById(R.id.llDate);
        llLocation = dial.findViewById(R.id.llLocation);
        llContains = dial.findViewById(R.id.llContains);

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

        llLocation.setVisibility(View.VISIBLE);
        llContains.setVisibility(View.GONE);
        llDate.setVisibility(View.VISIBLE);
        llMedia.setVisibility(View.VISIBLE);
        tvFile.setVisibility(View.VISIBLE);

        BaseModel baseModel = playItemsList.get(PlayListVideoAdapter.mPositionSelected);

        tvTitle.setText(baseModel.getName());

        String extension = FilenameUtils.getExtension(baseModel.getBucketPath());
        tvFormat.setText(extension);
        Log.e("LLL_Extension: ", extension);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(baseModel.getBucketPath());
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        tvLength.setText(Util.generateTime(new File(baseModel.getBucketPath())));

        tvResolution.setText(width + " * " + height);

        retriever.release();

        tvPath.setText(baseModel.getBucketPath());

        File file = new File(baseModel.getBucketPath());
        Date theDate = new Date(file.lastModified());
        SimpleDateFormat spf = new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa");
        String date = spf.format(theDate);

        tvCreatedAt.setText(date);

        tvSize.setText(Util.getFileSize(file.length()));

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
    }

    private void propertiesFolder() {
        final Dialog dial = new Dialog(PlaylistActivity.this, android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialoge_properties);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LinearLayout llLocation, llDate, llContains;
        llDate = dial.findViewById(R.id.llDate);
        llLocation = dial.findViewById(R.id.llLocation);
        llContains = dial.findViewById(R.id.llContains);

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

        llMedia.setVisibility(View.GONE);
        tvFile.setVisibility(View.GONE);

        if (PlayListVideoAdapter.selectedList.size() == 1) {

            llLocation.setVisibility(View.VISIBLE);
            llContains.setVisibility(View.GONE);
            llDate.setVisibility(View.VISIBLE);
            llMedia.setVisibility(View.VISIBLE);
            tvFile.setVisibility(View.VISIBLE);

            BaseModel baseModel = PlayListVideoAdapter.selectedList.get(0);

            tvTitle.setText(baseModel.getName());

            String extension = FilenameUtils.getExtension(baseModel.getBucketPath());
            tvFormat.setText(extension);
            Log.e("LLL_Extension: ", extension);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(baseModel.getBucketPath());
            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

            tvLength.setText(Util.generateTime(new File(baseModel.getBucketPath())));

            tvResolution.setText(width + " * " + height);

            retriever.release();

            tvPath.setText(baseModel.getBucketPath());

            File file = new File(baseModel.getBucketPath());
            Date theDate = new Date(file.lastModified());
            SimpleDateFormat spf = new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa");
            String date = spf.format(theDate);

            tvCreatedAt.setText(date);

            tvSize.setText(Util.getFileSize(file.length()));

        } else {
            tvTitle.setText("Properties");
            llLocation.setVisibility(View.GONE);
            llDate.setVisibility(View.GONE);
            long count = 0;
            for (int j = 0; j < PlayListVideoAdapter.selectedList.size(); j++) {
                File file1 = new File(PlayListVideoAdapter.selectedList.get(j).getBucketPath());
                count += file1.length();
            }
            tvSize.setText(Constant.getSize(count));
            tvCount.setText(PlayListVideoAdapter.selectedList.size() + " Videos ");
        }

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void addVideoToList(View view) {
            Util.isAdded = true;
            Intent intent = new Intent(PlaylistActivity.this, MainActivity.class);
            intent.putExtra("PlaylistId", playLists.get(position).getP_id());
            startActivity(intent);
        }

        public void onMoreBtnClicked(View view) {
            if (playlistBinding.cvFolderSelect.getVisibility() == View.GONE) {
                playlistBinding.cvFolderSelect.setVisibility(View.VISIBLE);
            } else {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            if (AllVideoAdapter.selectedList.size() > 0) {
                playlistBinding.llRename.setVisibility(View.GONE);
                playlistBinding.llSelectAll.setVisibility(View.GONE);
            } else {
                playlistBinding.llSelectAll.setVisibility(View.VISIBLE);
            }
            if (FolderAdapter.selectedList.size() > 1) {
                playlistBinding.llRename.setVisibility(View.GONE);
            } else {
                playlistBinding.llRename.setVisibility(View.VISIBLE);
            }
        }

        public void onPropertiesClick(View v) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            propertiesFolder();
        }

        public void onRenameFolder(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            Log.e("LLL_Size: ", String.valueOf(playItemsList.size()));
            for (int i = 0; i < playItemsList.size(); i++) {
                playListVideoAdapter.removeItem1(i);
            }

            runOnUiThread(() -> {
                playItemsList = new ArrayList<>();
                playItemsList = dbHelper.getPlayListItems(playLists.get(position).getP_id());
                playListVideoAdapter = new PlayListVideoAdapter(playItemsList, PlaylistActivity.this);
                playlistBinding.rvAllVideos.setAdapter(playListVideoAdapter);
            });
        }

        public void onRemoveFileClick(View v) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            playListVideoAdapter.removeItem(PlayListVideoAdapter.mPositionSelected);

            Log.e("LLL_Size: ", String.valueOf(playItemsList.size()));
            if (playItemsList.size() <= 1)
                playlistBinding.tvVideoCounts.setText(playItemsList.size() + " video");
            else
                playlistBinding.tvVideoCounts.setText(playItemsList.size() + " videos");
        }

        public void shareFileClick(View v) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            Util.shareVideo(PlaylistActivity.this, new File(playItemsList.get(PlayListVideoAdapter.mPositionSelected).getBucketPath()));
            playlistBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void propertiesClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            propertySingleFile();
            playlistBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onSelectAll(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (playlistBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                playlistBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            if (playlistBinding.imgSelect.isSelected()) {
                playlistBinding.imgSelect.setSelected(false);
                playlistBinding.tvSelectAll.setText("Select All");

                PlayListVideoAdapter.selectedList.clear();
                PlayListVideoAdapter.selectedPathList.clear();
            } else {
                playlistBinding.imgSelect.setSelected(true);
                playlistBinding.tvSelectAll.setText("Deselect All");

                PlayListVideoAdapter.selectedList.clear();
                PlayListVideoAdapter.selectedPathList.clear();

                PlayListVideoAdapter.selectedList.addAll(playItemsList);
                for (int i = 0; i < playItemsList.size(); i++) {
                    PlayListVideoAdapter.selectedPathList.add(String.valueOf(i));
                }
            }

            playListVideoAdapter.notifyDataSetChanged();
            playlistBinding.rvAllVideos.getAdapter().notifyDataSetChanged();

        }
    }

}