package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.PlaybackParams;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import io.hamed.floatinglayout.FloatingLayout;
import io.hamed.floatinglayout.callback.FloatingListener;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper.DBHelper;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ActivityAllVideoPlayerBinding;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.EqualizerModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.sharedprefrance.SharedPreference;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Constant;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils.Util;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class AllVideoPlayer extends AppCompatActivity {

    public static ArrayList<BaseModel> mainList;
    public static ActivityAllVideoPlayerBinding allVideoPlayerBinding;
    public static FloatingLayout floatingLayout;
    public static int currentPos = 0;
    public static int position = 0;
    public static MediaPlayer musicMediaPlayer;
    public MediaScannerConnection msConn;
    public android.widget.VideoView videoView;
    DBHelper dbHelper;
    BassBoost bassBoost;
    PresetReverb presetReverb;
    Virtualizer virtualizer;
    String[] courses = {"None", "Small Room",
            "Medium room", "Large Room",
            "Medium Hall", "Large Hall", "Plate"};
    int prevSelection = 0;
    SeekBar[] seekBarFinal;
    int newPosition;
    Intent activityIntent = null;
    int currentPosition;
    private String url = "";
    private MyReceiver myReceiver;
    private MyReceiver1 myReceiver1;
    private ForwardReceiver forwardReceiver;
    private BackwardReceiver backwardReceiver;
    private Equalizer mEqualizer;
    private MediaPlayer mediaPlayer;
    private final FloatingListener floatingListener = new FloatingListener() {
        @Override
        public void onCreateListener(View view) {
            videoView = view.findViewById(R.id.video_view);
            videoView.setVideoPath(mainList.get(position).getBucketPath());

            videoView.setOnPreparedListener(mp -> {
                mediaPlayer = mp;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer.seekTo(newPosition, MediaPlayer.SEEK_CLOSEST);
                } else {
                    mediaPlayer.seekTo(newPosition);
                }
                videoView.start();
            });

            ImageView imgFullClose = view.findViewById(R.id.imgFullClose);
            imgFullClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    floatingLayout.destroy();
                }
            });

            ImageView imgFullScreen = view.findViewById(R.id.imgFullScreen);
            imgFullScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPos = videoView.getCurrentPosition();
                    floatingLayout.destroy();
                    AllVideoPlayer allVideoPlayer = new AllVideoPlayer();
                    allVideoPlayer.addAll(mainList);
                    Intent intent = new Intent(AllVideoPlayer.this, AllVideoPlayer.class);
                    intent.putExtra("position", position);
                    intent.putExtra("currPosition", currentPos);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

        }

        @Override
        public void onCloseListener() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allVideoPlayerBinding =
                DataBindingUtil.setContentView(AllVideoPlayer.this, R.layout.activity_all_video_player);

        dbHelper = new DBHelper(AllVideoPlayer.this);

        myReceiver = new MyReceiver();
        myReceiver1 = new MyReceiver1();
        forwardReceiver = new ForwardReceiver();
        backwardReceiver = new BackwardReceiver();

        LocalBroadcastManager.getInstance(AllVideoPlayer.this).registerReceiver(myReceiver,
                new IntentFilter("TAG_SHOW"));

        LocalBroadcastManager.getInstance(AllVideoPlayer.this).registerReceiver(myReceiver1,
                new IntentFilter("TAG_HIDE"));

        LocalBroadcastManager.getInstance(AllVideoPlayer.this).registerReceiver(forwardReceiver,
                new IntentFilter("TAG_FORWARD"));

        LocalBroadcastManager.getInstance(AllVideoPlayer.this).registerReceiver(backwardReceiver,
                new IntentFilter("TAG_BACKWARD"));

        if (floatingLayout != null) {
            if (floatingLayout.isShow()) {
                floatingLayout.destroy();
            }
        }

        if (getIntent() != null) {
            position = getIntent().getIntExtra("position", 0);
            BaseModel baseModel = (BaseModel) getIntent().getSerializableExtra("selectedVideo");

            currentPosition = getIntent().getIntExtra("currPosition", 0);
            activityIntent = getIntent();
            if (musicMediaPlayer != null) {
                if (musicMediaPlayer.isPlaying()) {
                    musicMediaPlayer.stop();
                    musicMediaPlayer.reset();
                    musicMediaPlayer.release();
                    musicMediaPlayer = null;
                    musicMediaPlayer = new MediaPlayer();
                }
            }

            url = mainList.get(position).getBucketPath();

            boolean isInserted = dbHelper.insertPlayHistory(mainList.get(position));
            if (isInserted) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AllVideoPlayer.this);
                Intent localIn;
                localIn = new Intent("REFRESH_HISTORY");
                lbm.sendBroadcast(localIn);
            }

            allVideoPlayerBinding.videoView.setVideoPath(url);
            allVideoPlayerBinding.videoView.getPlayer().start();

            allVideoPlayerBinding.videoView.getVideoInfo().setBgColor(Color.BLACK);

            allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());

            int selectedAspectRatio = VideoInfo.AR_ASPECT_WRAP_CONTENT;
            allVideoPlayerBinding.videoView.getPlayer().aspectRatio(selectedAspectRatio);

            allVideoPlayerBinding.videoView.setPlayerListener(new PlayerListener() {
                @Override
                public void onPrepared(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {
                    Log.e("LLL_Curr: ", String.valueOf(currentPosition));
                    if (currentPosition >= 1000) {
                        runOnUiThread(() -> allVideoPlayerBinding.videoView.getPlayer().seekTo(currentPosition));
                        currentPosition = 0;
                    }
                }

                @Override
                public void onBufferingUpdate(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, int percent) {
                }

                @Override
                public boolean onInfo(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onCompletion(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {
                    if (allVideoPlayerBinding.llMorePopup.getVisibility() == View.VISIBLE) {
                        setEqualizerModel();
                        allVideoPlayerBinding.llMorePopup.setVisibility(View.GONE);
                    }

                    if (Util.isShuffle) {
                        Collections.shuffle(mainList);
                    }

                    if (Util.isRepeat.equals("one")) {

                        dbHelper.insertPlayHistory(mainList.get(position));

                        allVideoPlayerBinding.videoView.setVideoPath(mainList.get(position).getBucketPath());

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(mainList.get(position).getBucketPath());

                        allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());
                        allVideoPlayerBinding.videoView.getPlayer().start();
                    } else if (Util.isRepeat.equals("all")) {
                        if (position == mainList.size() - 1) {
                            position = 0;
                        } else {
                            position = position + 1;
                        }
                        dbHelper.insertPlayHistory(mainList.get(position));

                        allVideoPlayerBinding.videoView.setVideoPath(mainList.get(position).getBucketPath());

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(mainList.get(position).getBucketPath());

                        allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());
                        allVideoPlayerBinding.videoView.getPlayer().start();
                    } else {
                        if (position != mainList.size() - 1) {
                            position = position + 1;

                            dbHelper.insertPlayHistory(mainList.get(position));

                            allVideoPlayerBinding.videoView.setVideoPath(mainList.get(position).getBucketPath());

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(mainList.get(position).getBucketPath());

                            allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());
                            allVideoPlayerBinding.videoView.getPlayer().start();
                        } else {
                            onBackPressed();
                        }
                    }
                }

                @Override
                public void onSeekComplete(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {

                }

                @Override
                public boolean onError(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, int what, int extra) {
                    return false;
                }

                @Override
                public void onPause(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onRelease(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onStart(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTargetStateChange(int oldState, int newState) {

                }

                @Override
                public void onCurrentStateChange(int oldState, int newState) {

                }

                @Override
                public void onDisplayModelChange(int oldModel, int newModel) {

                }

                @Override
                public void onPreparing(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer) {

                }

                @Override
                public void onTimedText(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, IjkTimedText text) {
                    Log.e("LLL_Text: ", text.getText());
                }

                @Override
                public void onLazyLoadProgress(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, int progress) {
                    Log.e("LLL_Progress: ", String.valueOf(progress));
                }

                @Override
                public void onLazyLoadError(tcking.github.com.giraffeplayer2.GiraffePlayer giraffePlayer, String message) {

                }
            });

            allVideoPlayerBinding.imgSS.setOnClickListener(v -> {
                allVideoPlayerBinding.videoView.getPlayer().getCurrentDisplay().setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(allVideoPlayerBinding.videoView.getPlayer().getCurrentDisplay().getBitmap());
                allVideoPlayerBinding.videoView.getPlayer().getCurrentDisplay().setDrawingCacheEnabled(false);
                Uri uri = getImageUri(AllVideoPlayer.this, bitmap);
                CaptureImage(uri);
            });

            allVideoPlayerBinding.imgEqualizer.setOnClickListener(v -> {
                mEqualizer = new Equalizer(0, allVideoPlayerBinding.videoView.getPlayer().getAudioSessionId());
                mEqualizer.setEnabled(true);

                if (allVideoPlayerBinding.llMorePopup.getVisibility() == View.VISIBLE) {
                    setEqualizerModel();
                    allVideoPlayerBinding.llMorePopup.setVisibility(View.GONE);
                } else {
                    setEqualizer();
                    getEqualizerModel();
                    allVideoPlayerBinding.llMorePopup.setVisibility(View.VISIBLE);
                }
            });

            allVideoPlayerBinding.imgOrientation.setOnClickListener(v -> {
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
                if (allVideoPlayerBinding.llMorePopup.getVisibility() == View.VISIBLE)
                    allVideoPlayerBinding.llMorePopup.setVisibility(View.GONE);
            });

            allVideoPlayerBinding.imgSpeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //works only from api 23

                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioSessionId(allVideoPlayerBinding.videoView.getPlayer().getAudioSessionId());
                    PlaybackParams myPlayBackParams = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        myPlayBackParams = new PlaybackParams();
                        myPlayBackParams.setSpeed(0.8f); //you can set speed here
                        mediaPlayer.setPlaybackParams(myPlayBackParams);
                    }
                }
            });

            allVideoPlayerBinding.imgFloatScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(AllVideoPlayer.this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 100);
                        } else {
                            floatingLayout = new FloatingLayout(AllVideoPlayer.this, R.layout.float_box1);
                            floatingLayout.setFloatingListener(floatingListener);
                            floatingLayout.create();

                            GiraffePlayer player = allVideoPlayerBinding.videoView.getPlayer();
                            newPosition = player.getCurrentPosition();

                            if (allVideoPlayerBinding.videoView.getPlayer().isPlaying()) {
                                onBackPressed();
                            }

                        }
                    } else {
                        floatingLayout = new FloatingLayout(AllVideoPlayer.this, R.layout.float_box1);
                        floatingLayout.setFloatingListener(floatingListener);
                        floatingLayout.create();

                        GiraffePlayer player = allVideoPlayerBinding.videoView.getPlayer();
                        newPosition = player.getCurrentPosition();

                        if (allVideoPlayerBinding.videoView.getPlayer().isPlaying()) {
                            onBackPressed();
                        }

                    }
                }
            });

            allVideoPlayerBinding.imgMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.isSelected()) {
                        v.setSelected(false);
                        allVideoPlayerBinding.videoView.getPlayer().setMute(false);
                    } else {
                        v.setSelected(true);
                        allVideoPlayerBinding.videoView.getPlayer().setMute(true);
                    }
                }
            });

            allVideoPlayerBinding.imgBackground.setOnClickListener(v -> {
                playInBackground(allVideoPlayerBinding.videoView.getPlayer().getCurrentPosition());
                onBackPressed();
            });

            allVideoPlayerBinding.imgRepeat.setOnClickListener(v -> {
                if (Util.isRepeat.equals("off")) {
                    Util.isRepeat = "one";
                    allVideoPlayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_one);
                    Toasty.info(AllVideoPlayer.this, "Repeat One", Toasty.LENGTH_SHORT).show();
                } else if (Util.isRepeat.equals("one")) {
                    Util.isRepeat = "all";
                    allVideoPlayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_all);
                    Toasty.info(AllVideoPlayer.this, "Repeat All", Toasty.LENGTH_SHORT).show();
                } else {
                    Util.isRepeat = "off";
                    allVideoPlayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat);
                    Toasty.info(AllVideoPlayer.this, "Repeat Off", Toasty.LENGTH_SHORT).show();
                }
            });

            allVideoPlayerBinding.imgShuffle.setOnClickListener(v -> {
                if (Util.isShuffle) {
                    Util.isShuffle = false;
                    allVideoPlayerBinding.imgRepeat.setSelected(false);
                    Toasty.info(AllVideoPlayer.this, "Shuffle Off", Toasty.LENGTH_SHORT).show();
                } else {
                    Util.isShuffle = true;
                    allVideoPlayerBinding.imgRepeat.setSelected(true);
                    Toasty.info(AllVideoPlayer.this, "Shuffle On", Toasty.LENGTH_SHORT).show();
                }
            });

            allVideoPlayerBinding.imgBack.setOnClickListener(v -> onBackPressed());
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                if (!Settings.canDrawOverlays(AllVideoPlayer.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 100);
                }
            } else {
                floatingLayout = new FloatingLayout(AllVideoPlayer.this, R.layout.float_box1);
                floatingLayout.setFloatingListener(floatingListener);
                floatingLayout.create();

                GiraffePlayer player = allVideoPlayerBinding.videoView.getPlayer();
                newPosition = player.getCurrentPosition();

                if (allVideoPlayerBinding.videoView.getPlayer().isPlaying()) {
                    onBackPressed();
                }
            }
        }
    }

    public void playInBackground(int pos) {
        Toasty.info(AllVideoPlayer.this, "Video play in background.", Toast.LENGTH_SHORT).show();
        if (musicMediaPlayer != null) {
            if (musicMediaPlayer.isPlaying()) {
                musicMediaPlayer.stop();
                musicMediaPlayer.reset();
                musicMediaPlayer.release();
                musicMediaPlayer = null;
                musicMediaPlayer = new MediaPlayer();
            }
        } else {
            musicMediaPlayer = new MediaPlayer();
        }
        try {
            musicMediaPlayer.setDataSource(AllVideoPlayer.this, Uri.parse(mainList.get(position).getBucketPath()));
            musicMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        musicMediaPlayer.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            musicMediaPlayer.seekTo(pos, MediaPlayer.SEEK_CLOSEST);
        } else {
            musicMediaPlayer.seekTo(pos);
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        if (allVideoPlayerBinding.llMorePopup.getVisibility() == View.VISIBLE)
            allVideoPlayerBinding.llMorePopup.setVisibility(View.GONE);
        super.onConfigurationChanged(newConfig);
    }

    private void setEqualizer() {

        if (Constant.isEqualizerOn) {
            allVideoPlayerBinding.equalizerOn.setChecked(true);
            allVideoPlayerBinding.mBlankRl.setVisibility(View.GONE);
            allVideoPlayerBinding.tvOnOff.setText("ON");
        } else {
            allVideoPlayerBinding.equalizerOn.setChecked(false);
            allVideoPlayerBinding.mBlankRl.setVisibility(View.VISIBLE);
            allVideoPlayerBinding.tvOnOff.setText("OFF");
        }
        allVideoPlayerBinding.equalizerOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Constant.isEqualizerOn = true;
                allVideoPlayerBinding.mBlankRl.setVisibility(View.GONE);
                allVideoPlayerBinding.tvOnOff.setText("ON");
            } else {
                Constant.isEqualizerOn = false;
                allVideoPlayerBinding.mBlankRl.setVisibility(View.VISIBLE);
                allVideoPlayerBinding.tvOnOff.setText("OFF");
            }
        });

        allVideoPlayerBinding.tabLayout1.addTab(allVideoPlayerBinding.tabLayout1.newTab().setText("Custom"));
        seekBarFinal = new SeekBar[5];

        bassBoost = new BassBoost(0, allVideoPlayerBinding.videoView.getPlayer().getAudioSessionId());
        BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
        BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
        bassBoostSetting.strength = (1000 / 19);
        bassBoost.setProperties(bassBoostSetting);

        allVideoPlayerBinding.bassboostSeekbar.setMax(100);

        allVideoPlayerBinding.bassboostSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bassBoost.setEnabled(true);
                bassBoostSetting.strength = (short) (((float) 100 / 19) * (progress));
                bassBoost.setProperties(bassBoostSetting);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        presetReverb = new PresetReverb(0, allVideoPlayerBinding.videoView.getPlayer().getAudioSessionId());
        presetReverb.setPreset(PresetReverb.PRESET_NONE);
        presetReverb.setEnabled(true);

        virtualizer = new Virtualizer(0, allVideoPlayerBinding.videoView.getPlayer().getAudioSessionId());
        virtualizer.setEnabled(true);
        Virtualizer.Settings virtualizerSettingTemp = virtualizer.getProperties();
        Virtualizer.Settings virtualizerSetting = new Virtualizer.Settings(virtualizerSettingTemp.toString());
        virtualizerSetting.strength = (1000 / 19);
        virtualizer.setProperties(virtualizerSetting);

        allVideoPlayerBinding.virtulizerSeekbar.setMax(100);

        allVideoPlayerBinding.virtulizerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                virtualizerSetting.strength = (short) (((float) 1000 / 19) * (progress));
                virtualizer.setProperties(virtualizerSetting);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            allVideoPlayerBinding.tabLayout1.addTab(allVideoPlayerBinding.tabLayout1.newTab().setText(mEqualizer.getPresetName(i)));
        }

        ViewGroup slidingTabStrip = (ViewGroup) allVideoPlayerBinding.tabLayout1.getChildAt(0);

        for (int i = 0; i < slidingTabStrip.getChildCount() - 1; i++) {
            View v = slidingTabStrip.getChildAt(i);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.rightMargin = 10;
            params.leftMargin = 10;
        }

        allVideoPlayerBinding.tabLayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    if (allVideoPlayerBinding.tabLayout1.getSelectedTabPosition() != 0) {
                        mEqualizer.usePreset((short) (allVideoPlayerBinding.tabLayout1.getSelectedTabPosition() - 1));
                        short numberOfFreqBands = 5;
                        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
                        for (short i = 0; i < numberOfFreqBands; i++) {
                            seekBarFinal[i].setProgress(mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                        }
                    }
                } catch (Exception e) {
                    Toasty.error(getBaseContext(), "Error while updating Equalizer", Toasty.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        short bands = mEqualizer.getNumberOfBands();

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < bands; i++) {
            final short band = i;

            SeekBar seekBar = new SeekBar(getBaseContext());
            TextView textView = new TextView(getBaseContext());
            TextView textView1 = new TextView(getBaseContext());
            switch (i) {
                case 0:
                    seekBar = allVideoPlayerBinding.seekBar1;
                    textView = allVideoPlayerBinding.mHz1;
                    textView1 = allVideoPlayerBinding.tvDb1;
                    break;
                case 1:
                    seekBar = allVideoPlayerBinding.seekBar2;
                    textView = allVideoPlayerBinding.mHz2;
                    textView1 = allVideoPlayerBinding.tvDb2;
                    break;
                case 2:
                    seekBar = allVideoPlayerBinding.seekBar3;
                    textView = allVideoPlayerBinding.mHz3;
                    textView1 = allVideoPlayerBinding.tvDb3;
                    break;
                case 3:
                    seekBar = allVideoPlayerBinding.seekBar4;
                    textView = allVideoPlayerBinding.mHz4;
                    textView1 = allVideoPlayerBinding.tvDb4;
                    break;
                case 4:
                    seekBar = allVideoPlayerBinding.seekBar5;
                    textView = allVideoPlayerBinding.mHz5;
                    textView1 = allVideoPlayerBinding.tvDb5;
                    break;
            }

            TextView freqTextView = new TextView(this);
            freqTextView.setTextColor(Color.WHITE);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");

            TextView minDbTextView = new TextView(this);
            minDbTextView.setTextColor(Color.WHITE);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setTextColor(Color.WHITE);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            seekBarFinal[i] = seekBar;
            seekBar.setId(i);
            seekBar.setMax(maxEQLevel - minEQLevel);

            seekBar.setProgress(mEqualizer.getBandLevel(band));

            textView.setText(freqTextView.getText());
            textView.setTextColor(Color.WHITE);

            textView1.setText(maxDbTextView.getText());
            textView1.setTextColor(Color.WHITE);

            Log.e("LLL_DataProgress: ", String.valueOf(seekBar.getProgress()));
            Log.e("LLL_DataMax: ", String.valueOf(seekBar.getMax()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.e("LLL_DataMin: ", String.valueOf(seekBar.getMin()));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        ArrayAdapter ad
                = new ArrayAdapter(
                this,
                R.layout.spinner_item,
                courses);

        ad.setDropDownViewResource(
                R.layout.spinner_drop_down);

        allVideoPlayerBinding.spinner.setAdapter(ad);
        allVideoPlayerBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prevSelection = position;
                switch (prevSelection) {
                    case 0:
                        presetReverb.setPreset(PresetReverb.PRESET_NONE);
                        break;
                    case 1:
                        presetReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
                        break;
                    case 2:
                        presetReverb.setPreset(PresetReverb.PRESET_MEDIUMROOM);
                        break;
                    case 3:
                        presetReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
                        break;
                    case 4:
                        presetReverb.setPreset(PresetReverb.PRESET_MEDIUMHALL);
                        break;
                    case 5:
                        presetReverb.setPreset(PresetReverb.PRESET_LARGEHALL);
                        break;
                    case 6:
                        presetReverb.setPreset(PresetReverb.PRESET_PLATE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                allVideoPlayerBinding.spinner.setSelection(prevSelection);
                switch (prevSelection) {
                    case 0:
                        presetReverb.setPreset(PresetReverb.PRESET_NONE);
                        break;
                    case 1:
                        presetReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
                        break;
                    case 2:
                        presetReverb.setPreset(PresetReverb.PRESET_MEDIUMROOM);
                        break;
                    case 3:
                        presetReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
                        break;
                    case 4:
                        presetReverb.setPreset(PresetReverb.PRESET_MEDIUMHALL);
                        break;
                    case 5:
                        presetReverb.setPreset(PresetReverb.PRESET_LARGEHALL);
                        break;
                    case 6:
                        presetReverb.setPreset(PresetReverb.PRESET_PLATE);
                        break;
                }
            }
        });
    }

    public void setEqualizerModel() {
        EqualizerModel model = new EqualizerModel();
        model.setEnable(Constant.isEqualizerOn);
        model.setPreset(allVideoPlayerBinding.tabLayout1.getSelectedTabPosition());

        if (allVideoPlayerBinding.tabLayout1.getSelectedTabPosition() == 0) {
            model.setVs1(allVideoPlayerBinding.seekBar1.getProgress());
            model.setVs2(allVideoPlayerBinding.seekBar2.getProgress());
            model.setVs3(allVideoPlayerBinding.seekBar3.getProgress());
            model.setVs4(allVideoPlayerBinding.seekBar4.getProgress());
            model.setVs5(allVideoPlayerBinding.seekBar5.getProgress());
        }
        model.setReverb(allVideoPlayerBinding.spinner.getSelectedItemPosition());
        model.setBass(allVideoPlayerBinding.bassboostSeekbar.getProgress());
        model.setVirtualizer(allVideoPlayerBinding.virtulizerSeekbar.getProgress());
        SharedPreference.setEqualizer(AllVideoPlayer.this, model);
    }

    public void getEqualizerModel() {
        EqualizerModel model = SharedPreference.getEqualizer(AllVideoPlayer.this);
        if (model != null) {
            Constant.isEqualizerOn = model.isEnable();

            allVideoPlayerBinding.tabLayout1.getTabAt(model.getPreset()).select();
            if (model.getPreset() == 0) {
                allVideoPlayerBinding.seekBar1.setProgress(model.getVs1());
                allVideoPlayerBinding.seekBar2.setProgress(model.getVs2());
                allVideoPlayerBinding.seekBar3.setProgress(model.getVs3());
                allVideoPlayerBinding.seekBar4.setProgress(model.getVs4());
                allVideoPlayerBinding.seekBar5.setProgress(model.getVs5());
            } else {
                if (allVideoPlayerBinding.tabLayout1.getSelectedTabPosition() != 0) {
                    mEqualizer.usePreset((short) (allVideoPlayerBinding.tabLayout1.getSelectedTabPosition() - 1));
                    short numberOfFreqBands = 5;
                    final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
                    for (short i = 0; i < numberOfFreqBands; i++) {
                        seekBarFinal[i].setProgress(mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                    }
                }
            }

            allVideoPlayerBinding.spinner.setSelection(model.getReverb());
            allVideoPlayerBinding.bassboostSeekbar.setProgress(model.getBass());
            allVideoPlayerBinding.tvBoostBass.setText(allVideoPlayerBinding.bassboostSeekbar.getProgress() + "%");
            allVideoPlayerBinding.virtulizerSeekbar.setProgress(model.getVirtualizer());

            allVideoPlayerBinding.tvVirtulizer.setText(allVideoPlayerBinding.virtulizerSeekbar.getProgress() + "%");
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void addAll(ArrayList<BaseModel> videoList) {
        mainList = new ArrayList<>();
        mainList.addAll(videoList);
    }

    public File CaptureImage(Uri uri) {

        Bitmap bitmap = null;
        File f = null;
        try {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (Exception e) {
                //handle exception
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            //----------------dsestination path--------
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myDir = new File(root, "DCIM");
            myDir.mkdirs();

            File myDir1 = new File(myDir, getString(R.string.app_name));
            myDir1.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Uri destination = Uri.fromFile(new File(myDir1.getPath() + "/" + timeStamp + ".png"));
            String NewImagePath = destination.getPath();

            //-----------------------------------------
            if (NewImagePath != null) {
                f = new File(NewImagePath);
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //------------insert into media list----
                File newfilee = new File(destination.getPath());
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, destination.getPath());
                values.put(MediaStore.Images.Media.DATE_TAKEN, newfilee.lastModified());
                scanPhoto(newfilee.getPath());
                Toasty.success(AllVideoPlayer.this, "Image saved successfully!!!", Toasty.LENGTH_LONG).show();
                Uri uri1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri1 = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", newfilee);
                } else {
                    uri1 = Uri.fromFile(newfilee);
                }
                getContentResolver().notifyChange(uri1, null);
            }
        } catch (Exception e) {
        }
        return f;

    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(AllVideoPlayer.this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
            }
        });
        msConn.connect();
    }

    @Override
    public void onBackPressed() {
        int orientation = getResources().getConfiguration().orientation;
        if (allVideoPlayerBinding.llMorePopup.getVisibility() == View.VISIBLE) {
            setEqualizerModel();
            allVideoPlayerBinding.llMorePopup.setVisibility(View.GONE);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (allVideoPlayerBinding.videoView.getPlayer() != null) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (allVideoPlayerBinding.videoView.getPlayer() != null) {
            allVideoPlayerBinding.videoView.getPlayer().onActivityPaused();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allVideoPlayerBinding.videoView.getPlayer() != null) {
            allVideoPlayerBinding.videoView.getPlayer().onActivityResumed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (allVideoPlayerBinding.videoView.getPlayer() != null) {
            allVideoPlayerBinding.videoView.getPlayer().onActivityDestroyed();
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            allVideoPlayerBinding.rlTop.setVisibility(View.VISIBLE);
            allVideoPlayerBinding.rlIcons.setVisibility(View.VISIBLE);
        }
    }

    private class MyReceiver1 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            allVideoPlayerBinding.rlTop.setVisibility(View.GONE);
            allVideoPlayerBinding.rlIcons.setVisibility(View.GONE);
        }
    }

    private class ForwardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (position != mainList.size() - 1) {
                position = position + 1;
                allVideoPlayerBinding.videoView.setVideoPath(mainList.get(position).getBucketPath());

                //config player
                allVideoPlayerBinding.videoView.getVideoInfo().setBgColor(Color.BLACK);

                allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());
                allVideoPlayerBinding.videoView.getPlayer().start();
            } else {
                onBackPressed();
            }
        }
    }

    private class BackwardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (position >= 1) {
                position = position - 1;
                allVideoPlayerBinding.videoView.setVideoPath(mainList.get(position).getBucketPath());

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(mainList.get(position).getBucketPath());
                int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                if (height < width) {
                    allVideoPlayerBinding.videoView.getVideoInfo().setBgColor(Color.BLACK).setAspectRatio(VideoInfo.AR_ASPECT_WRAP_CONTENT);//config player
                } else {
                    allVideoPlayerBinding.videoView.getVideoInfo().setBgColor(Color.BLACK).setAspectRatio(VideoInfo.AR_MATCH_PARENT);//config player
                }

                allVideoPlayerBinding.tvVidName.setText(mainList.get(position).getName());
                allVideoPlayerBinding.videoView.getPlayer().start();
            } else {
                onBackPressed();
            }
        }
    }

}