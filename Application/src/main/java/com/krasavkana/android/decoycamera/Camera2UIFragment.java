package com.krasavkana.android.decoycamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera2UIFragment extends Camera2BasicFragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2UIFragment";

    SharedPreferences mPref;

    /**
     * LENS Facing button is visible
     * Button to lens facing
     */
    private ImageButton mButtonLensFacing;

    /**
     * Shoot button is visible
     * Button to shoot
     */
    private ImageButton mButtonShoot;

    /**
     * Save button is visible while saving image file
     */
//    protected ImageButton mButtonSave;

    /**
     * bleCommand from Intent
     */
    private static String mBleCommand;

    public static Camera2UIFragment newInstance(String bleCommand) {
        mBleCommand = bleCommand;
        Log.d(TAG, "mBleCommand: " + mBleCommand);
        return new Camera2UIFragment();
    }


    /**
     * View.OnKeyListenerを設定する
     * http://outofmem.hatenablog.com/entry/2014/04/20/090047
     * https://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        final View v = inflater.inflate(R.layout.fragment_camera2_basic, container, false);

        // View#setFocusableInTouchModeでtrueをセットしておくこと
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // KeyEvent.ACTION_DOWN以外のイベントを無視する
                // （これがないとKeyEvent.ACTION_UPもフックしてしまう）
                Log.d(TAG, "onKey()");
                if(event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                switch(keyCode) {
//                    case KeyEvent.KEYCODE_VOLUME_UP:
//                        // TODO:音量増加キーが押された時のイベント
//                        return true;
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        // TODO:音量減少キーが押された時のイベント
                        takePicture();
                        return true;
                    default:
                        return false;
                }
            }
        });

        mHandler = new Handler();

        return v;
    }

    // Does setWidth(int pixels) use dip or px?
    // https://stackoverflow.com/questions/2406449/does-setwidthint-pixels-use-dip-or-px
    // value in DP
    private static int getValueInDP(Context context, int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    private static float getValueInDP(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    // value in PX
    private static int getValueInPixel(Context context, int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, context.getResources().getDisplayMetrics());
    }

    private static float getValueInPixel(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        super.onViewCreated(view,savedInstanceState);

        mButtonShoot = view.findViewById(R.id.picture);
        mButtonShoot.setOnClickListener(this);

        mButtonLensFacing = view.findViewById(R.id.info);
        mButtonLensFacing.setOnClickListener(this);

        mButtonSave = view.findViewById(R.id.save);
        mButtonSave.setVisibility(View.INVISIBLE);

        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);

        if (mBleCommand != null) {
            Log.d(TAG, "takePicture() will be done by bleCommand");
            mState = STATE_WAITING_NON_PRECAPTURE;
//            mState = STATE_WAITING_LOCK;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String aaa = mPref.getString("preference_theme", "");
        Log.d(TAG, "preference_theme:" + aaa);
        mLensFacingFront = mPref.getBoolean("preference_front_lens_facing", false);
        Log.d(TAG, "mLensFacingFront:" + mLensFacingFront);
        mPrefix = mPref.getString("preference_save_prefix", "");
        Log.d(TAG, "mPrefix:" + mPrefix);

        // ファインダの表示場所と大きさを変更する
        String finderLocation = mPref.getString("preference_finder_location", "ML");
        Log.d(TAG, "finderLocation:" + finderLocation);
        String finderSize = mPref.getString("preference_finder_size", "40x60");
        Log.d(TAG, "finderSize:" + finderSize);
        // ファインダの大きさを設定する
        int finderWidth = Integer.parseInt(finderSize.substring(0,finderSize.indexOf('x')));
        Log.d(TAG, "finderWidth:" + finderWidth);
        int finderHeight = Integer.parseInt(finderSize.substring(finderSize.indexOf('x')+1,finderSize.length()));
        Log.d(TAG, "finderHeight:" + finderHeight);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                getValueInDP(getContext(),40),getValueInDP(getContext(),60)
                getValueInDP(getContext(),finderWidth),getValueInDP(getContext(),finderHeight)
        );
        // ファインダの場所を設定する
        int M10DP = getValueInDP(getContext(),10);
        switch(finderLocation){
            case "TL":
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                lp.setMargins(M10DP,M10DP,0,0);
                break;
            case "TR":
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                lp.setMargins(0,M10DP,M10DP,0);
                break;
            case "ML":
                lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                lp.setMarginStart(M10DP);
                break;
            case "BL":
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                lp.setMargins(M10DP,0,0,M10DP);
                break;
            case "BR":
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                lp.setMargins(0,0,M10DP,M10DP);
                break;
        }
        mTextureView.setLayoutParams(lp);

        // 撮影ボタンの表示ONOFF
        boolean buttonShootOn = mPref.getBoolean("preference_shoot_button_on", true);
        Log.d(TAG, "buttonShootOn:" + buttonShootOn);
        if(buttonShootOn) {
            mButtonShoot.setVisibility(View.VISIBLE);
        }else{
            mButtonShoot.setVisibility(View.INVISIBLE);
        }

        // カメラ切り替えボタンの表示ONOFF
        boolean buttonLensFacingOn = mPref.getBoolean("preference_lens_facing_button_on", true);
        Log.d(TAG, "buttonLensFacingOn:" + buttonLensFacingOn);
        if(buttonLensFacingOn) {
            mButtonLensFacing.setVisibility(View.VISIBLE);
        }else{
            mButtonLensFacing.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: {
                takePicture();
                break;
            }
            case R.id.info: {
                if (mLensFacingFront){
                    mLensFacingFront = false;
                }else{
                    mLensFacingFront = true;
                }
                onPause();
                onResume();
                break;
            }
        }
    }

//    /**
//     * 現在日時をyyyyMMddTHHmmssSSS形式で取得する.<br>
//     */
//    public static String getNowTimestamp(){
//        final DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
//        final Date date = new Date(System.currentTimeMillis());
//        return df.format(date);
//    }

}
