package com.example.sgfuturenursingapp.di

import com.example.sgfuturenursingapp.ui.data.FirestoreTaskRepositoryImpl
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: FirestoreTaskRepositoryImpl,
    ): TaskRepository
}
