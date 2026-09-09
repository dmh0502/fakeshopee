package com.fakeshopee.app.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.TransactionEntity
import com.fakeshopee.app.data.local.TransactionRemoteKeyEntity
import com.fakeshopee.app.data.remote.FakeShopeeApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediator(
    private val database: AppDatabase,
    private val apiService: FakeShopeeApiService,
    private val filterType: String?
) : RemoteMediator<Int, TransactionEntity>() {

    private val transactionDao = database.transactionDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevKey
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val response = try {
                apiService.getTransactions(
                    page = page,
                    pageSize = state.config.pageSize,
                    type = filterType
                )
            } catch (e: Exception) {
                if (transactionDao.getTransactionCount() > 0) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    return MediatorResult.Error(e)
                }
            }

            if (!response.isSuccessful || response.body() == null) {
                if (transactionDao.getTransactionCount() > 0) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                return MediatorResult.Error(HttpException(response))
            }

            val pagedData = response.body()!!
            val items = pagedData.items
            val endOfPaginationReached = !pagedData.hasMore || items.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.clearRemoteKeys()
                    transactionDao.clearTransactions()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = items.map {
                    TransactionRemoteKeyEntity(
                        transactionId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                val entities = items.map { dto ->
                    TransactionEntity(
                        id = dto.id,
                        title = dto.title,
                        amount = dto.amount,
                        type = dto.type,
                        timestamp = dto.timestamp,
                        formattedDate = dto.formattedDate,
                        category = dto.category,
                        merchant = dto.merchant,
                        referenceId = dto.referenceId,
                        status = dto.status,
                        iconName = dto.iconName
                    )
                }

                remoteKeyDao.insertAll(keys)
                transactionDao.insertTransactions(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TransactionEntity>): TransactionRemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { tx ->
            remoteKeyDao.getRemoteKey(tx.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, TransactionEntity>): TransactionRemoteKeyEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { tx ->
            remoteKeyDao.getRemoteKey(tx.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, TransactionEntity>): TransactionRemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                remoteKeyDao.getRemoteKey(id)
            }
        }
    }
}
