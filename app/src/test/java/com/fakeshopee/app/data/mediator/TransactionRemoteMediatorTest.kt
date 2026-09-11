package com.fakeshopee.app.data.mediator

import androidx.paging.*
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.RemoteKeyDao
import com.fakeshopee.app.data.local.TransactionDao
import com.fakeshopee.app.data.local.TransactionEntity
import com.fakeshopee.app.data.remote.FakeShopeeApiService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
class TransactionRemoteMediatorTest {

    private val database: AppDatabase = mockk(relaxed = true)
    private val transactionDao: TransactionDao = mockk(relaxed = true)
    private val remoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val apiService: FakeShopeeApiService = mockk(relaxed = true)

    private lateinit var mediator: TransactionRemoteMediator

    @Before
    fun setUp() {
        every { database.transactionDao() } returns transactionDao
        every { database.remoteKeyDao() } returns remoteKeyDao

        mediator = TransactionRemoteMediator(
            database = database,
            apiService = apiService,
            filterType = null
        )
    }

    @Test
    fun testLoadRefreshNetworkErrorEmptyDb() = runTest {
        coEvery { transactionDao.getTransactionCount() } returns 0
        coEvery { apiService.getTransactions(any(), any(), any()) } throws IOException("Network Offline")

        val pagingState = PagingState<Int, TransactionEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun testLoadRefreshNetworkErrorCachedDbFallback() = runTest {
        coEvery { transactionDao.getTransactionCount() } returns 5
        coEvery { apiService.getTransactions(any(), any(), any()) } throws IOException("Network Offline")

        val pagingState = PagingState<Int, TransactionEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
    }
}
