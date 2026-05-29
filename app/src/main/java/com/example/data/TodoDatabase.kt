package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val completed: Boolean = false,
    val time: String = "",
    val category: String = "Today's Plan", // "Today's Plan" or "Quick Task"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY timestamp DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE completed = 0 ORDER BY timestamp DESC")
    fun getUncompletedTodosSync(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItem)

    @Update
    suspend fun updateTodo(todo: TodoItem)

    @Delete
    suspend fun deleteTodo(todo: TodoItem)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoById(id: Int)
}

@Database(entities = [TodoItem::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()

    fun getUncompletedTodosSync(): List<TodoItem> {
        return todoDao.getUncompletedTodosSync()
    }

    suspend fun insert(todo: TodoItem) {
        todoDao.insertTodo(todo)
    }

    suspend fun update(todo: TodoItem) {
        todoDao.updateTodo(todo)
    }

    suspend fun delete(todo: TodoItem) {
        todoDao.deleteTodo(todo)
    }

    suspend fun deleteById(id: Int) {
        todoDao.deleteTodoById(id)
    }
}
