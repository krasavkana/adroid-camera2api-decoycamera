package com.krasavkana.android.decoycamera;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity
    implements Camera2UIFragment.Camera2UIFragmentCallback {

    private File[] mFiles;
    private int mFileNum = 0;
    private int mFilePos = -1;
    private ImageView mImageView;
//    private boolean isFinished = false;

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "CameraActivity";

    private static final String CAMOUFLAGE_MODE = "imageview";

    private final static String MASTER_MUTE_PACKAGE_NAME = "com.krasavkana.android.mastermute";
    private final static String MASTER_MUTE_CLASS_NAME = "com.krasavkana.android.mastermute.MainActivity";
    private static final int REQUEST_CODE_MASTER_MUTE = 1;

    SharedPreferences mPref;
    boolean mCamouflageImageview;
    boolean mMasterMuteOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //
        //ActionModeを入れたのでActionBarの実装コードを削除する。
        //menuの設定とLayout定義を変更（原則削除）した
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar ab = getSupportActionBar();
//        ab.setTitle(R.string.shortAppName);
        if (null == savedInstanceState) {
            Intent intent = getIntent();
            String bleCommand;
            if (intent != null){
                bleCommand = intent.getStringExtra("bleCommand");
            }else{
                bleCommand = null;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2UIFragment.newInstance(bleCommand))
                    .commit();
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        String camouflage = mPref.getString("preference_camouflage", "");
        Log.d(TAG, "mCamouflage:" + camouflage);
        if(CAMOUFLAGE_MODE.equals(camouflage)) {
            mCamouflageImageview = true;

            // get file list from the external storage path

            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
            if (path == null) {
                Toast.makeText(this, "Check external file dir.", Toast.LENGTH_LONG).show();
                finish();
            }
            mFiles = new File(path).listFiles();
            mFileNum = mFiles.length;
            if (mFileNum == 0) {
                Toast.makeText(this, "Camouflage images not found.", Toast.LENGTH_LONG).show();
                finish();
            }
            Log.d(TAG, "Num of image files: " + mFileNum);
            if (savedInstanceState != null) {
                mFilePos = -1;
            } else {
                mFilePos = -1;
            }
            mImageView = findViewById(R.id.image_view);
        }

        mMasterMuteOn = mPref.getBoolean("preference_master_mute_on", false);
        Log.d(TAG, "mMasterMuteOn:" + (mMasterMuteOn?"true":"false"));
        if(mMasterMuteOn) {
            Intent intent = new Intent();
            intent.setClassName(MASTER_MUTE_PACKAGE_NAME, MASTER_MUTE_CLASS_NAME);

            try {
                startActivityForResult(intent, REQUEST_CODE_MASTER_MUTE);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "Intent to be invoked NOT found");
                Toast.makeText(this, "Intent to be invoked NOT found", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }
    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }
    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }
    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

//    // メニューをActivity上に設置する
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
//        getMenuInflater().inflate(R.menu.option, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    // メニューが選択されたときの処理
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menuItem1:
//                startActivity(new Intent(CameraActivity.this, SettingsActivity.class));
//                return true;
//
//            case R.id.menuItem2:
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    // startActivityForResult起動後に値が返されたときの処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (REQUEST_CODE_MASTER_MUTE):
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult():RESULT_OK");
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult():RESULT_CANCELED");
                    Toast.makeText(this, "MasterMute has to be ON, or Die", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    //その他
                }
                break;
            default:
                break;
        }
    }

    public void imageClickEvent(boolean increment) {
        if(! mCamouflageImageview) return;

        if(increment) {
            mFilePos++;
            if (mFilePos >= mFileNum) {
                mFilePos = 0;
            }
        }else{
            mFilePos--;
            if (mFilePos < 0) {
                mFilePos = mFileNum - 1;
            }
        }
        Log.d(TAG, "Current Pos in image list: " + mFilePos);
        try (InputStream inputStream0 =
                     new FileInputStream(mFiles[mFilePos])) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream0);
            mImageView.setImageBitmap(bitmap);
            mImageView.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
