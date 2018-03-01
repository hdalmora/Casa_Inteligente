package com.example.pichau.casainteligente

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.activity_launcher.*


class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        home_btn.setOnClickListener(View.OnClickListener {
            val i = Intent(this@LauncherActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        })
    }
}
