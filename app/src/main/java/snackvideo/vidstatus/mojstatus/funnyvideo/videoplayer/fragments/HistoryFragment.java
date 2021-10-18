package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies.MainActivity;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.HistoryAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.FragmentHistoryBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;

public class HistoryFragment extends Fragment {

    private static final String TAG = HistoryFragment.class.getSimpleName();
    public static HistoryAdapter historyAdapter;

    public static FragmentHistoryBinding historyBinding;
    DBHelper dbHelper;

    private RefreshReceiver refreshReceiver;

    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        historyBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);

        refreshReceiver = new RefreshReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshReceiver,
                new IntentFilter("REFRESH_HISTORY"));

        dbHelper = new DBHelper(getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        historyBinding.rvAllVideos.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                linearLayoutManager.getOrientation());
        Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
        dividerItemDecoration.setDrawable(verticalDivider);
        historyBinding.rvAllVideos.addItemDecoration(dividerItemDecoration);

        historyAdapter = new HistoryAdapter("History", getActivity());
        historyBinding.rvAllVideos.setAdapter(historyAdapter);

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
            }
        }
        Collections.reverse(folderListArray);

        if (folderListArray.size() > 0) {
            historyBinding.rlNoData.setVisibility(View.GONE);
            historyBinding.tvTitle.setVisibility(View.VISIBLE);
            historyBinding.rvAllVideos.setVisibility(View.VISIBLE);
            getActivity().runOnUiThread(() -> historyAdapter.addAll(folderListArray));
        } else {
            historyBinding.rlNoData.setVisibility(View.VISIBLE);
            historyBinding.tvTitle.setVisibility(View.GONE);
            historyBinding.rvAllVideos.setVisibility(View.GONE);
        }

        MainActivity.mainBinding.tvDelCancel.setOnClickListener(v -> {
            HistoryAdapter.selectedList.clear();
            HistoryAdapter.selectedPathList.clear();
            historyAdapter.notifyDataSetChanged();
            MainActivity.mainBinding.cvDeleteHis.setVisibility(View.GONE);
        });

        MainActivity.mainBinding.tvDelOk.setOnClickListener(v -> {
            for (int i = 0; i < HistoryAdapter.selectedList.size(); i++) {
                BaseModel baseModel = HistoryAdapter.selectedList.get(i);
                dbHelper.deleteOldHistory(baseModel.getBucketPath());
                int finalI = i;
                getActivity().runOnUiThread(() -> historyAdapter.notifyItemChanged(Integer.parseInt(HistoryAdapter.selectedPathList.get(finalI))));
            }
            HistoryAdapter.selectedList.clear();
            HistoryAdapter.selectedPathList.clear();
            MainActivity.mainBinding.cvDeleteHis.setVisibility(View.GONE);
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext().getApplicationContext());
            Intent localIn;
            localIn = new Intent("MORE_OPERATION");
            localIn.putExtra("option", "Refresh");
            lbm.sendBroadcast(localIn);


            LocalBroadcastManager lbm1 = LocalBroadcastManager.getInstance(getActivity());
            Intent localIn1;
            localIn1 = new Intent("REFRESH_HISTORY");
            lbm1.sendBroadcast(localIn1);
        });

        return historyBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
                }
            }
            Collections.reverse(folderListArray);

            if (folderListArray.size() > 0) {
                historyBinding.rlNoData.setVisibility(View.GONE);
                historyBinding.tvTitle.setVisibility(View.VISIBLE);
                historyBinding.rvAllVideos.setVisibility(View.VISIBLE);
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> historyAdapter.addAll(folderListArray));
            } else {
                historyBinding.rlNoData.setVisibility(View.VISIBLE);
                historyBinding.tvTitle.setVisibility(View.GONE);
                historyBinding.rvAllVideos.setVisibility(View.GONE);
            }
        }
    }

}