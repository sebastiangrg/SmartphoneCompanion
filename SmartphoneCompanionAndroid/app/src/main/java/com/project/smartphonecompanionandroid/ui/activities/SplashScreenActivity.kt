package com.project.smartphonecompanionandroid.ui.activities

import android.os.Bundle
import android.content.Intent
import android.view.WindowManager
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(com.project.smartphonecompanionandroid.R.layout.activity_splash_screen)

        Handler().postDelayed({
            val i = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_SCREEN_TIME_OUT)
    }

    companion object {
        const val SPLASH_SCREEN_TIME_OUT = 2000L
    }
}
