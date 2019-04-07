package com.project.smartphonecompanionandroid.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.smartphonecompanionandroid.utils.goBack
import com.project.smartphonecompanionandroid.utils.snackbar
import kotlinx.android.synthetic.main.fragment_qrcode_scanner.*
import java.io.IOException


class QRCodeScannerFragment : Fragment() {
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            com.project.smartphonecompanionandroid.R.layout.fragment_qrcode_scanner,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(Array(CAMERA_PERMISSION_REQUEST_CODE) { Manifest.permission.CAMERA }, 1)
            } else {
                startScanning()
            }
        }
    }

    private fun startScanning() {
        barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()


        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = 1024
        val height = 1024

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedFps(10.0f)
            .setRequestedPreviewSize(height, width)
            .setAutoFocusEnabled(true)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(cameraView.holder)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                } catch (ex: SecurityException) {
                    ex.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val decoded = barcodes.valueAt(0).displayValue
                    Log.d(TAG, decoded)

                    if (isValidUID(decoded)) {
                        stopScanning()
                        loginOnComputer(decoded)
                    }
                }
            }
        })
    }

    private fun stopScanning() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cameraSource.stop()
            val activity = requireActivity()
            activity.goBack()
        }
    }

    private fun isValidUID(value: String): Boolean {
        // TODO find more validation criteria
        return value.length in 27..30
    }

    private fun loginOnComputer(token: String) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val myRef = database.getReference("users/$userId/webUID")

        myRef.setValue(token)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE ->
                // if the request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                    snackbar("You declined to allow the app to access your camera")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeDetector.release()
        cameraSource.release()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "QRCodeScannerFragment"
    }
}
