package com.tg.unity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.tg.unitylibrary.MainUnityActivity

class MainActivity : AppCompatActivity() {
    var isUnityLoaded = false
    var button: TextView? = null
    var buttonFirst: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.init(this.application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button2_second)
        buttonFirst = findViewById(R.id.button_first)
        button?.setOnClickListener {
            btnUnloadUnity()
        }

        buttonFirst?.setOnClickListener {
            btnLoadUnity()
        }
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) return
        if (intent.extras!!.containsKey("setColor")) {
            val v: View = findViewById(R.id.button2_second)
            when (intent.extras!!.getString("setColor")) {
                "yellow" -> v.setBackgroundColor(Color.YELLOW)
                "red" -> v.setBackgroundColor(Color.RED)
                "blue" -> v.setBackgroundColor(Color.BLUE)
                else -> {
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    fun btnLoadUnity() {
        isUnityLoaded = true
        val intent  = Intent(this, MainUnityActivity::class.java)
        startActivity(intent)
    }

    fun unloadUnity(doShowToast: Boolean) {
        if (isUnityLoaded) {
            ARouter.getInstance().build("/unityGame/open").withBoolean("doQuit",true).greenChannel().navigation(this)
            isUnityLoaded = false
        } else if (doShowToast) showToast("Show Unity First")
    }

    fun btnUnloadUnity() {
        unloadUnity(true)
    }

    fun showToast(message: String) {
        val text: CharSequence = message
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}