package com.dhruva.allfirebasesampleapp.data.wrapper

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.dhruva.allfirebasesampleapp.R
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch


class AuthOperationsWrapper(private val firebaseAuth: FirebaseAuth) {
    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(activity)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("918396085047-upt49fcl84b5v94c6glb227sd3298798.apps.googleusercontent.com")//(activity.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        if (activity is ComponentActivity) {
            activity.lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(activity, request)
                    val credential = result.credential

                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        
                        firebaseAuth.signInWithCredential(authCredential)
                            .addOnSuccessListener {
                                onSuccess()
                            }.addOnFailureListener {
                                onFailure(it.message ?: "Firebase Authentication Failed")
                            }
                    } else {
                        onFailure("Unexpected credential type")
                    }
                } catch (e: GetCredentialException) {
                    onFailure(e.message ?: "Credential Manager Error")
                } catch (e: Exception) {
                    onFailure(e.message ?: "An unknown error occurred")
                }
            }
        } else {
            onFailure("Activity must be a ComponentActivity")
        }
    }

    fun signInWithGithub(
        activity: Activity, onSuccess: () -> Unit, onFailure: (String) -> Unit
    ) {
        val provider = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("login", "")

        firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { authResult ->
                authResult.user?.let {
                    onSuccess()
                }
            }.addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

}
