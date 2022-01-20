package com.hareshnayak.readme

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.hareshnayak.readme.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityMainBinding
    lateinit var bitmap : Bitmap

    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)

        binding.btnCapture.setOnClickListener {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)
        }

        binding.btnCopy.setOnClickListener {
            val scannedText: String = binding.textData.text.toString()
            copyToClipboard(scannedText)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val resultUri : Uri  = result.uri
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,resultUri)
                getTextFromImage(bitmap)
            }
        }
    }

    private fun getTextFromImage(bitmap : Bitmap){
        val recognizer : TextRecognizer = TextRecognizer.Builder(this).build()
        if(!recognizer.isOperational){
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
        }else{
            val frame: Frame = Frame.Builder().setBitmap(bitmap).build()
            val textBlockSparseArray : SparseArray<TextBlock> = recognizer.detect(frame)
            val stringBuilder = StringBuilder()
            val n = textBlockSparseArray.size() - 1
            for(i in 0..n) {
                val textBlock: TextBlock = textBlockSparseArray[i]
                stringBuilder.append(textBlock.value)
                stringBuilder.append("\n")
            }
            binding.textData.text = stringBuilder.toString()
            binding.btnCopy.text = "Copy"
            binding.btnCopy.visibility = View.VISIBLE
        }
    }

    private fun copyToClipboard(text: String){
        val clipboard : ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip : ClipData = ClipData.newPlainText("Copied Data", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }
}