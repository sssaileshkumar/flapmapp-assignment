package com.example.cvgl

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.cvgl.databinding.ActivityMainBinding
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var renderer: CVGLRenderer
    private var streamClient: StreamClient? = null

    // For FPS calculation
    private var frameCount = 0
    private var lastFpsTimestamp: Long = 0
    
    // Streaming control
    private var frameCounter = 0
    private val STREAM_SKIP_FRAMES = 2 // Send every 3rd frame

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize OpenGL Renderer
        renderer = CVGLRenderer()
        binding.glSurfaceView.setEGLContextClientVersion(2)
        binding.glSurfaceView.setRenderer(renderer)
        binding.glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize Stream Client
        // REPLACE WITH YOUR PC IP ADDRESS
        // 10.0.2.2 is localhost for Android Emulator
        val serverUrl = "ws://192.168.1.34:8080" 
        streamClient = StreamClient(object : StreamClient.StreamListener {
            override fun onConnected() {
                runOnUiThread { Toast.makeText(this@MainActivity, "Connected to Stream", Toast.LENGTH_SHORT).show() }
            }

            override fun onDisconnected() {
                runOnUiThread { Toast.makeText(this@MainActivity, "Disconnected from Stream", Toast.LENGTH_SHORT).show() }
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "Stream Error: ${t.message}")
            }
        })
        streamClient?.connect(serverUrl)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { image ->
                processImage(image)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis
                )
                // Reset FPS counter when camera starts
                lastFpsTimestamp = System.currentTimeMillis()
                frameCount = 0
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(image: ImageProxy) {
        // Get the Y plane (grayscale)
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = image.width
        val height = image.height

        // Create OpenCV Mats
        val yMat = org.opencv.core.Mat(height, width, org.opencv.core.CvType.CV_8UC1)
        yMat.put(0, 0, data)
        
        val processedMat = org.opencv.core.Mat()

        // Process in Native
        nativeProcessFrame(yMat.nativeObjAddr, processedMat.nativeObjAddr)

        // Stream Frame
        if (frameCounter++ % STREAM_SKIP_FRAMES == 0) {
            val matOfByte = MatOfByte()
            Imgcodecs.imencode(".jpg", processedMat, matOfByte)
            val byteArray = matOfByte.toArray()
            streamClient?.send(byteArray)
            matOfByte.release()
        }

        // Convert back to byte array for OpenGL
        val processedData = ByteArray(processedMat.total().toInt() * processedMat.channels())
        processedMat.get(0, 0, processedData)

        // Update Renderer
        renderer.updateTexture(processedData, width, height)
        binding.glSurfaceView.requestRender()

        // Calculate and display FPS
        calculateFps()

        // Release Mats
        yMat.release()
        processedMat.release()
        
        image.close()
    }

    private fun calculateFps() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastFpsTimestamp

        if (elapsedTime >= 1000) { // Update every second
            val fps = frameCount
            frameCount = 0
            lastFpsTimestamp = currentTime
            
            runOnUiThread {
                binding.fpsTextView.text = "FPS: $fps"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        streamClient?.close()
    }

    companion object {
        private const val TAG = "MainActivity"
        
        // Load native library
        init {
            System.loadLibrary("cvgl")
            System.loadLibrary("opencv_java4") 
        }
    }
    
    // Native methods
    external fun stringFromJNI(): String
    external fun nativeProcessFrame(matAddrInput: Long, matAddrOutput: Long)
}
