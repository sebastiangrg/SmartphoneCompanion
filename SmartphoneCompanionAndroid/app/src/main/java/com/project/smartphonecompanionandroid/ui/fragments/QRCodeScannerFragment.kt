package com.project.smartphonecompanionandroid.ui.fragments

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.project.smartphonecompanionandroid.utils.hasPermission
import kotlinx.android.synthetic.main.fragment_qrcode_scanner.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import java.io.IOException


class QRCodeScannerFragment : Fragment(), AnkoLogger {
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

        info("Starting QRCodeScannerFragment")

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        if (requireContext().hasPermission(Manifest.permission.CAMERA)) {
            startScanning()
        } else {
            requireActivity().onBackPressed()
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
                    info("Decoded QR: $decoded")

                    if (isValidUID(decoded)) {
                        stopScanning()
                        sendUIDToFirebase(decoded)
                    }
                }
            }
        })
    }

    private fun stopScanning() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cameraSource.stop()
            requireActivity().onBackPressed()
        }
    }

    private fun isValidUID(value: String): Boolean {
        return value.length in 27..30
    }

    private fun sendUIDToFirebase(uid: String) {
        val functions = FirebaseFunctions.getInstance()

        val data = hashMapOf(
            "webUID" to uid
        )

        functions.getHttpsCallable("pairWithUID")
            .call(data)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    warn("Pair with UID failed")
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val details = e.details
                        warn("Error code - $code - $details")
                    }
                } else {
                    info("Pair with UID completed successfully")
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::barcodeDetector.isInitialized) {
            barcodeDetector.release()
        }
        if (::cameraSource.isInitialized) {
            cameraSource.release()
        }
    }
}
