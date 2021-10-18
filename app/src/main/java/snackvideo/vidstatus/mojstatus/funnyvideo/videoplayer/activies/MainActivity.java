package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.internal.BaselineLayout;

import es.dmoral.toasty.Toasty;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.AllVideoAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.FolderAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter.HistoryAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.pageradapter.ViewPagerAdapter;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ActivityMainBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.HistoryFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;

import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment.foldersBinding;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.HistoryFragment.historyBinding;
import static snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment.fragmentVideosBinding;

public class MainActivity extends BaseActivity {

    public static ActivityMainBinding mainBinding;
    public boolean isPlayList = false;
    MyClickHandlers myClickHandlers;
    ViewPagerAdapter viewPagerAdapter;
    boolean isSet = false;
    BottomSheetBehavior moreVideoOptionBehaviour;
    private MoreOptionReceiver moreOptionReceiver;
    private String PlaylistId = "1";

  /*  private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }*/

    @Override
    public void permissionGranted() {
        if (!isSet)
            setViewPager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        myClickHandlers = new MyClickHandlers(MainActivity.this);
        mainBinding.setOnClick(myClickHandlers);

        moreOptionReceiver = new MoreOptionReceiver();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(moreOptionReceiver,
                new IntentFilter("MORE_OPTION"));

        PlaylistId = getIntent().getStringExtra("PlaylistId");

        moreVideoOptionBehaviour = BottomSheetBehavior.from(mainBinding.llMorePopup);

        EditText searchEditText = mainBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);
        Typeface myFont = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myFont = getResources().getFont(R.font.segoeui);
            searchEditText.setTypeface(myFont);
        }

        int colorInt = getResources().getColor(R.color.white);
        ColorStateList csl = ColorStateList.valueOf(colorInt);

        ImageView searchIcon = mainBinding.searchFile.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchIcon.setImageTintList(csl);
        }

        mainBinding.imgSearch.setOnClickListener(v -> {
            if (mainBinding.rlSearch.getVisibility() == View.VISIBLE) {
                Util.collapse(mainBinding.rlSearch);
            } else {
                Util.expand(mainBinding.rlSearch);
            }
        });
    }

    private void setViewPager() {
        isSet = true;
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mainBinding.viewPager.setAdapter(viewPagerAdapter);

        int position = 0;

        switch (Constant.backFrom) {
            case "Main":
                position = 0;
                break;
            case "Video":
                position = 1;
                break;
            case "History":
                position = 2;
                break;
            case "PlayList":
                position = 3;
                break;
        }

        if (Util.isAdded) {
            position = 1;
        }

        if (position == 0) {
            mainBinding.viewPager.setCurrentItem(0);
            mainBinding.imgSearch.setVisibility(View.VISIBLE);
            mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_folders).setChecked(true);
        } else if (position == 1) {
            mainBinding.viewPager.setCurrentItem(1);
            mainBinding.imgSearch.setVisibility(View.VISIBLE);
            mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_videos).setChecked(true);
        } else if (position == 2) {
            mainBinding.viewPager.setCurrentItem(2);
            mainBinding.imgSearch.setVisibility(View.GONE);
            mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_history).setChecked(true);
        } else {
            mainBinding.viewPager.setCurrentItem(3);
            mainBinding.imgSearch.setVisibility(View.GONE);
            mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_playlist).setChecked(true);
        }

        mainBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (mainBinding.rlSearch.getVisibility() == View.VISIBLE) {
                    EditText searchEditText = mainBinding.searchFile.findViewById(androidx.appcompat.R.id.search_src_text);
                    searchEditText.getText().clear();
                    Util.collapse(mainBinding.rlSearch);
                }
            }

            @Override
            public void onPageSelected(int i) {
                if (mainBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                    mainBinding.cvFolderSelect.setVisibility(View.GONE);
                }
                mainBinding.tvTitle.setText(viewPagerAdapter.getPageTitle(i));
                switch (i) {
                    case 0:
                        mainBinding.imgSearch.setVisibility(View.VISIBLE);
                        mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_folders).setChecked(true);
                        break;
                    case 1:
                        mainBinding.imgSearch.setVisibility(View.VISIBLE);
                        mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_videos).setChecked(true);
                        break;
                    case 2:
                        mainBinding.imgSearch.setVisibility(View.GONE);
                        mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_history).setChecked(true);
                        break;
                    case 3:
                        mainBinding.imgSearch.setVisibility(View.GONE);
                        mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_playlist).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mainBinding.bottomNavigation.setOnNavigationItemSelectedListener(
                item -> {
                    if (mainBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                        mainBinding.cvFolderSelect.setVisibility(View.GONE);
                    }
                    switch (item.getItemId()) {
                        case R.id.navigation_folders:
                            mainBinding.imgSearch.setVisibility(View.VISIBLE);
                            mainBinding.viewPager.setCurrentItem(0);
                            break;
                        case R.id.navigation_videos:
                            mainBinding.imgSearch.setVisibility(View.VISIBLE);
                            mainBinding.viewPager.setCurrentItem(1);
                            break;
                        case R.id.navigation_history:
                            mainBinding.imgSearch.setVisibility(View.GONE);
                            mainBinding.viewPager.setCurrentItem(2);
                            break;
                        case R.id.navigation_playlist:
                            mainBinding.imgSearch.setVisibility(View.GONE);
                            mainBinding.viewPager.setCurrentItem(3);
                            break;
                    }
                    return false;
                });
        setNavigationTypeface();
        mainBinding.viewPager.setOffscreenPageLimit(4);
    }

    public void setNavigationTypeface() {
        final Typeface avenirHeavy = ResourcesCompat.getFont(this, R.font.ibmplexserif_medium);//replace it with your own font
        ViewGroup navigationGroup = (ViewGroup) mainBinding.bottomNavigation.getChildAt(0);
        for (int i = 0; i < navigationGroup.getChildCount(); i++) {
            ViewGroup navUnit = (ViewGroup) navigationGroup.getChildAt(i);
            for (int j = 0; j < navUnit.getChildCount(); j++) {
                View navUnitChild = navUnit.getChildAt(j);
                if (navUnitChild instanceof BaselineLayout) {
                    BaselineLayout baselineLayout = (BaselineLayout) navUnitChild;
                    for (int k = 0; k < baselineLayout.getChildCount(); k++) {
                        View baselineChild = baselineLayout.getChildAt(k);
                        if (baselineChild instanceof TextView) {
                            TextView textView = (TextView) baselineChild;
                            textView.setTypeface(avenirHeavy);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        runOnUiThread(() -> {
            if (AllVideoPlayer.musicMediaPlayer != null) {
                if (AllVideoPlayer.musicMediaPlayer.isPlaying()) {
                    AllVideoPlayer.musicMediaPlayer.stop();
                    AllVideoPlayer.musicMediaPlayer.reset();
                    AllVideoPlayer.musicMediaPlayer.release();
                    AllVideoPlayer.musicMediaPlayer = null;
                    Toasty.info(MainActivity.this, "Play in background stop.", Toasty.LENGTH_SHORT, true).show();
                }
            } else if (FolderAdapter.selectedList.size() > 0) {
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
            } else if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (mainBinding.cvDeleteHis.getVisibility() == View.VISIBLE) {
                mainBinding.cvDeleteHis.setVisibility(View.GONE);
            } else if (mainBinding.cvSecondTop.getVisibility() == View.VISIBLE) {
                mainBinding.cvSecondTop.setVisibility(View.GONE);
            } else if (mainBinding.cvFolderSelect.getVisibility() == View.VISIBLE) {
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            } else if (mainBinding.rlSearch.getVisibility() == View.VISIBLE) {
                Util.collapse(mainBinding.rlSearch);
            } else {
                int position = mainBinding.viewPager.getCurrentItem();

                if (position == 0) {
                    super.onBackPressed();
                } else if (position == 1) {
                    mainBinding.viewPager.setCurrentItem(0);
                    mainBinding.imgSearch.setVisibility(View.VISIBLE);
                    mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_folders).setChecked(true);
                } else if (position == 2) {
                    mainBinding.viewPager.setCurrentItem(1);
                    mainBinding.imgSearch.setVisibility(View.VISIBLE);
                    mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_videos).setChecked(true);
                } else {
                    mainBinding.viewPager.setCurrentItem(2);
                    mainBinding.imgSearch.setVisibility(View.GONE);
                    mainBinding.bottomNavigation.getMenu().findItem(R.id.navigation_history).setChecked(true);
                }
            }
        });
    }

    private class MoreOptionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                mainBinding.tvVidName.setText(intent.getStringExtra("fileName"));
            }
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            if (intent.getStringExtra("isFrom") != null) {
                if (intent.getStringExtra("isFrom").equals("PlayList")) {
                    isPlayList = true;
                    mainBinding.tvShare.setVisibility(View.GONE);
                    mainBinding.tvAddToPlaylist.setVisibility(View.GONE);
                } else {
                    isPlayList = false;
                    mainBinding.tvShare.setVisibility(View.VISIBLE);
                    mainBinding.tvAddToPlaylist.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onMoreBtnClicked(View view) {
            if (mainBinding.cvFolderSelect.getVisibility() == View.GONE) {
                mainBinding.cvFolderSelect.setVisibility(View.VISIBLE);
            } else {
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            }
            if (AllVideoAdapter.selectedList.size() > 0) {
                mainBinding.llRename.setVisibility(View.GONE);
                mainBinding.llSelectAll.setVisibility(View.GONE);
            } else {
                mainBinding.llSelectAll.setVisibility(View.VISIBLE);
            }
            if (FolderAdapter.selectedList.size() > 0) {
                mainBinding.llRename.setVisibility(View.GONE);
                mainBinding.llSelectAll.setVisibility(View.GONE);
            } else {
                mainBinding.llSelectAll.setVisibility(View.VISIBLE);
            }
        }

        public void onDeleteBtnClick(View view) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("DELETE_FOLDER");
            localIn.putExtra("isFolder", AllVideoAdapter.selectedPathList.size() <= 0);
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onPropertiesClick(View v) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("PROPERTIES");
            localIn.putExtra("isFolder", AllVideoAdapter.selectedPathList.size() <= 0);
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onRenameFolder(View view) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("RENAME_FOLDER");
            localIn.putExtra("isFolder", AllVideoAdapter.selectedPathList.size() <= 0);
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onRenameFileClick(View v) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            if (isPlayList) {
                Intent localIn;
                localIn = new Intent("MORE_OPERATION_PLAY");
                localIn.putExtra("option", "Rename");
                lbm.sendBroadcast(localIn);
            } else {
                Intent localIn;
                localIn = new Intent("MORE_OPERATION");
                localIn.putExtra("option", "Rename");
                lbm.sendBroadcast(localIn);
            }
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void shareFileClick(View v) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("MORE_OPERATION");
            localIn.putExtra("option", "Share");
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void propertiesClick(View view) {
            if (!isPlayList) {
                if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
                Intent localIn;
                localIn = new Intent("MORE_OPERATION");
                localIn.putExtra("option", "Property");
                lbm.sendBroadcast(localIn);
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            } else {
                if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
                Intent localIn;
                localIn = new Intent("MORE_OPERATION_PLAY");
                localIn.putExtra("option", "Property");
                lbm.sendBroadcast(localIn);
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            }
        }

        public void addToPlaylist(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("MORE_OPERATION");
            localIn.putExtra("option", "AddToPlayList");
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onDoneClick(View view) {
            if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("MORE_OPERATION");
            localIn.putExtra("option", "onDone");
            localIn.putExtra("PlaylistId", PlaylistId);
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }

        public void onDeleteLine(View view) {
            if (!isPlayList) {
                if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
                Intent localIn;
                localIn = new Intent("MORE_OPERATION");
                localIn.putExtra("option", "deleteFile");
                lbm.sendBroadcast(localIn);
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            } else {
                if (moreVideoOptionBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    moreVideoOptionBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
                Intent localIn;
                localIn = new Intent("MORE_OPERATION_PLAY");
                localIn.putExtra("option", "deleteFile");
                lbm.sendBroadcast(localIn);
                mainBinding.cvFolderSelect.setVisibility(View.GONE);
            }

        }

        public void onSelectAll(View view) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
            Intent localIn;
            localIn = new Intent("MORE_OPERATION");
            localIn.putExtra("option", "selectAll");
            lbm.sendBroadcast(localIn);
            mainBinding.cvFolderSelect.setVisibility(View.GONE);
        }
    }
}