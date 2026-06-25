package com.dhruva.allfirebasesampleapp.service

import android.annotation.SuppressLint
import com.dhruva.allfirebasesampleapp.common.showLogDebug
import com.dhruva.allfirebasesampleapp.common.showNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        //this method is called when the device is registered
        sendRegistrationToServer(installationId)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.notification!=null) {
            this.showLogDebug("received notification is : ${message.notification}")
        }
        if (message.data.isNotEmpty()) {
            this.showLogDebug("received data is : ${message.data}")
        }
        message.notification?.let {
            showNotification(applicationContext, "AllFirebaseApp", it.body.toString())
        }
    }

    override fun onUnregistered(installationId: String) {
        super.onUnregistered(installationId)
        //this method is called when the device is unregistered,
        //so the server should delete the FID - as it's no longer active
        sendRegistrationToServer(installationId)
    }

    fun sendRegistrationToServer(fid: String) {
        //use this code to send token to server
        this.showLogDebug("New fid : $fid")
    }
}