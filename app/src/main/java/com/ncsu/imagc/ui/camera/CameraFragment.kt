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
import com.ncsu.imagc.ui.dialogs.PhotoConditionsDialog
import kotlinx.android.synthetic.main.dialog_photo.*
import kotlinx.android.synthetic.main.dialog_photo.view.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class CameraFragment : Fragment(), PhotoConditionsDialog.PhotoConditionsDialogListener {

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
    private var weedsAmount = "1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startCamera()
        takePhotoButton.setOnClickListener {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatted = formatter.format(Date())
            titlePhoto = formatted
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
            if (motionEvent.action == MotionEvent.ACTION_DOWN){
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
            view.performClick()
        }
        typeCardView.setOnClickListener {
            showConditionsDialog()
        }
        weatherCardView.setOnClickListener {
            showConditionsDialog()
        }
        zoomInButton.setOnClickListener{ zoomScale(2f) }
        zoomOutButton.setOnClickListener { zoomScale(0.5f) }
        showConditions()
    }

    private fun captureImage() {
        val photoFile = File.createTempFile("IMG_", null, requireContext().cacheDir)
        if(!photoFile.exists()) {
            try {
                photoFile.createNewFile()
            } catch (e: IOException) {
                Snackbar.make(requireView(), "Failed to create the file", Snackbar.LENGTH_LONG)
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

            preview = createPreviewUseCase()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                cameraProvider.unbindAll()
                imageCapture = createCaptureUseCase()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                camera.cameraControl.setZoomRatio(1f)
                preview.setSurfaceProvider(previewView.createSurfaceProvider(camera?.cameraInfo))
                setUpTapToFocus()
            } catch(exc: Exception) {
                Log.e("LXT", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context!!))
    }

    private fun setUpTapToFocus() {
        previewView.setOnTouchListener{ view, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    previewView.width.toFloat(), previewView.height.toFloat()
                )
                val autoFocusPoint = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF).setAutoCancelDuration(5, TimeUnit.SECONDS).build()
                camera.cameraControl.startFocusAndMetering(action)
                view.performClick()
            }
            view.performClick()
        }
    }
    private fun createPreviewUseCase(): Preview {
        return Preview.Builder().apply {
            setTargetRotation(previewView.display.rotation)
        }.build()
    }
    private fun createCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder()
            .setTargetRotation(view!!.display.rotation)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    private fun discardPhoto() {
        imageCardView.animate().alpha(0f).withEndAction {
            cardViewContainer.visibility = View.GONE
            imageCardView.translationX = 0f
            imageCardView.translationY = 0f
            imageCardView.rotation = 0f
            imageCardView.alpha = 1f
        }
    }

    private fun confirmPhoto() {
        discardPhoto()
        (activity as? MainActivity)?.addImage(savedUri!!, titlePhoto, photoDescription, weedsAmount)
    }

    private fun editPhoto() {
        val view = this.layoutInflater.inflate(R.layout.dialog_photo, null)
        view.photoDescription.setText(photoDescription)
        view.sensorInfoTextView.text = (activity as MainActivity).getSensorInfo(false)
        val builder = AlertDialog.Builder(context!!)
        builder.setView(view)
        builder.setPositiveButton("Ok") { _, _ ->
            this.photoDescription = view.photoDescription.text.toString()
            this.weedsAmount = when {
                view.radioButton1.isChecked -> "1"
                view.radioButton25.isChecked -> "2-5"
                else -> ">5"
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.create()
        builder.show()
    }

    private fun showConditions() {
        val weather = (activity as MainActivity).weather
        val weedType = (activity as MainActivity).weedType
        weatherImageView.setImageDrawable(resources.getDrawable(
        when(weather) {
            PhotoConditionsDialog.WeatherType.SUNNY -> R.drawable.ic_sun
            PhotoConditionsDialog.WeatherType.CLOUDY -> R.drawable.ic_cloud
            PhotoConditionsDialog.WeatherType.PARTIAL -> R.drawable.ic_clouds_and_sun
        }, null))
        typeWeedTextView.text = weedType
    }

    private fun showConditionsDialog() {
        (activity as MainActivity).let {
            val dialog = PhotoConditionsDialog(requireContext(), it.weather, it.weedType, this)
            dialog.show()
        }
    }

    private fun zoomScale(scale: Float) {
        val ratio = (camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f) * scale
        camera.cameraControl.setZoomRatio(ratio)
    }

    override fun onContinueButton(weather: PhotoConditionsDialog.WeatherType, weedType: String) {
        (activity as MainActivity).weather = weather
        (activity as MainActivity).weedType = weedType
        showConditions()
    }
}
