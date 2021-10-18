package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.FolderAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.HistoryAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.FragmentFoldersBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.MainActivity.mainBinding;

public class FoldersFragment extends Fragment {

    public static String TAG = FoldersFragment.class.getSimpleName();
    public static FragmentFoldersBinding foldersBinding;
    public static FolderAdapter folderAdapter;
    public static boolean isUpdate = false;
    HistoryAdapter recentVideoAdapter;
    ArrayList<BaseModel> directoriesList;
    DBHelper dbHelper;
    private RenameReceiver renameReceiver;
    private DeleteReceiver deleteReceiver;
    private PropertiesReceiver propertiesReceiver;
    private MediaScannerConnection msConn;

    private MoreOperationReceiver moreOperationReceiver;

    public FoldersFragment() {
        // Required empty public constructor
    }

    public static String getAlbumPathRenamed(String olderPath, String newName) {
        return olderPath.substring(0, olderPath.lastIndexOf('/')) + "/" + newName;
    }

    public static String getPhotoPathRenamedAlbumChange(String olderPath, String albumNewName) {
        String c = "";
        String[] b = olderPath.split("/");
        for (int x = 0; x < b.length - 2; x++) c += b[x] + "/";
        c += albumNewName + "/" + b[b.length - 1];
        return c;
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
            FolderAdapter.selectedPathList = new ArrayList<>();
            FolderAdapter.selectedList = new ArrayList<>();
            folderAdapter.notifyDataSetChanged();
        });
        dial.show();
    }


    private void deleteFolderAll() {
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
        title.setText("Are you sure you want to delete the History?");

        TextView del = dial.findViewById(R.id.yes);
        del.setText("Delete");

        EditText etRename = dial.findViewById(R.id.etRename);
        etRename.setVisibility(View.GONE);

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            ArrayList<BaseModel> historyList = dbHelper.getHistoryData();
            for (int i = 0; i < historyList.size(); i++) {
                dbHelper.deleteOldHistory(historyList.get(i).getBucketPath());
            }
            Objects.requireNonNull(getActivity()).runOnUiThread(FoldersFragment.this::onResume);

            FolderAdapter.selectedList = new ArrayList<>();
            FolderAdapter.selectedPathList = new ArrayList<>();

            LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(getActivity());
            Intent localIn1;
            localIn1 = new Intent("REFRESH_HISTORY");
            lbm1.sendBroadcast(localIn1);

            Toasty.info(getContext(), "Deleted successfully.", Toasty.LENGTH_SHORT).show();
            dial.dismiss();
        });
        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
            FolderAdapter.selectedPathList = new ArrayList<>();
            FolderAdapter.selectedList = new ArrayList<>();
            folderAdapter.notifyDataSetChanged();
        });
        dial.show();
    }

    private void propertiesFolder() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialoge_properties);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        LinearLayout llLocation, llDate;
        llDate = dial.findViewById(R.id.llDate);
        llLocation = dial.findViewById(R.id.llLocation);

        TextView tvFile = dial.findViewById(R.id.tvFile);
        TextView tvTitle = dial.findViewById(R.id.tvTitle);
        TextView del = dial.findViewById(R.id.yes);
        TextView tvPath = dial.findViewById(R.id.tvPath);
        TextView tvCreatedAt = dial.findViewById(R.id.tvCreatedAt);
        TextView tvSize = dial.findViewById(R.id.tvSize);
        TextView tvCount = dial.findViewById(R.id.tvVideoCounts);

        LinearLayout llMedia = dial.findViewById(R.id.llMedia);

        llMedia.setVisibility(View.GONE);
        tvFile.setVisibility(View.GONE);

        if (FolderAdapter.selectedList.size() == 1) {
            BaseModel baseModel = FolderAdapter.selectedList.get(0);

            tvTitle.setText(baseModel.getBucketName());

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

            long count = 0;
            for (int i = 0; i < baseModel.pathlist.size(); i++) {
                File file1 = new File(baseModel.pathlist.get(i));
                count += file1.length();
                tvSize.setText(Constant.getSize(count));
            }

        } else {
            tvTitle.setText("Properties");
            llLocation.setVisibility(View.GONE);
            llDate.setVisibility(View.GONE);
            long count = 0;
            long videoSize = 0;
            for (int j = 0; j < FolderAdapter.selectedList.size(); j++) {
                BaseModel baseModel = FolderAdapter.selectedList.get(j);
                videoSize = videoSize + baseModel.pathlist.size();
                for (int i = 0; i < baseModel.pathlist.size(); i++) {
                    File file1 = new File(baseModel.pathlist.get(i));
                    count += file1.length();
                }
            }
            tvSize.setText(Constant.getSize(count));
            tvCount.setText(videoSize + " Videos " + FolderAdapter.selectedList.size() + " Folders");

        }

        del.findViewById(R.id.yes).setOnClickListener(view -> dial.dismiss());

        dial.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mainBinding.searchFile.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    folderAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    folderAdapter.getFilter().filter(query);
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<BaseModel> folderListArray = new ArrayList<>();
        ArrayList<BaseModel> folderListArray1;
        folderListArray1 = dbHelper.getHistoryData();
        for (int i = 0; i < folderListArray1.size(); i++) {
            BaseModel baseModel = folderListArray1.get(i);
            File file = new File(baseModel.getBucketPath());
            if (file.exists()) {
                folderListArray.add(baseModel);
            } else {
                dbHelper.deleteOldHistory(baseModel.getBucketPath());
                if (dbHelper.ifPlayListItemExist(file.getAbsolutePath()))
                    dbHelper.deleteOldPlayListItem(file.getAbsolutePath());
            }
        }
        Collections.reverse(folderListArray);

        if (folderListArray.size() > 0) {
            foldersBinding.rvRecentVideos.setVisibility(View.VISIBLE);
            foldersBinding.tvTitle.setVisibility(View.VISIBLE);
            foldersBinding.imgDeleteHis.setVisibility(View.VISIBLE);
            getActivity().runOnUiThread(() -> recentVideoAdapter.addAll(folderListArray));
        } else {
            foldersBinding.rvRecentVideos.setVisibility(View.GONE);
            foldersBinding.tvTitle.setVisibility(View.GONE);
            foldersBinding.imgDeleteHis.setVisibility(View.GONE);
        }

        if (!isUpdate) {
            isUpdate = true;
            getFolders();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        foldersBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_folders, container, false);

        dbHelper = new DBHelper(getContext());

        foldersBinding.rvFolders.setLayoutManager(new GridLayoutManager(getContext(), 4));
        folderAdapter = new FolderAdapter(new ArrayList<>(), getActivity());
        foldersBinding.rvFolders.setAdapter(folderAdapter);
        foldersBinding.rvFolders.setNestedScrollingEnabled(false);

        foldersBinding.rvRecentVideos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recentVideoAdapter = new HistoryAdapter("Recent", getActivity());
        foldersBinding.rvRecentVideos.setAdapter(recentVideoAdapter);

        deleteReceiver = new DeleteReceiver();
        renameReceiver = new RenameReceiver();
        propertiesReceiver = new PropertiesReceiver();
        moreOperationReceiver = new MoreOperationReceiver();

        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(deleteReceiver,
                    new IntentFilter("DELETE_FOLDER"));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(renameReceiver,
                    new IntentFilter("RENAME_FOLDER"));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(propertiesReceiver,
                    new IntentFilter("PROPERTIES"));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(moreOperationReceiver,
                    new IntentFilter("MORE_OPERATION"));
        }

        mainBinding.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainBinding.rlSearch.getVisibility() == View.VISIBLE) {
                    Util.collapse(mainBinding.rlSearch);
                } else {
                    Util.expand(mainBinding.rlSearch);
                }
            }
        });

        EditText searchEditText = mainBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));

        foldersBinding.imgDeleteHis.setOnClickListener(v -> deleteFolderAll());

        return foldersBinding.getRoot();
    }

    public String GetParentPath(String path) {
        File file = new File(path);
        return file.getAbsoluteFile().getParent();
    }

    public ArrayList<String> getCameraVideoCover(String id) {
        String data = null;
        ArrayList<String> result = new ArrayList<>();

        final String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        final String selection = MediaStore.Video.Media.BUCKET_ID + " = ?";

        final String[] selectionArgs = {id};

        final Cursor cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(getContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
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

    private void getFolders() {
        ArrayList<BaseModel> folderListArray = new ArrayList<>();
        try {

            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, DATE_ADDED + " DESC");
            ArrayList<String> ids = new ArrayList<String>();
            int count = 0;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    BaseModel album = new BaseModel();
                    int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                    album.setBucketId(cursor.getString(columnIndex));

                    if (!ids.contains(album.getBucketId())) {
                        columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                        album.setBucketName(cursor.getString(columnIndex));

                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        String result = cursor.getString(column_index);

                        String ParentPath = GetParentPath(result);
                        File file = new File(result).getAbsoluteFile();
                        album.setBucketPath(ParentPath);

                        columnIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                        album.setId(cursor.getString(columnIndex));

                        count += file.length();

                        album.totalSize = count;
                        album.pathlist = getCameraVideoCover("" + album.getBucketId()); //----get four image path arraylist
                        folderListArray.add(album);
                        album.setSize(album.pathlist.size());

                        ids.add(album.getBucketId());
                    }
                }

                cursor.close();
                Message message = new Message();
                message.what = 21;
                message.obj = folderListArray;

                directoriesList = folderListArray;

                if (folderListArray.size() > 0) {
                    mainBinding.rlNoData.setVisibility(View.GONE);
                    mainBinding.layoutMain.setVisibility(View.VISIBLE);
                    getActivity().runOnUiThread(() -> folderAdapter.addAll(directoriesList));
                } else {
                    mainBinding.rlNoData.setVisibility(View.VISIBLE);
                    mainBinding.layoutMain.setVisibility(View.GONE);
                }
            }
        } catch (Exception w) {
            Log.e("Error:", w.getMessage());
        }
    }

    private void renameFolder() {
        final Dialog dial = new Dialog(getContext(), android.R.style.Theme_Black);
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

        del.findViewById(R.id.yes).setOnClickListener(view -> {
            dial.dismiss();
            new ASynchTaskRename(FolderAdapter.selectedList.get(0), etRename.getText().toString().trim()).execute();
        });

        dial.findViewById(R.id.no).setOnClickListener(v -> {
            dial.dismiss();
            FolderAdapter.selectedPathList = new ArrayList<>();
            FolderAdapter.selectedList = new ArrayList<>();
            getActivity().runOnUiThread(() -> onResume());
        });

        dial.show();
    }

    public boolean renameAlbum(BaseModel albumDetail, String newName) {
        boolean success;
        File newDir = new File(getAlbumPathRenamed(albumDetail.getBucketPath(), newName));
        File oldDir = new File(albumDetail.getBucketPath());
        success = oldDir.renameTo(newDir);
        Log.e("TAG", "renameAlbum: " + success);
        Log.e("From", oldDir.getPath());
        Log.e("To", newDir.getPath());
        if (success) {
            for (final String m : albumDetail.getPathlist()) {
                final File from = new File(m);
                File to = new File(getPhotoPathRenamedAlbumChange(m, newName));

                dbHelper.renameHistory(from.getAbsolutePath(), to.getAbsolutePath());
                dbHelper.renamePlaylist(from.getAbsolutePath(), to.getAbsolutePath());

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
                Intent localIn;
                localIn = new Intent("REFRESH_HISTORY");
                lbm.sendBroadcast(localIn);

                final String where = MediaStore.MediaColumns.DATA + "=?";
                final String[] selectionArgs = new String[]{albumDetail.getBucketPath() + "/" + to.getName()};

                try {
                    getActivity().getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Video.Media.DATA, to.getPath());
                    values.put(MediaStore.Video.Media.DATE_TAKEN, to.lastModified());
                    getActivity().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, to.getPath());
                    values.put(MediaStore.Images.Media.DATE_TAKEN, to.lastModified());
                    getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }


                scanFile(new String[]{from.getAbsolutePath()});
                scanFile(new String[]{to.getAbsolutePath()}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Log.d(s, "onScanCompleted: " + s);
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(new File(s)));
                        getActivity().sendBroadcast(intent);
                    }
                });
            }
        }
        return success;
    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(getActivity(), path, null, null);
    }

    public void scanFile(String[] path, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        MediaScannerConnection.scanFile(getActivity(), path, null, onScanCompletedListener);
    }

    public class ASynchTaskRename extends AsyncTask<Void, Integer, Boolean> {
        BaseModel albumDetail;
        String etName;

        public ASynchTaskRename(BaseModel albumDetail, String newName) {
            this.albumDetail = albumDetail;
            this.etName = newName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return renameAlbum(albumDetail, etName);
        }

        @Override
        protected void onPostExecute(Boolean str) {
            super.onPostExecute(str);
            isUpdate = false;
            FolderAdapter.selectedPathList = new ArrayList<>();
            FolderAdapter.selectedList = new ArrayList<>();
            getActivity().runOnUiThread(() -> onResume());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext().getApplicationContext());
                    Intent localIn;
                    localIn = new Intent("REFRESH");
                    lbm.sendBroadcast(localIn);

                    LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(getContext().getApplicationContext());
                    Intent localIn1;
                    localIn1 = new Intent("REFRESH_HISTORY");
                    lbm1.sendBroadcast(localIn1);
                }
            });

            Toasty.normal(getContext(), "Rename Folder Successfully", Toasty.LENGTH_SHORT).show();
        }
    }

    private class MoreOperationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String operation = intent.getStringExtra("option");
            if (operation.equals("Refresh")) {
                if (getActivity() != null)
                    onResume();
            }
        }
    }

    private class DeleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("isFolder", true))
                deleteFolder();
        }
    }

    private class RenameReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            renameFolder();
        }
    }

    private class PropertiesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("isFolder", true))
                propertiesFolder();
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
            for (int i = 0; i < FolderAdapter.selectedList.size(); i++) {
                File dir = new File(FolderAdapter.selectedList.get(i).getBucketPath());
                if (dir.exists()) {
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (String child : children) {
                            File file = new File(dir, child);
                            dbHelper.getHistoryCheck(file.getAbsolutePath());
                            if (dbHelper.ifPlayListItemExist(file.getAbsolutePath()))
                                dbHelper.deleteOldPlayListItem(file.getAbsolutePath());
                            boolean isDelete = file.delete();
                            scanPhoto(file.toString());
                            Log.e("LLL_FileDel: ", String.valueOf(isDelete));
                        }

                        if (dir.exists())
                            dir.delete();
                    }
                }

                if (getActivity() != null) {
                    int finalI = i;
                    directoriesList.remove(Integer.parseInt(FolderAdapter.selectedPathList.get(finalI)));
                    getActivity().runOnUiThread(() -> folderAdapter.notifyDataSetChanged());
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isDelete) {
            super.onPostExecute(isDelete);
            Log.d(TAG, "onPostExecute: " + "done");
            Objects.requireNonNull(getActivity()).runOnUiThread(FoldersFragment.this::onResume);

            FolderAdapter.selectedList = new ArrayList<>();
            FolderAdapter.selectedPathList = new ArrayList<>();
            Toasty.info(getContext(), "Deleted successfully.", Toasty.LENGTH_SHORT).show();
        }
    }
}