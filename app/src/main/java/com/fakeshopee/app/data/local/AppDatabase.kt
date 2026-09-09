package com.fakeshopee.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val imagesJson: String,
    val specsJson: String,
    val highlightsJson: String,
    val variantsJson: String,
    val inStock: Boolean,
    val stockQuantity: Int,
    val isFavorite: Boolean = false,
    val reviewsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val quantity: Int,
    val selectedColor: String,
    val inStock: Boolean
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String, // RECHARGE, PAYMENT, REFUND
    val timestamp: Long,
    val formattedDate: String,
    val category: String,
    val merchant: String?,
    val referenceId: String,
    val status: String,
    val iconName: String
)

@Entity(tableName = "transaction_remote_keys")
data class TransactionRemoteKeyEntity(
    @PrimaryKey val transactionId: String,
    val prevKey: Int?,
    val nextKey: Int?
)

@Entity(tableName = "wallet_info")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val availableBalance: Double,
    val monthlySpend: Double
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY title ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("DELETE FROM products")
    suspend fun clearAll()
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun getCartItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getPagedTransactions(): androidx.paging.PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getPagedTransactionsByType(type: String): androidx.paging.PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<TransactionRemoteKeyEntity>)

    @Query("SELECT * FROM transaction_remote_keys WHERE transactionId = :id")
    suspend fun getRemoteKey(id: String): TransactionRemoteKeyEntity?

    @Query("DELETE FROM transaction_remote_keys")
    suspend fun clearRemoteKeys()
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_info WHERE id = 1")
    fun getWallet(): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet_info WHERE id = 1")
    suspend fun getWalletOnce(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWallet(wallet: WalletEntity)
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        TransactionEntity::class,
        TransactionRemoteKeyEntity::class,
        WalletEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun transactionDao(): TransactionDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun walletDao(): WalletDao
}
