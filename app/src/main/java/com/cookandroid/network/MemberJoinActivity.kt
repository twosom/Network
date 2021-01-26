package com.cookandroid.network

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.cookandroid.network.R
import kotlinx.android.synthetic.main.activity_member_join.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MemberJoinActivity : AppCompatActivity() {

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            val text = msg.obj as String

            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
            Log.e("text", text)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_join)

        join.setOnClickListener {
            val msg = Message()
            //클라이언트에서 할 수 있는 유효성 검사
            val email = emailinput.text.toString()
            val pw = pwinput.text.toString()
            val nickname = nicknameinput.text.toString()

            //email길이가 0인 경우 핸들러에게 메시지 전송
            if (email.trim().length == 0) {
                msg.obj = "이메일은 필수 입력입니다."
                handler.sendMessage(msg)
                return@setOnClickListener
            } else {
                //형식 검사
                //정규식 문자열 생성
                val regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+\$"
                //정규식 생성
                val p: Pattern = Pattern.compile(regex)
                //입력한 내용이 정규식을 포함하는 지 확인
                val m: Matcher = p.matcher(email)
                //정규식을 만족하지 못하면
                if (m.matches() == false) {
                    msg.obj = "이메일은 패턴이 아닙니다"
                    handler.sendMessage(msg)
                    return@setOnClickListener
                }

            }
            //비밀번호 유효성 검사
            if (pw.trim().length == 0) {
                msg.obj = "잘못된 비밀번호 입니다."
                handler.sendMessage(msg)
                return@setOnClickListener
            } else {
                val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\$@\$!%*?&])[A-Za-z\\d\$@\$!%*?&]{8,}"

                val p: Pattern = Pattern.compile(regex)
                val m: Matcher = p.matcher(pw)

                if (m.matches() == false) {
                    msg.obj = "비밀번호는 영문 대소문자 1개 이상 특수문자 그리고 숫자를 1개 이상 포함해야 됩니다."
                    handler.sendMessage(msg)
                    return@setOnClickListener
                }

                //별명 유효성 검사
                if (nickname.trim().length < 2) {
                    msg.obj = "닉네임은 2자 이상이어야 합니다."
                    handler.sendMessage(msg)
                    return@setOnClickListener
                } else {
                    val regex = "[0-9a-zA-Z가-힣]"

                    var i = 0
                    while (i < nickname.length) {
                        //첫 번째 글자 가져오기
                        //글자마다 정규식에 포함되는지 확인
                        val ch = nickname[i].toString() + ""
                        val p = Pattern.compile(regex)
                        var m: Matcher = p.matcher(ch)
                        if (m.matches() == false) {
                            msg.obj = "별명은 영문과 숫자 그리고 한글로만 만들어야 합니다."
                            handler.sendMessage(msg)
                            return@setOnClickListener
                        }
                        i += 1
                    }
                }
//                msg.obj = "가입처리중입니다."
//                handler.sendMessage(msg)

            }
            //서버에게 요청을 전송하고 응답을 받아오는 스레드
            object : Thread() {
                override fun run() {

                    val addr = "http://cyberadam.cafe24.com/member/join"
                    val url = URL(addr)
                    //HttpURLConnection을
                    val con = url.openConnection() as HttpURLConnection

                    //파라미터 이름 만들기 - 파일은 빼고
                    val dataName = arrayOf("email", "pw", "nickname")
                    val data = arrayOf(email, pw, nickname)

                    //각 데이터를 구분하기 위해서 사용할 기호를 생성
                    val lineEnd = "\r\n"
                    val boundary = UUID.randomUUID().toString()
                    Log.e("boundary", boundary)

                    //옵션 설정
                    con.connectTimeout = 30000
                    con.useCaches = false
                    con.requestMethod = "POST"

                    //파일 업로드가 있는 경우 프로퍼티 설정
                    con.setRequestProperty("ENCTYPE", "multipart/form-data")
                    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=${boundary}")

                    //파일을 제외한 파라미터를 하나로 만들기
                    val delimiter = "--${boundary}${lineEnd}"
                    val postDataBuilder = StringBuffer()

                    var i = 0
                    while (i < data.size) {
                        postDataBuilder.append(delimiter)
                        postDataBuilder.append(
                            "Content-Disposition: form-data; name=\"${dataName[i]}\"${lineEnd}${lineEnd}${data[i]}${lineEnd}")
                        i += 1
                    }

                    //파일 파라미터 생성
                    val fileName = "duck.png"
                    if (fileName != null) {
                        postDataBuilder.append(delimiter)
                        postDataBuilder.append(
                            "Content-Disposition: form-data; name=\"profile\";filename=\"${fileName}\"${lineEnd}")
                    }

                    //파라미터 전송
                    var ds = DataOutputStream(con.outputStream)
                    ds.write(postDataBuilder.toString().toByteArray())

                    //파일 업로드
                    if (fileName != null) {
                        ds.writeBytes(lineEnd)

                        //파일의 내용을 Byte 배열로 생성
                        val fres = resources.openRawResource(R.raw.duck)
                        val buffer = ByteArray(fres.available())
                        var length = -1

                        //파일 전송
                        while (fres.read(buffer).also({ length = it }) != -1) {
                            ds.write(buffer, 0, length)

                        }
                        ds.writeBytes(lineEnd)
                        ds.writeBytes(lineEnd)

                        ds.writeBytes("--${boundary}--${lineEnd}")
                        fres.close()
                    } else {
                        ds.writeBytes(lineEnd)
                        ds.writeBytes("--${boundary}--${lineEnd}")
                    }
                    ds.flush()
                    ds.close()

                    //응답 받기
                    val sb = StringBuilder()
                    val br = BufferedReader(InputStreamReader(con.inputStream))

                    while (true) {
                        val line = br.readLine()
                        if (line == null) {
                            break
                        }
                        sb.append(line)
                    }
                    br.close()
                    con.disconnect()

                    Log.e("result", sb.toString())
                    //result : 가입 성공 여부
                    //emailcheck : 이메일 중복 검사 통과 여부

                    if (TextUtils.isEmpty(sb.toString())) {
                        msg.obj = "네트워크 사정이 좋지 안흥 것 같습니다. 잠시 후에 다시 접속해주세요."
                        handler.sendMessage(msg)
                        return
                    } else {
                        //JSON Parsing
                        val root = JSONObject(sb.toString())
                        val result = root.getBoolean("result")
                        if (result == true) {
                            msg.obj = "중복 검사가 통과되었습니다."
                        } else {
                            val emailcheck = root.getBoolean("emailcheck")
                            if (emailcheck == true) {
                                msg.obj = "이메일이 중복되었습니다."
                            } else {
                                msg.obj = "별명이 중복이라서 가입 실패"
                            }
                        }
                        handler.sendMessage(msg)
                    }


                }
            }.start()


            //서버에게 전송


            //응답을 받아서 처리
        }


    }
}