package com.fadlurahmanfdev.example.domain

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel


class LivenessFeature {
    private val scope = CoroutineScope(Dispatchers.Default)
    lateinit var interpreter: Interpreter
    fun initialize(context: Context, callback: () -> Unit) {
        Log.d(this::class.java.simpleName, "TEST-LOG %%% 3 - START INITIALIZE INTERPRETER")
        scope.launch {
            val modelName = "liveness_model.tflite"
            val modelInputStream = FileInputStream(context.assets.openFd(modelName).fileDescriptor)
            val fileChannel = modelInputStream.channel
            val startOffset = context.assets.openFd(modelName).startOffset
            val declaredLength = context.assets.openFd(modelName).declaredLength
            val fileBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            withContext(Dispatchers.Default){
                interpreter = Interpreter(fileBuffer)
                callback()
            }
        }
    }

    // Preprocess input data
    private fun preprocessInput(inputData: Array<Array<Array<Array<FloatArray>>>>): ByteBuffer {
        Log.d(this::class.java.simpleName, "TEST-LOG %%% 3 - PRE PROCESS INPUT")
        val inputShape = intArrayOf(1, 16, 224, 224, 3) // Input shape from your model
        val byteBuffer = ByteBuffer.allocateDirect(1 * 16 * 224 * 224 * 3 * 4) // 4 bytes per float
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
    fun runInference(inputData: Array<Array<Array<Array<FloatArray>>>>, callback: (Float) -> Unit) {
        Log.d(this::class.java.simpleName, "TEST-LOG %%% RUN INFERENCE")
        scope.launch {
            withContext(Dispatchers.Default){
                val inputBuffer = preprocessInput(inputData)
                val outputBuffer = ByteBuffer.allocateDirect(1 * 1 * 4) // Output shape [1, 1], 4 bytes per float
                outputBuffer.order(ByteOrder.nativeOrder())

                interpreter.run(inputBuffer, outputBuffer)

                outputBuffer.rewind()
                val output = outputBuffer.float
                Log.d(this::class.java.simpleName, "TEST-LOG %%% 3 - OUTPUT: ${output}")
                withContext(Dispatchers.Main){
                    callback(output)
                }
            }
        }
    }

    fun close() {
        interpreter.close()
    }
}