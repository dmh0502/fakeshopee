package com.fakeshopee.app.presentation.viewmodel

import app.cash.turbine.test
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.domain.model.TransactionStatus
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.domain.model.Wallet
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import com.fakeshopee.app.presentation.mvi.WalletEffect
import com.fakeshopee.app.presentation.mvi.WalletIntent
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
class WalletViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)

    private val sampleWallet = Wallet(availableBalance = 4250.00, monthlySpend = 2778.00)
    private val sampleTx = Transaction(
        id = "tx-1",
        title = "Top Up Deposit",
        amount = 500.0,
        type = TransactionType.RECHARGE,
        timestamp = System.currentTimeMillis(),
        formattedDate = "Today",
        category = "Recharge",
        merchant = "FakeShopee Treasury",
        referenceId = "FS-DEP-100",
        status = TransactionStatus.COMPLETED,
        iconName = "add_card"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { walletRepository.getWalletStream() } returns flowOf(sampleWallet)
        every { transactionRepository.getRecentTransactionsStream() } returns flowOf(listOf(sampleTx))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `wallet stream updates wallet balance in StateFlow`() = runTest(testDispatcher) {
        val viewModel = WalletViewModel(walletRepository, transactionRepository)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(4250.00, state.wallet.availableBalance, 0.01)
            assertEquals(1, state.recentTransactions.size)
            assertEquals("tx-1", state.recentTransactions[0].id)
        }
    }

    @Test
    fun `QuickAdd intent triggers repository topUp and emits ShowToast effect`() = runTest(testDispatcher) {
        coEvery { walletRepository.topUp(100.0, "Bank Account") } returns Result.success(Unit)
        val viewModel = WalletViewModel(walletRepository, transactionRepository)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(WalletIntent.QuickAdd(100.0, "Bank Account"))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is WalletEffect.ShowToast)
            assertEquals("Added $100.0 to wallet", (effect as WalletEffect.ShowToast).message)
            coVerify(exactly = 1) { walletRepository.topUp(100.0, "Bank Account") }
        }
    }

    @Test
    fun `ShowQuickAddDialog and DismissQuickAddDialog update dialog visibility state`() = runTest(testDispatcher) {
        val viewModel = WalletViewModel(walletRepository, transactionRepository)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isQuickAddDialogVisible)

            viewModel.handleIntent(WalletIntent.ShowQuickAddDialog)
            val openState = awaitItem()
            assertTrue(openState.isQuickAddDialogVisible)

            viewModel.handleIntent(WalletIntent.DismissQuickAddDialog)
            val closeState = awaitItem()
            assertFalse(closeState.isQuickAddDialogVisible)
        }
    }
}
