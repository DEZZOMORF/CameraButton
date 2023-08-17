package com.example.camerabutton

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camerabutton.ui.theme.CameraButtonTheme
import com.example.camerabutton.ui.theme.TextColor

class MainActivity : ComponentActivity() {

    private var clickState = true
    private val toastText get() = if (clickState) "CLICK" else "CLACK"
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraButtonTheme() {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        CameraButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(4.dp)
                                .clip(shape = RoundedCornerShape(12.dp)),
                            text = "CLICK-CLACK",
                            onClick = {
                                toast?.cancel()
                                toast = Toast.makeText(this@MainActivity, toastText, Toast.LENGTH_SHORT)
                                toast?.show()
                                clickState = !clickState
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
        OutlinedButton(
            onClick = { onClick() },
            contentPadding = PaddingValues(),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                SimpleCameraPreview()
                FlexibleText(text = text)
            }
        }
    }
}

@Composable
fun FlexibleText(
    modifier: Modifier = Modifier,
    text: String
) {
    val defaultTextStyle = TextStyle(
        textAlign = TextAlign.Center,
        fontSize = 200.sp,
        fontWeight = FontWeight.Bold,
        color = TextColor,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset.Zero,
            blurRadius = 4f
        ),
        drawStyle = Stroke(
            miter = 12f,
            width = 4f,
            join = StrokeJoin.Round,
        )
    )
    var textStyle by remember { mutableStateOf(defaultTextStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .drawWithContent {
                if (readyToDraw) drawContent()
            },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        },
    )
}

@Composable
private fun SimpleCameraPreview() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}