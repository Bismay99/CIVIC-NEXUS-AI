package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.model.CitizenProfile
import com.example.model.CivicAuthUser
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseNotConfiguredException(message: String) : Exception(message)
class GoogleSignInCancelledException(message: String = "Google Sign-In was cancelled or dismissed.") : Exception(message)

class FirebaseAuthService(private val context: Context) {

    private val tag = "CivicAuth"

    private val isFirebaseAppInitialized: Boolean
        get() = try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Throwable) {
            false
        }

    private val auth: FirebaseAuth?
        get() = if (isFirebaseAppInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }

    fun isConfigured(): Boolean = isFirebaseAppInitialized && auth != null

    fun getWebClientId(): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId)
            } else {
                "954633763870-ghn2le6b21eru94ekquo3l7ht6kt1iso.apps.googleusercontent.com"
            }
        } catch (e: Throwable) {
            "954633763870-ghn2le6b21eru94ekquo3l7ht6kt1iso.apps.googleusercontent.com"
        }
    }

    fun getCurrentUser(): CivicAuthUser? {
        val fbUser = auth?.currentUser ?: return null
        return CivicAuthUser(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@"),
            photoUrl = fbUser.photoUrl?.toString(),
            role = "citizen" // Strictly citizen role for all Android client sessions
        )
    }

    fun observeAuthState(): Flow<CivicAuthUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser
            if (user != null) {
                trySend(
                    CivicAuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName ?: user.email?.substringBefore("@"),
                        photoUrl = user.photoUrl?.toString(),
                        role = "citizen"
                    )
                )
            } else {
                trySend(null)
            }
        }

        currentAuth.addAuthStateListener(listener)
        awaitClose { currentAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(activity: Activity): Result<CivicAuthUser> {
        if (!isConfigured()) {
            return Result.failure(
                FirebaseNotConfiguredException(
                    "Firebase project configuration not detected. 'google-services.json' is missing from the /app directory. Please link your Firebase project in the console."
                )
            )
        }

        val clientId = getWebClientId()
        if (clientId.isNullOrBlank()) {
            return Result.failure(
                FirebaseNotConfiguredException(
                    "Google Web Client ID is not configured in 'google-services.json'. Please enable Google Sign-In in Firebase Console and download the updated config."
                )
            )
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        val credential = try {
            val result = withContext(Dispatchers.Main) {
                credentialManager.getCredential(activity, request)
            }
            result.credential
        } catch (e: GetCredentialCancellationException) {
            val causeMsg = e.cause?.localizedMessage
            val msg = e.message
            Log.w(tag, "Google Sign-In cancelled or dismissed: $msg (cause: $causeMsg)", e)
            val isExplicitUserDismiss = (causeMsg == null || causeMsg.contains("user", ignoreCase = true) || causeMsg.contains("activity is cancelled", ignoreCase = true)) &&
                    (msg == null || msg.contains("cancel", ignoreCase = true) || msg.contains("activity is cancelled", ignoreCase = true))
            if (isExplicitUserDismiss) {
                return Result.failure(GoogleSignInCancelledException())
            } else {
                return Result.failure(Exception("Google Sign-In was cancelled or failed: ${causeMsg ?: msg}"))
            }
        } catch (e: GetCredentialCustomException) {
            Log.e(tag, "Google Sign-In custom error: ${e.type} - ${e.message}", e)
            return Result.failure(Exception(e.message ?: "Authentication service error: ${e.type}"))
        } catch (e: GetCredentialProviderConfigurationException) {
            Log.e(tag, "Google Sign-In provider configuration error: ${e.message}", e)
            return Result.failure(Exception("Google Play Services configuration error: ${e.message}"))
        } catch (e: GetCredentialUnsupportedException) {
            Log.e(tag, "Google Sign-In unsupported: ${e.message}", e)
            return Result.failure(Exception("Google Sign-In is not supported on this device/version."))
        } catch (e: NoCredentialException) {
            Log.w(tag, "No Google credentials available on device: ${e.message}", e)
            return Result.failure(Exception("No Google accounts found. Please add a Google account in device settings."))
        } catch (e: GetCredentialException) {
            Log.e(tag, "Google Sign-In credential error: ${e.type} - ${e.message}", e)
            return Result.failure(Exception(e.message ?: "Google Sign-In failed (${e.type})"))
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In request failed", e)
            return Result.failure(Exception(mapAuthErrorMessage(e)))
        }

        return withContext(Dispatchers.IO) {
            try {
                val googleIdToken: String? = when {
                    credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    }
                    else -> null
                }

                if (googleIdToken.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Received invalid or empty Google ID token."))
                }

                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth!!.signInWithCredential(authCredential).await()
                val user = authResult.user
                    ?: return@withContext Result.failure(Exception("Authentication succeeded but user profile was empty."))

                // Sync citizen profile in Firestore
                syncCitizenProfileInFirestore(user, user.displayName ?: "Citizen")

                val citizen = CivicAuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: "Citizen",
                    photoUrl = user.photoUrl?.toString(),
                    role = "citizen"
                )
                Result.success(citizen)
            } catch (e: Exception) {
                Log.e(tag, "Firebase signInWithCredential error", e)
                Result.failure(Exception(mapAuthErrorMessage(e)))
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<CivicAuthUser> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(
                FirebaseNotConfiguredException(
                    "Firebase Authentication is unconfigured. Missing 'google-services.json' in /app."
                )
            )
        }

        try {
            val authResult = auth!!.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user
                ?: return@withContext Result.failure(Exception("Sign in completed with empty user."))

            val citizen = CivicAuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore("@"),
                photoUrl = user.photoUrl?.toString(),
                role = "citizen"
            )
            return@withContext Result.success(citizen)
        } catch (e: Exception) {
            Log.e(tag, "Email Sign-In error", e)
            return@withContext Result.failure(Exception(mapAuthErrorMessage(e)))
        }
    }

    suspend fun registerWithEmail(
        fullName: String,
        email: String,
        pass: String
    ): Result<CivicAuthUser> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(
                FirebaseNotConfiguredException(
                    "Firebase Authentication is unconfigured. Missing 'google-services.json' in /app."
                )
            )
        }

        try {
            val authResult = auth!!.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user
                ?: return@withContext Result.failure(Exception("Registration returned no user."))

            // Update display name on Firebase User
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to set display name on Auth profile", e)
            }

            // Sync citizen profile into Firestore
            syncCitizenProfileInFirestore(user, fullName.trim())

            val citizen = CivicAuthUser(
                uid = user.uid,
                email = user.email,
                displayName = fullName.trim(),
                photoUrl = user.photoUrl?.toString(),
                role = "citizen"
            )
            return@withContext Result.success(citizen)
        } catch (e: Exception) {
            Log.e(tag, "Registration error", e)
            return@withContext Result.failure(Exception(mapAuthErrorMessage(e)))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(
                FirebaseNotConfiguredException("Firebase Authentication is not configured.")
            )
        }

        try {
            auth!!.sendPasswordResetEmail(email.trim()).await()
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception(mapAuthErrorMessage(e)))
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            try {
                withTimeoutOrNull(1000L) {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
            } catch (e: Throwable) {
                Log.w(tag, "Credential manager state clear failed: ${e.message}")
            }
            return@withContext Result.success(Unit)
        } catch (e: Throwable) {
            Log.e(tag, "Sign out error", e)
            return@withContext Result.failure(e)
        }
    }

    private suspend fun syncCitizenProfileInFirestore(user: FirebaseUser, displayName: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(user.uid)
            val snapshot = userDoc.get().await()

            val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

            if (!snapshot.exists()) {
                val profileMap = hashMapOf<String, Any>(
                    "uid" to user.uid,
                    "name" to displayName,
                    "email" to (user.email ?: ""),
                    "citizenId" to "CN-OD-2026-" + user.uid.take(5).uppercase(),
                    "role" to "citizen", // Enforce citizen role exclusively
                    "trustScore" to 98,
                    "reportsCount" to 0,
                    "resolvedCount" to 0,
                    "impactPoints" to 100,
                    "createdAt" to nowStr,
                    "updatedAt" to nowStr
                )
                userDoc.set(profileMap, SetOptions.merge()).await()
            } else {
                val updateMap = hashMapOf<String, Any>(
                    "updatedAt" to nowStr,
                    "role" to "citizen" // Safeguard: Never allow client to elevate role
                )
                userDoc.set(updateMap, SetOptions.merge()).await()
            }
        } catch (e: Throwable) {
            Log.w(tag, "Firestore profile sync skipped or failed (offline/unconfigured): ${e.message}")
        }
    }

    private fun mapAuthErrorMessage(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please verify your credentials."
            is FirebaseAuthUserCollisionException -> "An account with this email address already exists. Please sign in instead."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters including letters and numbers."
            is FirebaseAuthException -> e.message ?: "Authentication failed."
            else -> e.localizedMessage ?: "Network or authentication service failure. Please check connection."
        }
    }
}
