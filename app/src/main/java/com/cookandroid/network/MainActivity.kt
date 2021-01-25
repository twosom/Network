package com.cookandroid.network

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var sortType: String = "accuracy"

        btnSort.setOnClickListener {
            var sortArray = arrayOf("정확도순", "발간인순")

            AlertDialog.Builder(this)
                .setTitle("정렬 방식 선택")
                .setItems(sortArray) { dialog, which ->
                    when (which) {
                        0 -> sortType = "accuracy"
                        1 -> sortType = "latest"
                    }
                    btnSort.setText(sortArray[which])
                }
                .show()
            Log.e("sortType", sortType)
        }



        btnKakaoOpenAPI.setOnClickListener {

            if (TextUtils.isEmpty(searchText.text)) {
                AlertDialog.Builder(this)
                    .setTitle("오류")
                    .setMessage("검색어 입력은 필수입니다")
                    .setPositiveButton("확인", null)
                    .show()
                return@setOnClickListener
            } else {
                var addr: String = "https://dapi.kakao.com/v3/search/book?"
                var sort: String = "sort=" + sortType
                var query: String = "query=" + URLEncoder.encode(searchText.text.toString().trim(), "utf-8")
                var pageCount: String = "page=" + pageNumber.text.toString().trim()
                var documentCount: String = "size=" + documnetCount.text.toString().trim()



                addr = addr + query + "&" + sort + "&" + pageCount + "&" + documentCount
                Log.e("addr", addr)


                val intent: Intent = Intent(this, KakaoOpenAPIActivity::class.java)
                intent.putExtra("addr", addr)
                startActivity(intent)
            }


        }

        var itemNumber: String = ""

        selectItem.setOnClickListener {
            var itemNumberArray = arrayOf("1", "2", "3", "4", "5", "6", "7")
            AlertDialog.Builder(this)
                .setTitle("아이템 번호를 선택해주세요")
                .setItems(itemNumberArray) { dialog, which ->
                    val number = which + 1
                    itemNumber = number.toString()
//                    when (which) {
//                        0 -> itemNumber = "1"
//                        1 -> itemNumber = "2"
//                        2 -> itemNumber = "3"
//                        3 -> itemNumber = "4"
//                        4 -> itemNumber = "5"
//                        5 -> itemNumber = "6"
//                        6 -> itemNumber = "7"
//                    }
                    selectItem.setText(itemNumberArray[which])
                }
                .show()
        }


        btnItemDetail.setOnClickListener {
            if (itemNumber == "") {
                Toast.makeText(applicationContext, "아이템 번호를 선택해주셔야 합니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent: Intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("itemId", itemNumber)
            startActivity(intent)
        }


        btnItemInsert.setOnClickListener {
            val intent: Intent = Intent(this, ItemInsertActivity::class.java)
            startActivity(intent)
        }


    }
}