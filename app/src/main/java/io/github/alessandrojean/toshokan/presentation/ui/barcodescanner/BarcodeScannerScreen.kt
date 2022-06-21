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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.IsbnLookupScreen
import io.github.alessandrojean.toshokan.service.barcode.BarcodeAnalyser
import io.github.alessandrojean.toshokan.util.extension.bottomPadding
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
        EnhancedSmallTopAppBar(
          contentPadding = WindowInsets.statusBars.asPaddingValues(),
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
    onBarcodeDetected: (List<Barcode>) -> Unit,
    overlay: @Composable BoxScope.() -> Unit = { Overlay(modifier = Modifier.fillMaxSize()) }
  ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = remember { Preview.Builder().build() }

    val cameraExecutor = Executors.newSingleThreadExecutor()

    val cameraProvider by produceState<ProcessCameraProvider?>(
      initialValue = null,
      lifecycleOwner.lifecycle
    ) {
      lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        value = ProcessCameraProvider.getInstance(context).await()
      }
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

    // TODO: Handle camera unbind on Lifecycle, currently broken by Voyager.
    DisposableEffect(lifecycleOwner.lifecycle) {
//      val observer = LifecycleEventObserver { _, event ->
//        if (event == Lifecycle.Event.ON_PAUSE) {
//          cameraProvider?.unbindAll()
//        }
//      }
//
//      lifecycleOwner.lifecycle.addObserver(observer)

      onDispose {
//        lifecycleOwner.lifecycle.removeObserver(observer)
        cameraProvider?.unbindAll()
      }
    }

    Box(
      modifier = Modifier
        .pointerInput(camera, focusOnTap) {
          if (!focusOnTap) {
            return@pointerInput
          }

          detectTapGestures { tapOffset ->
            val meteringPointFactory = SurfaceOrientedMeteringPointFactory(
              size.width.toFloat(),
              size.height.toFloat()
            )

            val meteringAction = FocusMeteringAction
              .Builder(
                meteringPointFactory.createPoint(tapOffset.x, tapOffset.y),
                FocusMeteringAction.FLAG_AF
              )
              .disableAutoCancel()
              .build()

            camera?.cameraControl?.startFocusAndMetering(meteringAction)
          }
        }
        .then(modifier)
    ) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
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

      overlay.invoke(this)
    }
  }

  @Composable
  fun Overlay(
    modifier: Modifier = Modifier,
    widthPercentage: Float = 0.7f,
    aspectRatio: Float = 2f / 1f,
    animationDurationMillis: Int = 1_500,
    anchorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
  ) {
    val navigationBarsPadding = WindowInsets.navigationBars.bottomPadding + 96.dp
    val navigationBarsHeightPx = with(LocalDensity.current) { navigationBarsPadding.toPx() }

    val infiniteTransition = rememberInfiniteTransition()
    val anchorOpacity by infiniteTransition.animateFloat(
      initialValue = 1.0f,
      targetValue = 0.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(animationDurationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      )
    )

    val anchorColorAnimated = anchorColor.copy(alpha = anchorOpacity)

    Canvas(modifier = modifier) {
      val width = widthPercentage * size.width
      val height = (1f / aspectRatio) * width

      val left = (size.width - width) / 2f
      val right = (size.width - width) / 2f + width
      val top = (size.height - height - navigationBarsHeightPx) / 2f
      val bottom = (size.height - height - navigationBarsHeightPx) / 2f + height
      val cornerRadius = CornerRadius(32f, 32f)

      val roundRectPath = Path().apply {
        val roundRect = RoundRect(
          left = left,
          right = right,
          top = top,
          bottom = bottom,
          cornerRadius = cornerRadius
        )

        addRoundRect(roundRect)
      }

      clipPath(roundRectPath, clipOp = ClipOp.Difference) {
        drawRect(SolidColor(containerColor))
      }

      val anchorSize = 0.1f * width
      val strokeWidth = 10f

      val anchorPath = Path().apply {
        val anchorRect = RoundRect(
          left = left + strokeWidth,
          right = right - strokeWidth,
          top = top + strokeWidth,
          bottom = bottom - strokeWidth,
          cornerRadius = CornerRadius(cornerRadius.x - 3, cornerRadius.y - 3)
        )

        val firstRect = RoundRect(
          left = left,
          right = right,
          top = top + anchorSize,
          bottom = bottom - anchorSize
        )

        val secondRect = RoundRect(
          left = left + anchorSize,
          right = right - anchorSize,
          top = top,
          bottom = bottom
        )

        addRoundRect(anchorRect)
        addRoundRect(firstRect)
        addRoundRect(secondRect)
      }

      clipPath(anchorPath, clipOp = ClipOp.Difference) {
        drawRoundRect(
          color = anchorColorAnimated,
          topLeft = Offset(x = left, y = top),
          size = Size(width, height),
          cornerRadius = cornerRadius
        )
      }
    }
  }

}