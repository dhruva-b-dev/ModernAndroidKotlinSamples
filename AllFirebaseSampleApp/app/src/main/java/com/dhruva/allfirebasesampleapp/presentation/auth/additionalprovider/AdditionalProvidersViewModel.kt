package com.dhruva.allfirebasesampleapp.presentation.auth.additionalprovider

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.dhruva.allfirebasesampleapp.data.wrapper.AuthOperationsWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdditionalProvidersViewModel @Inject constructor(
    private val authOperationsWrapper: AuthOperationsWrapper
) : ViewModel() {

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authOperationsWrapper.signInWithGoogle(activity, onSuccess, onFailure)
    }

    fun signInWithGithub(activity: Activity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authOperationsWrapper.signInWithGithub(activity, onSuccess, onFailure)
    }
}
