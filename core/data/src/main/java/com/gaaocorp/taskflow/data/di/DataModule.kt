package com.gaaocorp.taskflow.data.di

import android.content.Context
import androidx.room.Room
import com.gaaocorp.taskflow.data.local.TaskDao
import com.gaaocorp.taskflow.data.local.TaskFlowDatabase
import com.gaaocorp.taskflow.data.repository.TaskRepositoryImpl
import com.gaaocorp.taskflow.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskFlowDatabase =
        Room.databaseBuilder(context, TaskFlowDatabase::class.java, "taskflow.db")
            .build()

    @Provides
    fun provideTaskDao(database: TaskFlowDatabase): TaskDao = database.taskDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
