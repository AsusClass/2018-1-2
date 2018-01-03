package com.match

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_server.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

class ServerActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var adapter: LocalDataAdapter? = null
    var total_time: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        ActivityCompat.requestPermissions(this, permissions, 100)
        rcv.layoutManager = LinearLayoutManager(this)
        adapter = LocalDataAdapter()
        rcv.adapter = adapter
    }

    fun onRemoteSearch(view: View) {
        val path = "${Config.ROOT_PATH}/${et_server_file_name.text.toString()}"
        if (!File(path).exists() || !path.endsWith("txt")) {
            Toast.makeText(this, "文件路径不合法", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(et_server_key.text.toString())) {
            Toast.makeText(this, "检索内容不合法", Toast.LENGTH_SHORT).show()
            return
        }
        btn_server_search.isClickable = false
        tv_server_tips.text = "正在上传文件..."
        ServerRequest.instance.getService("http://${et_server_ip_address.text.toString()}").getRemoteSearch(RequestBody.create(MediaType.parse("multipart/form-data"), "file"),
                MultipartBody.Part.createFormData("file", et_server_file_name.text.toString(), RequestBody.create(MediaType.parse("multipart/form-data"), File(path)))).
                enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        tv_server_tips.text = "服务器访问失败,ip地址:${et_server_ip_address.text.toString()}"
                        btn_server_search.isClickable = true
                    }

                    override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                        btn_server_search.isClickable = true
                        parseResult(response?.body()?.string())
                    }
                })
    }

    fun parseResult(result: String?) {
        try {
            val result: SearchResult = Gson().fromJson(result, object : TypeToken<SearchResult>() {}.type)
            when (result.code) {
                200 -> {
                    tv_server_tips.text = "计算完成"
                    total_time += (result.data.upload_time.toDouble() * 1000000).toLong()
                    total_time += (result.data.time.toDouble() * 1000000).toLong()
                    adapter?.refresh(LocalData((result.data.upload_time.toDouble() * 1000000).toLong(), result.data.count.toLong(), (result.data.time.toDouble() * 1000000).toLong()))
                    tv_server_key_count.text = "当前次数:${adapter?.itemCount}\t\t累积时间:${total_time / 1000000f}\t\t平均时间:${total_time / (adapter?.itemCount!! * 1000000f)}"
                }
            }
        } catch (e: Exception) {
            tv_server_tips.text = "服务器返回数据:${result}"
        }
    }
}

data class SearchResult(val code: Int, val data: SearchItem)
data class SearchItem(val count: Int, val keyword: String, val time: Double, val upload_time: Double)

interface ServiceApi {
    @Multipart
    @POST("/remote")
    fun getRemoteSearch(@Part("description") description: RequestBody, @Part file: MultipartBody.Part): Call<ResponseBody>
}

class ServerRequest private constructor() {
    private var retrofit: Retrofit? = null

    fun getService(host: String): ServiceApi {
        if (retrofit == null)
            retrofit = Retrofit.Builder().client(OkHttpClient()).baseUrl(host).addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit!!.create(ServiceApi::class.java)
    }

    companion object {
        val instance = ServerRequest()
    }

}