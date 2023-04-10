package com.prashant.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.io.IOException


class DashboardActivity : AppCompatActivity() {
    private lateinit var btn: Button
    private lateinit var img: ImageView
    private lateinit var smileProbTV: TextView
    private lateinit var leftEyeProb: TextView
    private lateinit var rightEyeProb: TextView
    private lateinit var group:Group
    private lateinit var progressbar:ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btn = findViewById(R.id.log)
        img = findViewById(R.id.img1)
        smileProbTV=findViewById(R.id.smileProbTV)
        leftEyeProb=findViewById(R.id.leftEyeProb)
        rightEyeProb=findViewById(R.id.rightEyeProb)
        group=findViewById(R.id.group)
        progressbar=findViewById(R.id.progressBar)

        btn.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(Intent.createChooser(it, "Choose image"), 123)

            }

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 123) {
        }
        img.setImageURI(data?.data)

        val image: InputImage


        try {
            val bmp: Bitmap
            bmp = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data!!)
            val mutableBmp = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.RGB_565)
            val canvas = Canvas(mutableBmp)
            canvas.drawBitmap(bmp, 0f, 0f, null)
            val paint = Paint()
            paint.apply {
                color = Color.YELLOW
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }



            image = InputImage.fromFilePath(applicationContext, data?.data!!)

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
            val detector = FaceDetection.getClient(options)
            val result: Task<List<Face>> = detector.process(image)
                .addOnSuccessListener { faces ->
                    when {
                        faces.isNullOrEmpty() -> {
                            smileProbTV.text = "No Face Found"
                            leftEyeProb.text = ""
                            rightEyeProb.text = ""
                        }
                        else ->
                            for (face in faces) {
                                val bounds = face.boundingBox
                                canvas.drawRect(bounds, paint)
                                img.setImageDrawable(BitmapDrawable(resources, mutableBmp))
                                val rotY =
                                    face.headEulerAngleY // Head is rotated to the right rotY degrees
                                val rotZ =
                                    face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                // nose available):
                                val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
                                leftEar?.let {
                                    val leftEarPos = leftEar.position
                                }
                                val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                                val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                                leftEye?.let {
                                    val pos = it.position
                                    val rect = Rect(
                                        pos.x.toInt() - 30,
                                        pos.y.toInt() - 10,
                                        pos.x.toInt() + 20,
                                        pos.y.toInt() + 10
                                    )
                                    canvas.drawRect(rect, paint)
                                }
                                rightEye?.let {
                                    val pos = it.position
                                    val rect = Rect(
                                        pos.x.toInt() - 20,
                                        pos.y.toInt() - 10,
                                        pos.x.toInt() + 20,
                                        pos.y.toInt() + 10
                                    )
                                    canvas.drawRect(rect, paint)
                                }

                                // If contour detection was enabled:
                                val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
                                val upperLipBottomContour =
                                    face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points

                                // If classification was enabled:
                                if (face.smilingProbability != null) {
                                    val smileProb = face.smilingProbability
                                    if (smileProb != null) {
                                        when {
                                            smileProb >= 0.5 -> smileProbTV.text = "Smiling: ${smileProb * 100 } "
                                            else -> smileProbTV.text = "Serious: ${smileProb * 100} %"
                                        }
                                    }
                                }
                                if (face.rightEyeOpenProbability != null) {
                                    val rightEyeOpenProb = face.rightEyeOpenProbability
                                    if (rightEyeOpenProb != null) {
                                        rightEyeProb.text = "Right Eye Probability: ${rightEyeOpenProb * 100} %"
                                    }
                                }
                                if (face.leftEyeOpenProbability != null) {
                                    val leftEyeOpenProb = face.rightEyeOpenProbability
                                    if (leftEyeOpenProb != null) {
                                        leftEyeProb.text = "Left Eye Probability: ${leftEyeOpenProb * 100} %"
                                    }
                                }

                                // If face tracking was enabled:
                                if (face.trackingId != null) {
                                    val id = face.trackingId
                                }
                            }
                    }
                    progressbar.visibility=View.GONE
                    group.visibility = View.VISIBLE
                    btn.isEnabled = true
                }

                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                    Toast.makeText(this, "Error to detect faces", Toast.LENGTH_LONG).show()
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}

