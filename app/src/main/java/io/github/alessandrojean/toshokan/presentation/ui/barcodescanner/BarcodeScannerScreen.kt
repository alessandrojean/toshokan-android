package io.github.alessandrojean.toshokan.presentation.ui.barcodescanner

import android.Manifest
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.IsbnLookupScreen
import io.github.alessandrojean.toshokan.service.barcode.BarcodeAnalyser
import io.github.alessandrojean.toshokan.util.isValidBarcode
import io.github.alessandrojean.toshokan.util.isValidIsbn
import java.util.concurrent.Executors

class BarcodeScannerScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
      topBar = {
        SmallTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
              )
            }
          },
          title = { Text(stringResource(R.string.barcode_scanner)) }
        )
      },
      content = { innerPadding ->
        Crossfade(targetState = cameraPermission) { permissionState ->
          when (permissionState.status) {
            PermissionStatus.Granted -> {
              CameraPreview(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                onBarcodeDetected = { barcodes ->
                  val barcodeValid = barcodes
                    .filterNot { it.rawValue.isNullOrBlank() }
                    .firstOrNull { it.rawValue.orEmpty().isValidBarcode() }

                  if (barcodeValid != null && navigator.lastItem is BarcodeScannerScreen) {
                    navigator.push(IsbnLookupScreen(barcodeValid.rawValue!!))
                  }
                }
              )
            }
            is PermissionStatus.Denied -> {
              Box(modifier = Modifier.padding(innerPadding)) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = stringResource(R.string.grant_camera_permission),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                  )

                  Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = { cameraPermission.launchPermissionRequest() }
                  ) {
                    Text(stringResource(R.string.action_request_permission))
                  }
                }
              }
            }
          }
        }
      }
    )
  }

  @Composable
  fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (List<Barcode>) -> Unit
  ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }

    val cameraSelector = CameraSelector.Builder()
      .requireLensFacing(CameraSelector.LENS_FACING_BACK)
      .build()
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    val cameraProvider = cameraProviderFuture.get()
    val barcodeAnalyser = BarcodeAnalyser(onBarcodeDetected)
    val imageAnalysis = ImageAnalysis.Builder()
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build()
      .also { it.setAnalyzer(cameraExecutor, barcodeAnalyser) }

    DisposableEffect(Unit) {
      onDispose {
        cameraProvider.unbindAll()
      }
    }

    AndroidView(
      modifier = modifier,
      factory = { androidViewContext ->
        PreviewView(androidViewContext).apply {
          scaleType = PreviewView.ScaleType.FILL_CENTER
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
      },
      update = { previewView ->
        cameraProviderFuture.addListener({
          preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
          }

          try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
              lifecycleOwner,
              cameraSelector,
              preview,
              imageAnalysis
            )
          } catch (e: Exception) {
            Log.d("BarcodeScanner", "An exception happened: ${e.localizedMessage}")
          }
        }, ContextCompat.getMainExecutor(context))
      }
    )
  }

}