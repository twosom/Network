package com.cookandroid.network

import android.content.Context
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_member_login.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MemberLoginActivity : AppCompatActivity() {
    //로그인 정보를 저장할 프로프티
    var profileUrl: String? = null
    var email: String? = null
    var nickname: String? = null

    var regdate: String? = null


    //데이터를 출력할 핸들러
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                val str = msg.obj as String
                Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
            }else if (msg.what == 2) {
                val str = msg.obj as String
                Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()

            }else if (msg.what == 3) {
                val str = msg.obj as String
                Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
            }else if (msg.what == 4) {
                val image = msg.obj as Bitmap
                //이미지 뷰에 출력
                profileimage.setImageBitmap(image)
            }

            //로그인 성공했을 때 이미지 출력
            val imageDownloadThread = ImageDownloadThread()
            imageDownloadThread.start()


        }
    }

    //로그인을 하고 결과를 해석하는 스레드
    inner class LoginThread : Thread() {
        override fun run() {
            //url을 생성하고 연결 옵션 설정
            val url = URL("http://cyberadam.cafe24.com/member/login")
            val con = url.openConnection() as HttpURLConnection

            con.connectTimeout = 30000
            con.useCaches = false
            con.requestMethod = "POST"

            //email 과 pw에 해당하는 파라미터 생성 및 전송
            var data =
                URLEncoder.encode("email", "UTF-8").toString() + "=" + URLEncoder.encode(emailinput.text.toString()
                    .trim(),
                    "UTF-8") + "&" + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(pwinput.text.toString()
                    .trim(), "UTF-8")
            Log.e("data", data)

            //서버와의 출력 스트림을 생성
            val wr = OutputStreamWriter(con.outputStream)
            //데이터 전송
            wr.write(data)
            //버퍼의 내용을 비움 - 실제 전송
            wr.flush()

            //응답 결과 확인
            var br = BufferedReader(InputStreamReader(con.inputStream))
            var sb = StringBuilder()

            while (true) {
                val line = br.readLine()
                if (line == null) {
                    break
                }
                sb.append(line)
            }

            br.close()
            con.disconnect()

            Log.e("응답 결과", sb.toString())

            val msg = Message()

            if (TextUtils.isEmpty(sb.toString())) {
                msg.what = 1
                msg.obj = "네트워크가 불안정합니다."
            } else {
                //응답결과 파싱
                var data = JSONObject(sb.toString())
                val login = data.getBoolean("login")
                if (login == false) {
                    //로그인 실패
                    msg.what = 2
                    msg.obj = "없는 이메일이거나 잘못된 비밀번호입니다."
                } else {
                    //로그인 성공
                    msg.what = 3
                    msg.obj = "로그인 성공"
                    email = data.getString("email")
                    nickname = data.getString("nickname")
                    profileUrl = data.getString("profile")
                    regdate = data.getLong("regdate").toString()

                    //파일에 저장
                    val fos = openFileOutput("login.txt", Context.MODE_PRIVATE)
                    //기록할 문자열 생성 후 기록
                    val str = "${email}:${nickname}:${profileUrl}:${regdate}"
                    fos.write(str.toByteArray())
                    fos.close()
                }
            }

            //핸들러에게 메시지 전송
            handler.sendMessage(msg)


        }
    }

    inner class ImageDownloadThread : Thread() {
        override fun run() {
            //이미지를 다운로드 받기 위한 스트림을 생성
            val inputStream = URL("http://cyberadam.cafe24.com/profile/${profileUrl}")
                .openStream()
            //bitmap으로 데이터 가져오기
            val bit: Bitmap = BitmapFactory.decodeStream(inputStream)

            //메세지에 담아서 전송
            val msg = Message()
            msg.obj = bit
            msg.what=4
            handler.sendMessage(msg)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_login)

        //이전에 로그인 한 내용이 남아있는지 확인
        try {
            val openFileInput = openFileInput("login.txt")

            Log.e("로그인 내용", "있음")
        } catch (e: Exception) {
            Log.e("로그인 내용", "없음")
        }

        //로그인 버튼 클릭 이벤트 처리
        btnlogin.setOnClickListener {
            //유효성 검사 해야 함 - 이전꺼에서 복사

            //로그인 시도하는 스레드를 생성해서 시작
            val th = LoginThread()
            th.start()

        }

        //로그아웃 버튼 클릭 이벤트 처리
        btnlogout.setOnClickListener{
            //로그인 정보를 저장한 파일을 삭제
            var msg: String? = null
            if (deleteFile("login.txt")) {
                msg = "로그아웃 하셨습니다."
            } else {
                msg = "로그인 상태가 아닙니다."
            }
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

        }

    }
}