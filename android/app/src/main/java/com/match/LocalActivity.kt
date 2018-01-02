package com.match

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_local.*
import java.io.*
import java.util.*
import java.util.regex.Pattern

class LocalActivity : AppCompatActivity() {

    private var temps: MutableList<String> = ArrayList()
    lateinit var keyWord: EditText
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    lateinit var readFileTime: TextView
    lateinit var matchTime: TextView
    var lastTme: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local)
        ActivityCompat.requestPermissions(this, permissions, 100)
        keyWord = findViewById(R.id.et_key)
        readFileTime = findViewById(R.id.tv_read_from_store)
        matchTime = findViewById(R.id.tv_match_time)
    }

    fun onLocalSearch(view: View) {
        val path = "${Config.ROOT_PATH}/${et_file_name.text.toString()}"
        if (!File(path).exists() || !path.endsWith("txt")) {
            Toast.makeText(this, "文件路径不合法", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(et_key.text.toString())) {
            Toast.makeText(this, "检索内容不合法", Toast.LENGTH_SHORT).show()
            return
        }
        matchFile(et_file_name.text.toString())
        val keyword = keyWord.text.toString()
        val p = Pattern.compile(keyword)
        var i = 0
        Thread({
            lastTme = Date().time
            temps.forEach {
                val m = p.matcher(it)
                while (m.find()) {
                    i++
                }
            }
            runOnUiThread {
                matchTime.text = "匹配" + i + "个   " + "匹配耗时:" + (Date().time - lastTme!!) + "毫秒"
            }
        }).start()
    }

    private fun matchFile(fileName: String) {
        lastTme = Date().time
        try {
            val fin = FileInputStream(File("${Config.ROOT_PATH}/$fileName"))
            val reader = BufferedReader(InputStreamReader(fin, "UTF-8"))
            var line: String? = reader.readLine()
            while (line != null) {
                temps.add(line)
                line = reader.readLine()
            }
            reader.close()
            readFileTime.text = "读取文件耗时:" + (Date().time - lastTme!!) + "毫秒"
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
