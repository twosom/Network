package com.cookandroid.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_item_detail.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ItemDetailActivity : AppCompatActivity() {

    //조회할 ItemID를 저장할 프로퍼티


    //파싱한 결과를 저장하기 위한 Map
    var map: MutableMap<String, Any>? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    //전송된 데이터 읽기
                    map = msg.obj as MutableMap<String, Any>
                    itemname.setText(map!!["itemname"] as String?)
                    description.setText(map!!["description"] as String?)
                    price.setText("${map!!["price"] as Int?}")

                } else if (msg.what == 2) {
                    //핸들러가 전송해준 데이터를 Bitmap으로 변환
                    val bitmap = msg.obj as Bitmap
                    Log.e("bitmap", bitmap.toString())
                    picture.setImageBitmap(bitmap)
                }
            }
        }

        //코틀린에서는 private 으로 접근지정자를 지정해야
        //클래스 내의 모든 곳에서 사용 가능
        class ThreadEx : Thread() {
            override fun run() {
                val pictureurl = map!!["pictureurl"] as String?
                val url = URL("http://cyberadam.cafe24.com/img/$pictureurl")
                val inputStream = url.openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val msg = Message()
                msg.obj = bitmap
                msg.what = 2
                handler.sendMessage(msg)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        object : Thread() {
            override fun run() {
                val itemId = intent.getStringExtra("itemId")
                var url: URL = URL("http://cyberadam.cafe24.com/item/detail?itemid=${itemId}")

                val con = url.openConnection() as HttpURLConnection

                con.connectTimeout = 30000
                con.useCaches = false
                con.requestMethod = "GET"

                //문자열 다운로드 받기
                val sb = StringBuilder()
                val br = BufferedReader(InputStreamReader(con.inputStream))

                while (true) {
                    val line = br.readLine()
                    if (line == null) {
                        break
                    }
                    sb.append(line)
                }
                //정리
                br.close()
                con.disconnect()
                if (TextUtils.isEmpty(sb.toString())) {
                    Toast.makeText(applicationContext, "네트워크 오류입니다.", Toast.LENGTH_SHORT).show()
                }
                val data = JSONObject(sb.toString())
                Log.e("Data", data.toString())
                if (TextUtils.isEmpty(data.toString())) {
                    Toast.makeText(this@ItemDetailActivity, "데이터를 받아오지 못했습니다. 네트워크를 확인해주세요", Toast.LENGTH_LONG).show()
                    return
                } else {
                    //데이터를 파싱
                    //결과에서 result 값을 가져오면 데이터가 전소오디었는지 아닌지 확인 가능
                    //true이면 item 키에 데이터가 있는 것이고 false 이면 없는 것.
                    val result = data.getBoolean("result")
                    if (result == false) {
                        Toast.makeText(this@ItemDetailActivity, "itemid가 잘못되었습니다", Toast.LENGTH_LONG).show()
                    } else {
                        val item = data.getJSONObject("item")
                        //데이터를 저장할 Map을 생성
                        map = mutableMapOf<String, Any>()
                        map?.put("itemname", item.getString("itemname"))
                        map?.put("price", item.getInt("price"))
                        map?.put("description", item.getString("description"))
                        map?.put("pictureurl", item.getString("pictureurl"))


                        //핸들러에 메시지 저장
                        //구분하기 위한 번호를 1을 저장
                        val msg = Message()
                        msg.obj = map
                        msg.what = 1
                        //핸들러에게 메시지 전송
                        handler.sendMessage(msg)
                        ThreadEx().start()
                    }
                }
            }
        }.start()
    }
}