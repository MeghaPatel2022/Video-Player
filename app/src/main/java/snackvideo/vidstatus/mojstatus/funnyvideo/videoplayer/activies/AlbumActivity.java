package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.AlbumVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.PlayListAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ActivityAlbumBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

public class AlbumActivity extends AppCompatActivity {

    private static final String TAG = AlbumActivity.class.getSimpleName();
    public static ActivityAlbumBinding albumBinding;
    public static boolean isUpdate = false;
    MyClickHandlers myClickHandlers;
    ArrayList<BaseModel> playLists = new ArrayList<>();
    AlbumVideoAdapter albumVideoAdapter;
    BottomSheetBehavior moreVideoOptionBehaviour;
    DBHelper dbHelper;
    private String bucketId = "";
    private MediaScannerConnection msConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumBinding = DataBindingUtil.setContentView(AlbumActivity.this, R.layout.activity_album);
        myClickHandlers = new MyClickHandlers(AlbumActivity.this);
        albumBinding.setOnClick(myClickHandlers);

        dbHelper = new DBHelper(AlbumActivity.this);

        if (getIntent() != null)
            bucketId = getIntent().getStringExtra("bucketId");

        playLists = Util.getFolderVideoCover(bucketId, AlbumActivity.this);

        albumBinding.tvTitle.setText(playLists.get(0).getBucketName());

        albumVideoAdapter = new AlbumVideoAdapter(new ArrayList<>(), AlbumActivity.this);
        albumBinding.rvAllVideos.setLayoutManager(new LinearLayoutManager(AlbumActivity.this, RecyclerView.VERTICAL, false));
        albumBinding.rvAllVideos.setAdapter(albumVideoAdapter);
        moreVideoOptionBehaviour = BottomSheetBehavior.from(albumBinding.llMorePopup);

        albumVideoAdapter.addItemClickListener(position -> {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            albumBinding.tvVidName.setText(playLists.get(position).getName());
        });

        setAdapter();

        EditText searchEditText = albumBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));

        Typeface myFont = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myFont = getResources().getFont(R.font.segoeui);
            searchEditText.setTypeface(myFont);
        }

        int colorInt = getResources().getColor(R.color.white);
        ColorStateList csl = ColorStateList.valueOf(colorInt);

        ImageView searchIcon = albumBinding.searchFile.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchIcon.setImageTintList(csl);
        }

        albumBinding.imgSearch.setOnClickListener(v -> {
            if (albumBinding.rlSearch.getVisibility() == View.VISIBLE) {
                Util.collapse(albumBinding.rlSearch);
            } else {
                Util.expand(albumBinding.rlSearch);
            }
        });

        albumBinding.searchFile.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                albumVideoAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                albumVideoAdapter.getFilter().filter(query);
                return true;
            }
        });

        albumBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        if (AlbumVideoAdapter.selectedList.size() > 0) {
            AlbumVideoAdapter.selectedList.clear();
            AlbumVideoAdapter.selectedPathList.clear();
            AlbumVideoAdapter.mPositionSelected = 0;
            albumVideoAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFinishing()) {
            if (isUpdate) {
                isUpdate = false;
                playLists = new ArrayList<>();
                playLists = Util.getFolderVideoCover(bucketId, AlbumActivity.this);

                albumBinding.tvTitle.setText(playLists.get(0).getBucketName());

                albumVideoAdapter = new AlbumVideoAdapter(new ArrayList<>(), AlbumActivity.this);
                albumBinding.rvAllVideos.setLayoutManager(new LinearLayoutManager(AlbumActivity.this, RecyclerView.VERTICAL, false));
                albumBinding.rvAllVideos.setAdapter(albumVideoAdapter);
                moreVideoOptionBehaviour = BottomSheetBehavior.from(albumBinding.llMorePopup);

                albumVideoAdapter.addItemClickListener(position -> {
                    if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    albumBinding.tvVidName.setText(playLists.get(position).getName());
                });

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AlbumActivity.this);
                Intent localIn;
                localIn = new Intent("REFRESH");
                lbm.sendBroadcast(localIn);

                LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(AlbumActivity.this);
                Intent localIn1;
                localIn1 = new Intent("REFRESH_HISTORY");
                lbm1.sendBroadcast(localIn1);

                LocalBroadcastManager lbm2 = LocalBroadcastManager.getInstance(AlbumActivity.this);
                Intent localIn2;
                localIn2 = new Intent("REFRESH_PLAYLIST");
                lbm2.sendBroadcast(localIn2);

                setAdapter();
            }
        }
    }

    private void setAdapter() {
        albumVideoAdapter.clear();
        for (int i = 0; i < playLists.size(); i++) {
            albumVideoAdapter.add(i, playLists.get(i));
        }

    }

    private void deleteFolder() {
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_rename);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LottieAnimationView deleteAnim = dial.findViewById(R.id.deleteAnim);
        deleteAnim.setVisibility(View.VISIBLE);

        LottieAnimationView renameAnim = dial.findViewById(R.id.renameAnim);
        renameAnim.setVisibility(View.GONE);

        TextView title = dial.findViewById(R.id.tvTitleDel);
        title.setText("Are you sure you want to delete the Folder?");

        TextView del = dial.findViewById(R.id.yes);
        del.setText("Delete");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setVisibility(View.GONE);

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            new DeleteFolder(AlbumActivity.this).execute();
            dial.dismiss();
        });

        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
            AlbumVideoAdapter.selectedPathList = new ArrayList<>();
            AlbumVideoAdapter.selectedList = new ArrayList<>();
            albumVideoAdapter.notifyDataSetChanged();
        });

        dial.show();
    }

    private void propertiesFolder() {
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
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

        if (AlbumVideoAdapter.selectedList.size() == 1) {

            llLocation.setVisibility(View.VISIBLE);
            llContains.setVisibility(View.GONE);
            llDate.setVisibility(View.VISIBLE);
            llMedia.setVisibility(View.VISIBLE);
            tvFile.setVisibility(View.VISIBLE);

            BaseModel baseModel = AlbumVideoAdapter.selectedList.get(0);

            tvTitle.setText(baseModel.getName());

            String extension = FilenameUtils.getExtension(baseModel.getBucketPath());
            tvFormat.setText(extension);
            Log.e("LLL_Extension: ", extension);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(baseModel.getBucketPath());
            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

            tvLength.setText(String.format("%d:%d sec",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            ));

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
            for (int j = 0; j < AlbumVideoAdapter.selectedList.size(); j++) {
                File file1 = new File(AlbumVideoAdapter.selectedList.get(j).getBucketPath());
                count += file1.length();
            }
            tvSize.setText(Constant.getSize(count));
            tvCount.setText(AlbumVideoAdapter.selectedList.size() + " Videos ");
        }

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
    }

    private void addToPlayList() {
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_playlist);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        RelativeLayout rlCreatePlaylist = dial.findViewById(R.id.rlCreatePlaylist);
        RecyclerView rvAllVideos = dial.findViewById(R.id.rvAllVideos);
        TextView no = dial.findViewById(R.id.no);
        TextView yes = dial.findViewById(R.id.yes);

        yes.setVisibility(View.GONE);

        PlayListAdapter playListAdapter = new PlayListAdapter("Playlist dialog", dbHelper.getPlayList(), AlbumActivity.this);
        rvAllVideos.setLayoutManager(new LinearLayoutManager(AlbumActivity.this, RecyclerView.VERTICAL, false));
        if (dbHelper.getPlayList().size() > 2) {
            ViewGroup.LayoutParams params = rvAllVideos.getLayoutParams();
            params.height = 500;
            rvAllVideos.setLayoutParams(params);
        }
        rvAllVideos.setAdapter(playListAdapter);

        no.setOnClickListener(view -> dial.dismiss());

        rlCreatePlaylist.setOnClickListener(v -> {
            dial.dismiss();
            createPlaylist();
        });

        playListAdapter.addItemClickListener(position -> {
            PlayList playList = dbHelper.getPlayList().get(position);
            BaseModel baseModel = playLists.get(AlbumVideoAdapter.mPositionSelected);
            baseModel.setP_id(playList.getP_id());
            dbHelper.insertPlayListSong(baseModel);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AlbumActivity.this);
            Intent localIn;
            localIn = new Intent("REFRESH_PLAYLIST");
            lbm.sendBroadcast(localIn);

            Toasty.info(AlbumActivity.this, "Video added in " + playList.getP_name(), Toasty.LENGTH_SHORT).show();
            dial.dismiss();
        });

        dial.show();
    }

    private void createPlaylist() {
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
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

                PlayList playList = dbHelper.getPlayList().get(dbHelper.getPlayList().size() - 1);
                BaseModel baseModel = playLists.get(AlbumVideoAdapter.mPositionSelected);
                baseModel.setP_id(playList.getP_id());
                dbHelper.insertPlayListSong(baseModel);

                Log.e("LLL_Create: ", String.valueOf(isCreate));
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AlbumActivity.this);
                Intent localIn;
                localIn = new Intent("REFRESH_PLAYLIST");
                lbm.sendBroadcast(localIn);

                Toasty.success(AlbumActivity.this, "Video added in " + playList.getP_name(), Toasty.LENGTH_SHORT).show();
                dial.dismiss();
            } else {
                Toasty.info(AlbumActivity.this, "Already exists in the playlist", Toasty.LENGTH_SHORT).show();
            }

            dial.dismiss();
        });
        no.setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    private void propertiesSinglefile() {
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
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

        BaseModel baseModel = playLists.get(AlbumVideoAdapter.mPositionSelected);

        tvTitle.setText(baseModel.getName());

        String extension = FilenameUtils.getExtension(baseModel.getBucketPath());
        tvFormat.setText(extension);
        Log.e("LLL_Extension: ", extension);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(baseModel.getBucketPath());
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        tvLength.setText(String.format("%d:%d sec",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        ));

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

    private void renameFile() {
        File oldFile = new File(playLists.get(AlbumVideoAdapter.mPositionSelected).getBucketPath());
        final Dialog dial = new Dialog(AlbumActivity.this, android.R.style.Theme_Black);
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
        etRename.setText(oldFile.getName());

        String extension1 = playLists.get(AlbumVideoAdapter.mPositionSelected).getName().substring(playLists.get(AlbumVideoAdapter.mPositionSelected).getName().lastIndexOf("."));
        String str1 = playLists.get(AlbumVideoAdapter.mPositionSelected).getName();
        etRename.setText(str1.replace(extension1, ""));

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            String extension = oldFile.getAbsolutePath().substring(oldFile.getAbsolutePath().lastIndexOf("."));
            File sdcard = new File(oldFile.getParentFile().getAbsolutePath());
            File newFileName = new File(sdcard, etRename.getText().toString() + extension);
            boolean isRename = oldFile.renameTo(newFileName);

            if (isRename) {
                Log.e("LLL_Rename: ", "From: " + oldFile.getAbsolutePath() + " To: " + newFileName.getAbsolutePath());

                dbHelper.renameHistory(oldFile.getAbsolutePath(), newFileName.getAbsolutePath());
                dbHelper.renamePlaylist(oldFile.getAbsolutePath(), newFileName.getAbsolutePath());

                Toasty.normal(AlbumActivity.this, "Rename File " + oldFile.getName() + " to " + newFileName.getName(), Toasty.LENGTH_SHORT).show();
                ContentResolver resolver = getApplicationContext().getContentResolver();
                resolver.delete(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                                " =?", new String[]{oldFile.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(newFileName));
                getApplicationContext().sendBroadcast(intent);

                runOnUiThread(() -> scanPhoto(newFileName.toString()));
                isUpdate = true;
                new Handler().postDelayed(this::onResume, 1000);
            }

            dial.dismiss();
        });
        dial.findViewById(R.id.no).setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    public void scanPhoto(final String imageFileName) {
        Log.e("LLLL_FileScanned: ", imageFileName);
        msConn = new MediaScannerConnection(getApplicationContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onMoreBtnClicked(View view) {
            if (albumBinding.cvFolderSelect.getVisibility() == View.GONE) {
                albumBinding.cvFolderSelect.setVisibility(View.VISIBLE);
            } else {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            if (AlbumVideoAdapter.selectedPathList.size() > 1) {
                albumBinding.llRename.setVisibility(View.GONE);
            } else {
                albumBinding.llRename.setVisibility(View.VISIBLE);
            }
        }

        public void onRenameFolder(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            renameFile();
        }

        public void onPropertiesClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            propertiesFolder();
        }

        public void onRenameFileClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            renameFile();
        }

        public void shareFileClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            Util.shareVideo(AlbumActivity.this, new File(playLists.get(AlbumVideoAdapter.mPositionSelected).getBucketPath()));
        }

        public void propertiesClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            propertiesSinglefile();
        }

        public void addToPlaylist(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            addToPlayList();
        }

        public void onDeleteLine(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            deleteFolder();
        }

        public void onSelectAll(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                albumBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            if (view.isSelected()) {
                view.setSelected(false);
                albumBinding.tvSelectAll.setText("Select All");

                AlbumVideoAdapter.selectedList.clear();
                AlbumVideoAdapter.selectedPathList.clear();
            } else {
                view.setSelected(true);
                albumBinding.tvSelectAll.setText("Deselect All");
                AlbumVideoAdapter.selectedList.clear();
                AlbumVideoAdapter.selectedPathList.clear();

                AlbumVideoAdapter.selectedList.addAll(playLists);
                for (int i = 0; i < playLists.size(); i++) {
                    AlbumVideoAdapter.selectedPathList.add(String.valueOf(i));
                }
            }
            albumVideoAdapter.notifyDataSetChanged();
            albumBinding.rvAllVideos.getAdapter().notifyDataSetChanged();
        }
    }

    class DeleteFolder extends AsyncTask<Void, Void, Boolean> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;

        public DeleteFolder(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            File file = new File(playLists.get(AlbumVideoAdapter.mPositionSelected).getBucketPath());
            dbHelper.getHistoryCheck(file.getAbsolutePath());
            if (dbHelper.ifPlayListItemExist(file.getAbsolutePath()))
                dbHelper.deleteOldPlayListItem(file.getAbsolutePath());
            boolean isDelete = file.delete();
            ContentResolver resolver = getApplicationContext().getContentResolver();
            resolver.delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                            " =?", new String[]{file.getAbsolutePath()});
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            getApplicationContext().sendBroadcast(intent);

            Log.e("LLL_FileDel: ", String.valueOf(isDelete));

            runOnUiThread(() -> {
                if (playLists.size() == 1) {
                    playLists.remove(AlbumVideoAdapter.mPositionSelected);
                    Intent intent1 = new Intent(AlbumActivity.this, MainActivity.class);
                    startActivity(intent1);
                    finish();
                } else {
                    playLists.remove(AlbumVideoAdapter.mPositionSelected);
                    albumVideoAdapter.removeItem(AlbumVideoAdapter.mPositionSelected);
                    isUpdate = true;
                    new Handler().postDelayed(AlbumActivity.this::onResume, 1000);
                }
            });

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AlbumActivity.this);
            Intent localIn;
            localIn = new Intent("REFRESH");
            lbm.sendBroadcast(localIn);

            LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(AlbumActivity.this);
            Intent localIn1;
            localIn1 = new Intent("REFRESH_HISTORY");
            lbm1.sendBroadcast(localIn1);

            LocalBroadcastManager lbm2 = LocalBroadcastManager.getInstance(AlbumActivity.this);
            Intent localIn2;
            localIn2 = new Intent("REFRESH_PLAYLIST");
            lbm2.sendBroadcast(localIn2);

            FoldersFragment.isUpdate = true;

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isDelete) {
            super.onPostExecute(isDelete);
            Log.d(TAG, "onPostExecute: " + "done");

            AlbumVideoAdapter.selectedList = new ArrayList<>();
            AlbumVideoAdapter.selectedPathList = new ArrayList<>();
            Toasty.info(AlbumActivity.this, "Deleted successfully.", Toasty.LENGTH_SHORT).show();
        }
    }


}