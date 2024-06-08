package co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception

data class FeatureFaceDetectionException(
    val code: String?,
    override val message: String?
) : Throwable(message = message)
