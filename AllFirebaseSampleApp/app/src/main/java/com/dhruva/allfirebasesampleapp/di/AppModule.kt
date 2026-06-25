package com.dhruva.allfirebasesampleapp.di

import com.dhruva.allfirebasesampleapp.data.wrapper.AuthOperationsWrapper
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthOperationsWrapper(firebaseAuth: FirebaseAuth): AuthOperationsWrapper =
        AuthOperationsWrapper(firebaseAuth)
}
