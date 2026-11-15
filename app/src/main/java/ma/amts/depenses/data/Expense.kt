package ma.amts.depenses.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val amountCentimes: Long,
    val description: String,
    val chantier: String,
    val funding: String,
    val receiptStatus: String,
    val createdBy: String = "fouad",
    val syncStatus: String = "LOCAL"
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: Expense)
}

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AmtsDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    companion object {
        @Volatile private var instance: AmtsDatabase? = null
        fun get(context: android.content.Context): AmtsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, AmtsDatabase::class.java, "amts_depenses.db"
                ).build().also { instance = it }
            }
    }
}
