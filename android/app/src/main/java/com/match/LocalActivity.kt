package com.match

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_local.*
import java.io.*
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Pattern

class LocalActivity : AppCompatActivity() {

    lateinit var keyWord: EditText
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    lateinit var matchTime: TextView
    var lastTme: Long = 0
    var temp_str: StringBuilder = StringBuilder()
    var adapter: LocalDataAdapter? = null

    var s_st_time: Long = 0
    var s_ma_time: Long = 0
    var s_ma_num: Long = 0
    var total_time: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local)
        ActivityCompat.requestPermissions(this, permissions, 100)
        keyWord = findViewById(R.id.et_key)
        matchTime = findViewById(R.id.tv_match_time)
        rcv.layoutManager = LinearLayoutManager(this)
        adapter = LocalDataAdapter()
        rcv.adapter = adapter
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
        btn_local_search.isClickable = false
        matchFile(et_file_name.text.toString())
        val keyword = keyWord.text.toString()
        val p = Pattern.compile(keyword)
        var i = 0
        lastTme = System.nanoTime()
        s_ma_time = 0
        s_ma_num = 0
        val m = p.matcher(temp_str)
        while (m.find()) {
            i++
        }
        s_ma_num = i.toLong()
        s_ma_time = (System.nanoTime() - lastTme!!).toLong()
        total_time += s_ma_time
        total_time += s_st_time
        matchTime.text = "匹配" + s_ma_num + "个   " + "匹配耗时:" + s_ma_time + "毫秒"
        adapter?.refresh(LocalData(s_st_time, s_ma_num, s_ma_time))
        tv_match_time.text = "当前次数:${adapter?.itemCount}\t\t累积时间:${total_time / 1000000f}\t\t平均时间:${total_time / 1000000f / adapter?.itemCount!!}"

        btn_local_search.isClickable = true
    }

    private fun matchFile(fileName: String) {
        lastTme = System.nanoTime()
        s_st_time = 0
        temp_str.setLength(0)
        try {
            val fin = FileInputStream(File("${Config.ROOT_PATH}/$fileName"))
            val reader = BufferedReader(InputStreamReader(fin, "UTF-8"))
            var line: String? = reader.readLine()
            while (line != null) {
                temp_str.append(line)
                line = reader.readLine()
            }
            reader.close()
            s_st_time = (System.nanoTime() - lastTme!!).toLong()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
