package jp.techacademy.youichi.okami.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import jp.techacademy.youichi.okami.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    private var cursor: Cursor? = null

    private var timer: Timer? = null

    private var handler = Handler(Looper.getMainLooper())

    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }

        // 各ボタンの処理実装
        // 進む
        val button1 = findViewById<Button>(R.id.button1)
        button1.setOnClickListener{
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                if (cursor!!.isLast) {
                    if (cursor!!.moveToFirst()) {
                        // indexからIDを取得し、そのIDから画像のURIを取得する
                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        val id = cursor!!.getLong(fieldIndex)
                        val imageUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        binding.imageView.setImageURI(imageUri)
                    }
                } else {
                    if (cursor!!.moveToNext()) {
                        // indexからIDを取得し、そのIDから画像のURIを取得する
                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        val id = cursor!!.getLong(fieldIndex)
                        val imageUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        binding.imageView.setImageURI(imageUri)
                    }
                }
            }
        }

        // 戻る
        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener{
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                if (cursor!!.isFirst) {
                    if (cursor!!.moveToLast()) {
                        // indexからIDを取得し、そのIDから画像のURIを取得する
                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        val id = cursor!!.getLong(fieldIndex)
                        val imageUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        binding.imageView.setImageURI(imageUri)
                    }
                } else {
                    if (cursor!!.moveToPrevious()) {
                        // indexからIDを取得し、そのIDから画像のURIを取得する
                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        val id = cursor!!.getLong(fieldIndex)
                        val imageUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        binding.imageView.setImageURI(imageUri)
                    }
                }
            }
        }

        // 再生/停止
        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener{
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // 進む、戻るボタン非活性
                val button1 = findViewById<Button>(R.id.button1)
                button1.isClickable = false

                val button2 = findViewById<Button>(R.id.button2)
                button2.isClickable = false

                if (timer == null) {
                    button3.setText("停止")
                    timer = Timer()
                    timer!!.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                // 最後まで来たらあたまから
                                if (cursor!!.isLast) {
                                    if (cursor!!.moveToFirst()) {
                                        // indexからIDを取得し、そのIDから画像のURIを取得する
                                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                                        val id = cursor!!.getLong(fieldIndex)
                                        val imageUri =
                                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                                        binding.imageView.setImageURI(imageUri)
                                    }
                                } else {
                                    if (cursor!!.moveToNext()) {
                                        // indexからIDを取得し、そのIDから画像のURIを取得する
                                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                                        val id = cursor!!.getLong(fieldIndex)
                                        val imageUri =
                                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                                        binding.imageView.setImageURI(imageUri)
                                    }
                                }
                                Log.d("ANDROID", "test")
                            }
                        }
                    }, 2000, 2000)
                } else {
                    button1.isClickable =  true
                    button2.isClickable = true
                    button3.setText("再生")
                    timer!!.cancel()
                    timer = null
                }
            }
        }
    }

    override fun onClick(v: View) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
         cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            binding.imageView.setImageURI(imageUri)
        }
    }
}