package com.wgf.happyfaceapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private val TAG = SplashActivity::class.simpleName

    var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null

    /**
     * 안드로이드 생명주기 첫 실행 함수
     * (1) onCreate() :
     *
     *      어플을 실행하면 가장 첫번째로 호출 되는 함수
     *      여기에서는 Firebase에서 원격으로 조정하기 위한 설정을 수행!
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        var splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_background))


        var configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig!!.setConfigSettings(configSettings)
        mFirebaseRemoteConfig!!.setDefaults(R.xml.remote_config_defaults)

        // 배경설정
        splash_linear_layout.setBackgroundColor(Color.parseColor(splash_background));

        mFirebaseRemoteConfig!!.fetch(0)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig!!.activateFetched()
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")

                } else {
                    Toast.makeText(this, "Fetch Failed", Toast.LENGTH_SHORT).show()
                }
                displayWelcomeMessage()
            }
    }

    /**
     * Firebase Remote Config 값에 따라서 다음 작업을 수행하는 함
     * (2) displayWelcomeMessage() :
     *
     *      Firebase Remote Config의 caps 값이 true(참) 이면 팝업창 표시 후 어플 종료.
     *      Firebase Remote Config의 caps 값이 false(거짓) 이면 다음 화면인 MainActivity로 전환!
     */
    fun displayWelcomeMessage() {
        var caps = mFirebaseRemoteConfig!!.getBoolean(getString(R.string.rc_caps))
        var splash_message = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_message))

        if(caps) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                .setMessage(splash_message)
                .setPositiveButton("확인") { dialog, which ->
                    finish()
                }
            alertDialog.create().show()

        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}