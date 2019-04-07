package com.project.smartphonecompanionandroid.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_qrcode_scanner.*
import com.google.android.gms.vision.CameraSource
import android.view.SurfaceHolder
import android.view.ViewGroup
import java.io.IOException
import com.google.android.gms.vision.Detector
import com.project.smartphonecompanionandroid.utils.snackbar
import android.util.DisplayMetrics


class QRCodeScannerFragment : Fragment() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

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
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedFps(15.0f)
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
                    decodedStringTextView.post {
                        decodedStringTextView.text = decoded
                    }
                }
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                    snackbar("You declined to allow the app to access your camera")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
        barcodeDetector.release()
    }
}
