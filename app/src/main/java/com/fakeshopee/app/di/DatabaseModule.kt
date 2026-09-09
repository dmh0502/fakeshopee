package com.fakeshopee.app.di

import android.content.Context
import androidx.room.Room
import com.fakeshopee.app.data.local.*
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fakeshopee_ledger.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideRemoteKeyDao(database: AppDatabase): RemoteKeyDao = database.remoteKeyDao()

    @Provides
    fun provideWalletDao(database: AppDatabase): WalletDao = database.walletDao()
}
