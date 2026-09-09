package com.fakeshopee.app.presentation.viewmodel

import androidx.paging.PagingData
import app.cash.turbine.test
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.domain.usecase.GetPagedTransactionsUseCase
import com.fakeshopee.app.presentation.mvi.HistoryEffect
import com.fakeshopee.app.presentation.mvi.HistoryIntent
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPagedTransactionsUseCase: GetPagedTransactionsUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getPagedTransactionsUseCase(any()) } returns flowOf(PagingData.empty())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ChangeFilter intent updates selectedFilter in StateFlow and triggers useCase`() = runTest(testDispatcher) {
        val viewModel = HistoryViewModel(getPagedTransactionsUseCase)

        viewModel.state.test {
            val initial = awaitItem()
            assertNull(initial.selectedFilter)

            viewModel.handleIntent(HistoryIntent.ChangeFilter(TransactionType.RECHARGE))
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(TransactionType.RECHARGE, updated.selectedFilter)
        }
    }

    @Test
    fun `Refresh intent emits ShowToast effect`() = runTest(testDispatcher) {
        val viewModel = HistoryViewModel(getPagedTransactionsUseCase)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(HistoryIntent.Refresh)
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HistoryEffect.ShowToast)
            assertEquals("Refreshed transaction ledger", (effect as HistoryEffect.ShowToast).message)
        }
    }
}
