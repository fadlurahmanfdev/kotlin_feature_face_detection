# Overview

Android library simplify face & liveness operation using (MLKit, Firebase).

## Initialization

```kotlin
val liveFaceXDetection = LiveFaceXDetection()
liveFaceXDetection.initialize()
````

## Capture Face Detection
Face Detection via camera capture to detect face along with (left eye open probability, right eye open probability, smiling probability)
using Firebase MLKit.

```kotlin
liveFaceXDetection.processImage(
    imageProxy,
    object: LiveFaceXDetection.CaptureListener {
        override fun onFaceDetected(imageProxy: ImageProxy, faces: List<Face>) {
            // process image here
            imageProxy.close()
        }

        override fun onFailureFaceDetection(
            imageProxy: ImageProxy,
            exception: LiveFaceXException
        ) {
            // process image here
            imageProxy.close()
        }
    }
)
```

## Liveness Face Detection

Liveness detection camera processing, this method will ask user to do some gesture.

```kotlin
liveFaceXDetection.processLivenessImage(
    imageProxy = imageProxy,
    gestures = listOf(
        LiveFaceXEyeBlinked(
            type = LEFT_EYE_BLINK,
            blinkedThreshold = 0.8,
            blinkedThresholdCount = 2,
        ),
        LiveFaceXEyeBlinked(
            type = RIGHT_EYE_BLINK,
            blinkedThreshold = 0.8,
            blinkedThresholdCount = 2,
        ),
        LiveFaceXEyeBlinked(
            type = BLINK,
            blinkedThreshold = 0.8,
            blinkedThresholdCount = 2,
        ),
    ),
    listener = object: LiveFaceXDetection.LivenessListener {
        override fun onAskedLivenessVerification(gesture: LiveFaceXGesture) {}
        override fun onResetLivenessVerification() {}
        override fun onAskedLivenessVerificationSucceed(
            gesture: LiveFaceXGesture,
            imageProxy: ImageProxy,
        ) {}
        override fun onEmptyFaceDetected(imageProxy: ImageProxy) {}
        override fun onSuccessfullyLivenessVerification(imageProxy: ImageProxy) {}
        override fun onFailureFaceDetection(
            imageProxy: ImageProxy,
            exception: LiveFaceXException,
        ) {}
    },
)
```