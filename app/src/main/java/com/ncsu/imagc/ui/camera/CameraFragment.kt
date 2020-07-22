package com.ncsu.imagc.ui.camera

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.dialog_photo.view.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class CameraFragment : Fragment() {


    private var savedUri: Uri?= null
    private lateinit var camera: Camera
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture
    private var totalMovement: Float = 0f
    private var startX: Float = 0f
    private var titlePhoto = ""
        set(value) {
            field = value
            titleTextView.text = value
        }

    private var photoDescription = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startCamera()
        takePhotoButton.setOnClickListener {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatted = formatter.format(Date())
            titlePhoto =  "${formatted}"
            captureImage()
        }
        enableTorchButton.setOnClickListener {
            camera.cameraControl.enableTorch(camera.cameraInfo.torchState.value != TorchState.ON)
        }

        confirmButton.setOnClickListener {
            confirmPhoto()
        }
        cancelButton.setOnClickListener {
            discardPhoto()
        }
        editButton.setOnClickListener {
            editPhoto()
        }

        imageCardView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                totalMovement = 0f
                view.clearAnimation()
                startX = motionEvent.x
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                totalMovement += motionEvent.x - startX
                view.x += motionEvent.x - startX
                view.rotation += -(motionEvent.x - startX) / 100
                view.y += abs(motionEvent.x - startX) / 10
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if(abs(totalMovement) > 300) {
                    if(totalMovement > 0)
                        confirmPhoto()
                    else
                        discardPhoto()
                }
                else
                    view.animate().translationX(0f).translationY(0f).rotation(0f)
            }
            true
        }
    }

    private fun captureImage() {
        val photoFile = File.createTempFile("IMG_", null, context!!.cacheDir)
        if(!photoFile.exists()) {
            try {
                val isCreated = photoFile.createNewFile()
            } catch (e: IOException) {
                Snackbar.make(view!!, "Failed to create the file", Snackbar.LENGTH_LONG)
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Snackbar.make(view!!, "Photo capture failed: ${exc.message}", Snackbar.LENGTH_LONG)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)
                    photoImageView.setImageURI(savedUri)
                    cardViewContainer.visibility = View.VISIBLE
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context!!)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                cameraProvider.unbindAll()
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(view!!.display.rotation)
                    .build()

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                preview?.setSurfaceProvider(previewView.createSurfaceProvider(camera?.cameraInfo))
                previewView.afterMeasured {
                    previewView.setOnTouchListener { _, event ->
                        return@setOnTouchListener when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                                    previewView.width.toFloat(), previewView.height.toFloat()
                                )
                                val autoFocusPoint = factory.createPoint(event.x, event.y)
                                camera.cameraControl.startFocusAndMetering(
                                    FocusMeteringAction.Builder(
                                        autoFocusPoint,
                                        FocusMeteringAction.FLAG_AF
                                    ).apply {
                                        disableAutoCancel()
                                    }.build()
                                )
                                true
                            }
                            else -> false // Unhandled event.
                        }
                    }
                }
            } catch(exc: Exception) {
                Log.e("LXT", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context!!))
    }

    fun discardPhoto() {
        imageCardView.animate().alpha(0f).withEndAction {
            cardViewContainer.visibility = View.GONE
            imageCardView.translationX = 0f
            imageCardView.translationY = 0f
            imageCardView.rotation = 0f
            imageCardView.alpha = 1f
        }
    }

    fun confirmPhoto() {
        discardPhoto()
        (activity as? MainActivity)?.addImage(savedUri!!, titlePhoto, photoDescription)
    }

    fun editPhoto() {
        val view = this.layoutInflater.inflate(R.layout.dialog_photo, null)
        view.photoDescription.setText(photoDescription)
        view.sensorInfoTextView.text = (activity as MainActivity).getSensorInfo(false)
        val builder = AlertDialog.Builder(context!!)
        builder.setView(view)
        builder.setPositiveButton("Ok") { dialog, _ ->
            this.photoDescription = view.photoDescription.text.toString()
        }
        builder.setNegativeButton("Cancel", null)
        builder.create()
        builder.show()
    }
}

inline fun View.afterMeasured(crossinline block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}