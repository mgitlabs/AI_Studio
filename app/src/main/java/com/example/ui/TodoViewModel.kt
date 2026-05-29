package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TodoDatabase
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    val allTodos: StateFlow<List<TodoItem>>

    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Tasks, 2: Profile/Explanation
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        allTodos = repository.allTodos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial data if database is empty on startup
        viewModelScope.launch {
            val list = repository.allTodos.first()
            if (list.isEmpty()) {
                seedDefaultData()
            }
        }
    }

    private suspend fun seedDefaultData() {
        val defaults = listOf(
            TodoItem(title = "Create grocery list", completed = false, time = "at 9:00 AM", category = "Quick Task"),
            TodoItem(title = "Office presentation prep", completed = false, time = "at 10:00 AM", category = "Quick Task"),
            TodoItem(title = "Take afternoon medicine", completed = false, time = "at 2:00 PM", category = "Today's Plan"),
            TodoItem(title = "Go for a healthy run", completed = true, time = "at 7:30 AM", category = "Today's Plan")
        )
        for (item in defaults) {
            repository.insert(item)
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    private fun updateWidget() {
        val context = getApplication<Application>()
        val intent = android.content.Intent(context, com.example.widget.TodoWidgetProvider::class.java).apply {
            action = "com.example.widget.UPDATE_WIDGET"
        }
        context.sendBroadcast(intent)
    }

    fun addTask(title: String, category: String, time: String) {
        viewModelScope.launch {
            repository.insert(
                TodoItem(
                    title = title,
                    completed = false,
                    time = time,
                    category = category
                )
            )
            updateWidget()
        }
    }

    fun toggleTask(item: TodoItem) {
        viewModelScope.launch {
            repository.update(item.copy(completed = !item.completed))
            updateWidget()
        }
    }

    fun deleteTask(item: TodoItem) {
        viewModelScope.launch {
            repository.delete(item)
            updateWidget()
        }
    }
}
