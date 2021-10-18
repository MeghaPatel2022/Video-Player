package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.activies;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    static final String TAG = BaseActivity.class.getName();
    private static final int RC_READ_EXTERNAL_STORAGE = 123;
    public static boolean isOpen = false;
    String[] perms1 = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    AppSettingsDialog appSettingsDialog;

    public abstract void permissionGranted();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        readExternalStorage();
        ActivityCompat.requestPermissions(this,
                perms1, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isGranted = EasyPermissions.hasPermissions(this, perms1);
        if (isGranted)
            permissionGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private void readExternalStorage() {
        boolean isGranted = EasyPermissions.hasPermissions(this, perms1);
        if (isGranted) {
            permissionGranted();
        } else {
            isOpen = true;
            EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, perms1);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            appSettingsDialog = new AppSettingsDialog.Builder(this).build();
            appSettingsDialog.show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, perms1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, perms1)) {
                permissionGranted();
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                        RC_READ_EXTERNAL_STORAGE, perms1);
            }
        } else if (requestCode == RC_READ_EXTERNAL_STORAGE) {
            boolean isGranted = EasyPermissions.hasPermissions(this, perms1);
            if (!isGranted) {
                EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                        RC_READ_EXTERNAL_STORAGE, perms1);
            }
        }
    }

}
