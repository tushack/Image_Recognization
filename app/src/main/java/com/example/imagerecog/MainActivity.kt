package com.example.imagerecog

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.imagerecog.ml.MobilenetV110224Quant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

     lateinit var bitmap: Bitmap
     lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView= findViewById(R.id.imageView)

        var fileName = "label.txt"
        var inputString = application.assets.open(fileName).bufferedReader().use { it.readText() }
        var townList = inputString.split("\n")

        var tv : TextView = findViewById(R.id.textView)

        var select: Button = findViewById(R.id.button)
        select.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type= "image/*"
            startActivityForResult(intent,100)
        })

        val predict : Button = findViewById(R.id.button2)
        predict.setOnClickListener(View.OnClickListener {
            var resized = Bitmap.createScaledBitmap(bitmap,224,224,true)
            val model = MobilenetV110224Quant.newInstance(this)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)

            var tensorBuffer = TensorImage.fromBitmap(resized)
            var byteBuffer = tensorBuffer.buffer
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            var max = getMax(outputFeature0.floatArray)

            tv.setText(townList[max])

// Releases model resources if no longer used.
            model.close()

        })


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imageView.setImageURI(data?.data)
        val uri : Uri? = data?.data
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)


    }

     fun getMax(arr:FloatArray) : Int{
        var index = 0
        var min = 0.0f
        for (i in 0..1000){

            if (arr[i] > min)
            {
                index = i
                min = arr[i]
            }
        }
        return index
    }
}