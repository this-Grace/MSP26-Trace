package it.unibo.trace.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Utility class to handle biometric authentication using the Android Biometric library.
 *
 * This class provides methods to check if the device supports biometric authentication
 * and to perform the authentication process.
 *
 * @property activity The [FragmentActivity] used to host the biometric prompt.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    /**
     * Checks if the device and user are capable of authenticating using biometrics.
     *
     * @return An integer representing the status of biometric capabilities,
     * consistent with [BiometricManager.canAuthenticate] results.
     */
    fun canAuthenticate(): Int {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    }

    /**
     * Triggers the biometric authentication prompt.
     *
     * @param title The title displayed on the biometric prompt.
     * @param subtitle The subtitle displayed on the biometric prompt.
     * @param onSuccess Callback executed when authentication is successful.
     * @param onError Callback executed when an authentication error occurs, providing the error code and message.
     */
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
