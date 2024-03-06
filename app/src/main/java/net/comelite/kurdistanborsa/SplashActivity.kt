package net.comelite.kurdistanborsa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    lateinit var startMain: Runnable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()

        handler = Handler()
        startMain = Runnable {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
        handler.postDelayed(startMain, 3000)

    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(startMain)
    }
}
