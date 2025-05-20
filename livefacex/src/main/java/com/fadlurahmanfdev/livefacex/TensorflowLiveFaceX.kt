package com.fadlurahmanfdev.livefacex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.fadlurahmanfdev.livefacex.core.constant.LiveFaceXExceptionConstant
import com.fadlurahmanfdev.livefacex.exception.LiveFaceXException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TensorflowLiveFaceX {
    private val scope = CoroutineScope(Dispatchers.Default)
    lateinit var interpreter: Interpreter

    fun generateInputDataFromFile(
        path: String,
        onSuccess: (Array<Array<Array<Array<FloatArray>>>>) -> Unit,
        onFailure: (LiveFaceXException) -> Unit,
    ) {
        val bitmap = BitmapFactory.decodeFile(path)
        generateInputDataFromBitmap(bitmap = bitmap, onSuccess = onSuccess, onFailure = onFailure)
    }

    fun generateInputDataFromBitmap(
        bitmap: Bitmap,
        onSuccess: (Array<Array<Array<Array<FloatArray>>>>) -> Unit,
        onFailure: (LiveFaceXException) -> Unit,
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                    val result = Array(1) { // batch size
                        Array(1) { // 16 frames - same image
                            Array(224) { row ->
                                Array(224) { col ->
                                    FloatArray(3) // RGB
                                }
                            }
                        }
                    }

                    for (frame in 0 until 1) {
                        for (y in 0 until 224) {
                            for (x in 0 until 224) {
                                val pixel = resizedBitmap.getPixel(x, y)
                                val r = Color.red(pixel) / 255f
                                val g = Color.green(pixel) / 255f
                                val b = Color.blue(pixel) / 255f

                                result[0][frame][y][x][0] = r
                                result[0][frame][y][x][1] = g
                                result[0][frame][y][x][2] = b
                            }
                        }
                    }
                    onSuccess(result)
                }
            } catch (e: Throwable) {
                Log.e(
                    this::class.java.simpleName,
                    "LiveFaceX-LOG %%% - failed to create input data",
                    e
                )
                withContext(Dispatchers.Default) {
                    onFailure(LiveFaceXExceptionConstant.UNKNOWN.copy(message = e.message))
                }
            }
        }
    }

    fun initialize(
        assetFileName: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (LiveFaceXException) -> Unit,
    ) {
        scope.launch {
            try {
                val modelInputStream =
                    FileInputStream(context.assets.openFd(assetFileName).fileDescriptor)
                val fileChannel = modelInputStream.channel
                val startOffset = context.assets.openFd(assetFileName).startOffset
                val declaredLength = context.assets.openFd(assetFileName).declaredLength
                val fileBuffer =
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

                withContext(Dispatchers.Default) {
                    interpreter = Interpreter(fileBuffer)
                    onSuccess()
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Default) {
                    onFailure(LiveFaceXExceptionConstant.UNKNOWN.copy(message = e.message))
                }
            }
        }
    }

    // Preprocess input data
    private fun preprocessInput(inputData: Array<Array<Array<Array<FloatArray>>>>): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * 16 * 224 * 224 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (batch in inputData) {
            for (frame in batch) {
                for (row in frame) {
                    for (col in row) {
                        for (value in col) {
                            byteBuffer.putFloat(value)
                        }
                    }
                }
            }
        }
        return byteBuffer
    }

    // Run inference
    fun runInference(
        inputData: Array<Array<Array<Array<FloatArray>>>>,
        onSuccess: (Float) -> Unit,
        onFailure: (LiveFaceXException) -> Unit,
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val inputBuffer = preprocessInput(inputData)
                    val outputBuffer =
                        ByteBuffer.allocateDirect(1 * 1 * 4) // Output shape [1, 1], 4 bytes per float
                    outputBuffer.order(ByteOrder.nativeOrder())

                    interpreter.run(inputBuffer, outputBuffer)

                    outputBuffer.rewind()
                    val output = outputBuffer.float
                    Log.d(
                        this::class.java.simpleName,
                        "LiveFaceX-LOG %%% successfully detect liveness: $output"
                    )
                    withContext(Dispatchers.Main) {
                        onSuccess(output)
                    }
                }
            } catch (e: Throwable) {
                Log.e(this::class.java.simpleName, "LiveFaceX-LOG %%% failed to detect liveness", e)
                withContext(Dispatchers.Default) {
                    onFailure(LiveFaceXExceptionConstant.UNKNOWN.copy(message = e.message))
                }
            }
        }
    }

    fun close() {
        try {
            interpreter.close()
        } catch (e: Throwable) {
            Log.w(this::class.java.simpleName, "LiveFaceX-LOG %%% failed to close interpreter", e)
        }
    }
}