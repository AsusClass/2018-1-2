package com.match

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFolder()
    }

    fun onLocal(view: View) {
        startActivity(Intent(this, LocalActivity::class.java))
    }

    fun onServer(view: View) {
        startActivity(Intent(this, ServerActivity::class.java))
    }
    private fun initFolder() {
        val file = File(Config.ROOT_PATH)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

}
