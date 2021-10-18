package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.AllVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.PlayListAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.FragmentVideosBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.MainActivity.mainBinding;

public class VideosFragment extends Fragment {

    private static final String TAG = VideosFragment.class.getSimpleName();
    public static boolean isUpdate = false;
    public static AllVideoAdapter allVideoAdapter;
    public static FragmentVideosBinding fragmentVideosBinding;
    public ArrayList<BaseModel> videoList = new ArrayList<>();
    DBHelper dbHelper;
    Long size;
    private MoreOperationReceiver moreOperationReceiver;
    private MediaScannerConnection msConn;
    private DeleteReceiver deleteReceiver;
    private PropertiesReceiver propertiesReceiver;
    private RefreshReceiver refreshReceiver;

    public VideosFragment() {
    }

    public void removeAndSetData(ArrayList<BaseModel> baseModels) {
        videoList.clear();
        videoList.addAll(baseModels);
    }

    public String GetParentPath(String path) {
        File file = new File(path);
        return file.getAbsoluteFile().getParent();
    }

    public String GetPath(String path) {
        File file = new File(path);
        return file.getAbsoluteFile().getAbsolutePath();
    }

    public ArrayList<String> getCameraVideoCover(Activity fragmentActivity, String id) {
        String data = null;
        ArrayList<String> result = new ArrayList<>();

        final String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        final String selection = MediaStore.Video.Media.BUCKET_ID + " = ?";

        final String[] selectionArgs = {id};

        final Cursor cursor = fragmentActivity.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor.moveToFirst()) {

            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            final int name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            do {
                data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentVideosBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_videos, container, false);

        dbHelper = new DBHelper(getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        fragmentVideosBinding.rvAllVideos.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
        dividerItemDecoration.setDrawable(verticalDivider);
        fragmentVideosBinding.rvAllVideos.addItemDecoration(dividerItemDecoration);

        allVideoAdapter = new AllVideoAdapter(new ArrayList<>(), getActivity());
        fragmentVideosBinding.rvAllVideos.setAdapter(allVideoAdapter);
        new LoadVideoFolders(getActivity()).execute();

        deleteReceiver = new DeleteReceiver();
        refreshReceiver = new RefreshReceiver();
        propertiesReceiver = new PropertiesReceiver();
        moreOperationReceiver = new MoreOperationReceiver();

        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(deleteReceiver,
                    new IntentFilter("DELETE_FOLDER"));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(propertiesReceiver,
                    new IntentFilter("PROPERTIES"));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshReceiver,
                    new IntentFilter("REFRESH"));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(moreOperationReceiver,
                    new IntentFilter("MORE_OPERATION"));
        }

        EditText searchEditText = mainBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));

        return fragmentVideosBinding.getRoot();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mainBinding.searchFile.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.e("LLL_Video:", query);
                    // filter recycler view when query submitted
                    allVideoAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    // filter recycler view when text is changed
                    allVideoAdapter.getFilter().filter(query);
                    return true;
                }
            });
        }
    }

    private void renameFile() {
        File oldFile = new File(videoList.get(AllVideoAdapter.mPositionSelected).getBucketPath());
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
        etRename.setText(oldFile.getName());

        String extension1 = videoList.get(AllVideoAdapter.mPositionSelected).getName().substring(videoList.get(AllVideoAdapter.mPositionSelected).getName().lastIndexOf("."));
        String str1 = videoList.get(AllVideoAdapter.mPositionSelected).getName();
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

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
                Intent localIn;
                localIn = new Intent("REFRESH_HISTORY");
                lbm.sendBroadcast(localIn);

                Toasty.normal(getActivity(), "Rename File " + oldFile.getName() + " to " + newFileName.getName(), Toasty.LENGTH_SHORT).show();
                ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
                resolver.delete(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                                " =?", new String[]{oldFile.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(newFileName));
                getActivity().getApplicationContext().sendBroadcast(intent);

                getActivity().runOnUiThread(() -> scanPhoto(newFileName.toString()));
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
        msConn = new MediaScannerConnection(getActivity().getApplicationContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
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

    @Override
    public void onResume() {
        super.onResume();
        if (isUpdate) {
            isUpdate = false;
            new LoadVideoFolders(getActivity()).execute();
        }
    }

    private void addToPlayList() {
        if (getContext() != null) {
            final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
            dial.requestWindowFeature(1);
            dial.setContentView(R.layout.dialog_playlist);
            dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dial.setCanceledOnTouchOutside(true);

            RelativeLayout rlCreatePlaylist = dial.findViewById(R.id.rlCreatePlaylist);
            RecyclerView rvAllVideos = dial.findViewById(R.id.rvAllVideos);
            TextView no = dial.findViewById(R.id.no);
            TextView yes = dial.findViewById(R.id.yes);

            yes.setVisibility(View.GONE);

            PlayListAdapter playListAdapter = new PlayListAdapter("Playlist dialog", dbHelper.getPlayList(), getActivity());
            rvAllVideos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
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
                BaseModel baseModel = videoList.get(AllVideoAdapter.mPositionSelected);
                baseModel.setP_id(playList.getP_id());
                dbHelper.insertPlayListSong(baseModel);

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                Intent localIn;
                localIn = new Intent("REFRESH_PLAYLIST");
                lbm.sendBroadcast(localIn);

                Toasty.info(getContext(), "Video added in " + playList.getP_name(), Toasty.LENGTH_SHORT).show();
                dial.dismiss();
            });

            dial.show();
        }
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

                PlayList playList = dbHelper.getPlayList().get(dbHelper.getPlayList().size() - 1);
                BaseModel baseModel = videoList.get(AllVideoAdapter.mPositionSelected);
                baseModel.setP_id(playList.getP_id());
                dbHelper.insertPlayListSong(baseModel);

                Log.e("LLL_Create: ", String.valueOf(isCreate));
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                Intent localIn;
                localIn = new Intent("REFRESH_PLAYLIST");
                lbm.sendBroadcast(localIn);

                Toasty.success(getContext(), "Video added in " + playList.getP_name(), Toasty.LENGTH_SHORT).show();
                dial.dismiss();
            } else {
                Toasty.info(getActivity(), "Already exists in the playlist", Toasty.LENGTH_SHORT).show();
            }

            dial.dismiss();
        });
        no.setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    private void deleteFolder() {
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
        title.setText("Are you sure you want to delete the Folder?");

        TextView del = dial.findViewById(R.id.yes);
        del.setText("Delete");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setVisibility(View.GONE);

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            new DeleteFolder(getActivity()).execute();
            dial.dismiss();
        });

        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
            AllVideoAdapter.selectedPathList = new ArrayList<>();
            AllVideoAdapter.selectedList = new ArrayList<>();
            allVideoAdapter.notifyDataSetChanged();
        });

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
        title.setText("Are you sure you want to delete the Folder?");

        TextView del = dial.findViewById(R.id.yes);
        del.setText("Delete");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setVisibility(View.GONE);

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            new DeleteFolderMore(getActivity()).execute();
            dial.dismiss();
        });

        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
            AllVideoAdapter.selectedPathList = new ArrayList<>();
            AllVideoAdapter.selectedList = new ArrayList<>();
            allVideoAdapter.notifyDataSetChanged();
        });

        dial.show();
    }

    private void propertiesSinglefile() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
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

        BaseModel baseModel = videoList.get(AllVideoAdapter.mPositionSelected);

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

    private void propertiesFolder() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
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

        if (AllVideoAdapter.selectedList.size() == 1) {

            llLocation.setVisibility(View.VISIBLE);
            llContains.setVisibility(View.GONE);
            llDate.setVisibility(View.VISIBLE);
            llMedia.setVisibility(View.VISIBLE);
            tvFile.setVisibility(View.VISIBLE);

            BaseModel baseModel = AllVideoAdapter.selectedList.get(0);

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
            if (baseModel.pathlist.size() == 1)
                tvCount.setText(baseModel.pathlist.size() + " Video");
            else
                tvCount.setText(baseModel.pathlist.size() + " Videos");

            tvSize.setText(Util.getFileSize(file.length()));

        } else {
            tvTitle.setText("Properties");
            llLocation.setVisibility(View.GONE);
            llDate.setVisibility(View.GONE);
            long count = 0;
            for (int j = 0; j < AllVideoAdapter.selectedList.size(); j++) {
                File file1 = new File(AllVideoAdapter.selectedList.get(j).getBucketPath());
                count += file1.length();
            }
            tvSize.setText(Constant.getSize(count));
            tvCount.setText(AllVideoAdapter.selectedList.size() + " Videos ");
        }

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
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
            for (int i = 0; i < AllVideoAdapter.selectedList.size(); i++) {
                File file = new File(AllVideoAdapter.selectedList.get(i).getBucketPath());
                dbHelper.getHistoryCheck(file.getAbsolutePath());
                if (dbHelper.ifPlayListItemExist(file.getAbsolutePath()))
                    dbHelper.deleteOldPlayListItem(file.getAbsolutePath());
                boolean isDelete = file.delete();
                ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
                resolver.delete(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                                " =?", new String[]{file.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                getActivity().getApplicationContext().sendBroadcast(intent);

                Log.e("LLL_FileDel: ", String.valueOf(isDelete));
                if (getActivity() != null) {
                    int finalI = i;
                    getActivity().runOnUiThread(() -> {
                        videoList.remove(Integer.parseInt(AllVideoAdapter.selectedPathList.get(finalI)));
                        allVideoAdapter.removeItem(Integer.parseInt(AllVideoAdapter.selectedPathList.get(finalI)));
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext().getApplicationContext());
                        Intent localIn;
                        localIn = new Intent("MORE_OPERATION");
                        localIn.putExtra("option", "Refresh");
                        lbm.sendBroadcast(localIn);
                    });
                }

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isDelete) {
            super.onPostExecute(isDelete);
            Log.d(TAG, "onPostExecute: " + "done");
            /*if (getActivity() != null) {
                isUpdate = true;
                getActivity().runOnUiThread(() -> new Handler().postDelayed(VideosFragment.this::onResume, 1000));
            }*/

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            Intent localIn;
            localIn = new Intent("REFRESH_PLAYLIST");
            lbm.sendBroadcast(localIn);

            LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(getContext());
            Intent localIn1;
            localIn1 = new Intent("REFRESH_HISTORY");
            lbm1.sendBroadcast(localIn1);

            AllVideoAdapter.selectedList = new ArrayList<>();
            AllVideoAdapter.selectedPathList = new ArrayList<>();
            Toasty.info(getContext(), "Deleted successfully.", Toasty.LENGTH_SHORT).show();
        }
    }

    class DeleteFolderMore extends AsyncTask<Void, Void, Boolean> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;

        public DeleteFolderMore(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            File file = new File(videoList.get(AllVideoAdapter.mPositionSelected).getBucketPath());
            dbHelper.getHistoryCheck(file.getAbsolutePath());
            if (dbHelper.ifPlayListItemExist(file.getAbsolutePath()))
                dbHelper.deleteOldPlayListItem(file.getAbsolutePath());
            boolean isDelete = file.delete();
            ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
            resolver.delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA +
                            " =?", new String[]{file.getAbsolutePath()});
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            getActivity().getApplicationContext().sendBroadcast(intent);

            Log.e("LLL_FileDel: ", String.valueOf(isDelete));
            if (getActivity() != null) {
                int finalI = AllVideoAdapter.mPositionSelected;
                getActivity().runOnUiThread(() -> {
                    videoList.remove(finalI);
                    allVideoAdapter.removeItem(finalI);
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext().getApplicationContext());
                    Intent localIn;
                    localIn = new Intent("MORE_OPERATION");
                    localIn.putExtra("option", "Refresh");
                    lbm.sendBroadcast(localIn);
                });
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isDelete) {
            super.onPostExecute(isDelete);
            Log.d(TAG, "onPostExecute: " + "done");
            /*if (getActivity() != null) {
                isUpdate = true;
                getActivity().runOnUiThread(() -> new Handler().postDelayed(VideosFragment.this::onResume, 1000));
            }*/
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            Intent localIn;
            localIn = new Intent("REFRESH_PLAYLIST");
            lbm.sendBroadcast(localIn);

            LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(getContext());
            Intent localIn1;
            localIn1 = new Intent("REFRESH_HISTORY");
            lbm1.sendBroadcast(localIn1);

            AllVideoAdapter.selectedList = new ArrayList<>();
            AllVideoAdapter.selectedPathList = new ArrayList<>();
            Toasty.info(getContext(), "Deleted successfully.", Toasty.LENGTH_SHORT).show();
        }
    }

    public class LoadVideoFolders extends AsyncTask<Void, Void, ArrayList<BaseModel>> {

        @SuppressLint("StaticFieldLeak")
        Activity fragmentActivity;


        public LoadVideoFolders(Activity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
            getActivity().runOnUiThread(() -> allVideoAdapter.clear());
            videoList.clear();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<BaseModel> doInBackground(Void... voids) {

            int i = 0;
            ArrayList<BaseModel> folderListArray = new ArrayList<>();
            try {

                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA};
                Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(uri, projection, null, null, DATE_ADDED + " DESC");
                ArrayList<String> ids = new ArrayList<String>();
                int count = 0;

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        BaseModel album = new BaseModel();
                        int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                        album.setBucketId(cursor.getString(columnIndex));

                        columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                        album.setBucketName(cursor.getString(columnIndex));

                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        String result = cursor.getString(column_index);

                        String ParentPath = GetParentPath(result);
                        String path = GetPath(result);
                        File file = new File(path);

                        album.setBucketPath(file.getAbsolutePath());

                        album.setName(file.getName());

                        columnIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                        album.setId(cursor.getString(columnIndex));

                        album.pathlist = getCameraVideoCover(getActivity(), "" + album.getBucketId()); //----get four image path arraylist
                        album.setSize(file.getAbsolutePath().length());
                        folderListArray.add(album);

                        if (i == 0)
                            Constant.size = file.length();
                        else
                            Constant.size += file.length();

                        int finalI = i;
                        videoList.add(album);
                        getActivity().runOnUiThread(() -> allVideoAdapter.add(finalI, album));
                        i = i + 1;

                        ids.add(album.getBucketId());

                    }

                    cursor.close();
                }
            } catch (Exception w) {
                Log.e("Error:", w.getMessage());
            }

            return folderListArray;
        }

        @Override
        protected void onPostExecute(ArrayList<BaseModel> directories) {
            super.onPostExecute(directories);

            if (directories.size() > 0)
                fragmentVideosBinding.tvSizeCount.setText(directories.size() + " Videos " + Constant.getSize(Constant.size));
            Log.d(TAG, "onPostExecute: " + "done");
        }
    }

    private class MoreOperationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String operation = intent.getStringExtra("option");
            if (operation != null) {
                switch (operation) {
                    case "Rename":
                        if (getActivity() != null)
                            renameFile();
                        break;
                    case "Share":
                        Util.shareVideo(getActivity(), new File(videoList.get(AllVideoAdapter.mPositionSelected).getBucketPath()));
                        break;
                    case "Property":
                        propertiesSinglefile();
                        break;
                    case "AddToPlayList":
                        addToPlayList();
                        break;
                    case "onDone":
                        for (int i = 0; i < AllVideoAdapter.selectedList.size(); i++) {
                            String PlaylistId = intent.getStringExtra("PlaylistId");
                            PlayList playList = dbHelper.getPlayList().get(Integer.parseInt(PlaylistId) - 1);
                            BaseModel baseModel = AllVideoAdapter.selectedList.get(i);
                            baseModel.setP_id(playList.getP_id());
                            dbHelper.insertPlayListSong(baseModel);
                        }

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                        Intent localIn;
                        localIn = new Intent("REFRESH_PLAYLIST");
                        lbm.sendBroadcast(localIn);

                        mainBinding.cvSecondTop.setVisibility(View.GONE);
                        AllVideoAdapter.selectedList.clear();
                        AllVideoAdapter.selectedPathList.clear();
                        VideosFragment.allVideoAdapter.notifyDataSetChanged();
                        fragmentVideosBinding.rvAllVideos.getAdapter().notifyDataSetChanged();

                        Util.isAdded = false;
                        mainBinding.imgDone.setVisibility(View.GONE);

                        Constant.backFrom = "PlayList";
                        mainBinding.viewPager.setCurrentItem(3);
                        mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_playlist).setChecked(true);
                        break;
                    case "deleteFile":
                        if (getContext() != null)
                            deleteFolderMore();
                        break;
                    case "selectAll":

                        if (mainBinding.imgSelect.isSelected()) {
                            mainBinding.imgSelect.setSelected(false);
                            mainBinding.tvSelectAll.setText("Select All");

                            AllVideoAdapter.selectedList.clear();
                            AllVideoAdapter.selectedPathList.clear();
                        } else {
                            mainBinding.imgSelect.setSelected(true);
                            mainBinding.tvSelectAll.setText("Deselect All");
                            AllVideoAdapter.selectedList.clear();
                            AllVideoAdapter.selectedPathList.clear();

                            AllVideoAdapter.selectedList.addAll(videoList);
                            for (int i = 0; i < videoList.size(); i++) {
                                AllVideoAdapter.selectedPathList.add(String.valueOf(i));
                            }
                        }
                        allVideoAdapter.notifyDataSetChanged();
                        fragmentVideosBinding.rvAllVideos.getAdapter().notifyDataSetChanged();

                        break;

                }
            }
        }
    }

    private class DeleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("isFolder", true))
                deleteFolder();
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isUpdate = true;
            onResume();
        }
    }

    private class PropertiesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("isFolder", true))
                propertiesFolder();
        }
    }
}