package com.match

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_server.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

class ServerActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        ActivityCompat.requestPermissions(this, permissions, 100)
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
                        parseResult(response?.body()?.string()!!)
                    }
                })
    }

    fun parseResult(result: String) {
        val result: SearchResult = Gson().fromJson(result, object : TypeToken<SearchResult>() {}.type)
        when (result.code) {
            200 -> {
                tv_server_tips.text = "计算完成"
                tv_server_search_time.text = "${result.data.time.toString()} ms"
                tv_server_upload_time.text = "${result.data.upload_time.toString()} ms"
                tv_server_key_count.text = result.data.count.toString()
            }
        }
    }
}

data class SearchResult(val code: Int, val data: SearchItem)
data class SearchItem(val count: Int, val keyword: String, val time: Int, val upload_time: Int)

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