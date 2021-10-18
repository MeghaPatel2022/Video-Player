package tcking.github.com.giraffeplayer2.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;

public class MyService extends Service implements FloatingViewListener {
    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
/*
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }


        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        floatingServiceBinder = new FloatingServiceBinder(this);
        final View rootView = LayoutInflater.from(this).inflate(R.layout.view_exoplayer, null, false);
        final VideoView videoView = (VideoView) rootView.findViewById(R.id.video_view);
        videoView.setVideoURI(Uri.parse(MY_URL));
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoView.start();
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_play_circle_filled);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_menu);
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        mFloatingViewManager.addViewToWindow(rootView, options);*/

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onFinishFloatingView() {

    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }
}