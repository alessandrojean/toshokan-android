package io.github.alessandrojean.toshokan.presentation.ui.barcodescanner

import android.Manifest
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.guava.await
import java.util.concurrent.Executors

class BarcodeScannerScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val navigator = LocalNavigator.currentOrThrow

    var enableTorch by rememberSaveable { mutableStateOf(false) }
    var hasFlashUnit by rememberSaveable { mutableStateOf(false) }

    Scaffold(
      topBar = {
        Surface(color = MaterialTheme.colorScheme.surface) {
          SmallTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.smallTopAppBarColors(
              containerColor = Color.Transparent
            ),
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
        }
      },
      floatingActionButtonPosition = FabPosition.Center,
      floatingActionButton = {
        AnimatedVisibility(
          modifier = Modifier.navigationBarsPadding(),
          visible = hasFlashUnit,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          LargeFloatingActionButton(onClick = { enableTorch = !enableTorch }) {
            if (enableTorch) {
              Icon(
                imageVector = Icons.Outlined.FlashOn,
                contentDescription = stringResource(R.string.action_disable_flash),
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
              )
            } else {
              Icon(
                imageVector = Icons.Outlined.FlashOff,
                contentDescription = stringResource(R.string.action_enable_flash),
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
              )
            }
          }
        }
      },
      content = { innerPadding ->
        Crossfade(targetState = cameraPermission) { permissionState ->
          when (permissionState.status) {
            PermissionStatus.Granted -> {
              CameraPreview(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                enableTorch = enableTorch,
                onHasFlashUnitDetected = { hasFlashUnit = it },
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

  /**
   * Camera composable with addons.
   * https://stackoverflow.com/a/70302763
   */
  @Composable
  fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    implementationMode: PreviewView.ImplementationMode = PreviewView.ImplementationMode.COMPATIBLE,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    focusOnTap: Boolean = true,
    enableTorch: Boolean = false,
    onHasFlashUnitDetected: (Boolean) -> Unit = {},
    onBarcodeDetected: (List<Barcode>) -> Unit
  ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = remember { Preview.Builder().build() }

    val cameraExecutor = Executors.newSingleThreadExecutor()

    val cameraProvider by produceState<ProcessCameraProvider?>(initialValue = null) {
      value = ProcessCameraProvider.getInstance(context).await()
    }

    val barcodeAnalyser = BarcodeAnalyser(onBarcodeDetected)
    val imageAnalysis = ImageAnalysis.Builder()
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build()
      .also { it.setAnalyzer(cameraExecutor, barcodeAnalyser) }

    val camera = remember(cameraProvider) {
      cameraProvider.let {
        it?.unbindAll()
        it?.bindToLifecycle(
          lifecycleOwner,
          cameraSelector,
          preview,
          imageAnalysis
        )
      }
    }

    LaunchedEffect(camera, enableTorch) {
      camera?.let {
        val hasFlashUnit = it.cameraInfo.hasFlashUnit()
        onHasFlashUnitDetected.invoke(hasFlashUnit)

        if (it.cameraInfo.hasFlashUnit()) {
          it.cameraControl.enableTorch(enableTorch).await()
        }
      }
    }

    DisposableEffect(Unit) {
      onDispose {
        cameraProvider?.unbindAll()
      }
    }

    AndroidView(
      modifier = modifier.pointerInput(camera, focusOnTap) {
        if (!focusOnTap) {
          return@pointerInput
        }

        detectTapGestures {
          val meteringPointFactory = SurfaceOrientedMeteringPointFactory(
            size.width.toFloat(),
            size.height.toFloat()
          )

          val meteringAction = FocusMeteringAction
            .Builder(
              meteringPointFactory.createPoint(it.x, it.y),
              FocusMeteringAction.FLAG_AF
            )
            .disableAutoCancel()
            .build()

          camera?.cameraControl?.startFocusAndMetering(meteringAction)
        }
      },
      factory = { androidViewContext ->
        PreviewView(androidViewContext).also {
          it.scaleType = scaleType
          it.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          it.implementationMode = implementationMode
          preview.setSurfaceProvider(it.surfaceProvider)
        }
      }
    )
  }

}