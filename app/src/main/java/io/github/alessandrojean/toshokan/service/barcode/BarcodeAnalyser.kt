package io.github.alessandrojean.toshokan.service.barcode

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import logcat.LogPriority
import logcat.logcat

class BarcodeAnalyser(
  private val onBarcodeDetected: (List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {

  private var lastAnalyzedTimestamp: Long = 0L

  private val threshold = 1 * 1_000 // 1 second

  @SuppressLint("UnsafeOptInUsageError")
  override fun analyze(image: ImageProxy) {
    val currentTimestamp = System.currentTimeMillis()

    if (currentTimestamp - lastAnalyzedTimestamp >= threshold) {
      image.image?.let { imageToAnalyze ->
        val options = BarcodeScannerOptions.Builder()
          .setBarcodeFormats(Barcode.FORMAT_EAN_13)
          .build()
        val barcodeScanner = BarcodeScanning.getClient(options)
        val imageToProcess = InputImage.fromMediaImage(
          imageToAnalyze,
          image.imageInfo.rotationDegrees
        )

        barcodeScanner.process(imageToProcess)
          .addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
              onBarcodeDetected(barcodes)
            }
          }
          .addOnFailureListener { exception ->
            logcat("BarcodeAnalyzer", LogPriority.ERROR) { exception.stackTraceToString() }
          }
          .addOnCompleteListener { image.close() }
      }

      lastAnalyzedTimestamp = currentTimestamp
    } else {
      image.close()
    }
  }

}