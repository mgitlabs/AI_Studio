package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.TodoDatabase
import com.example.data.TodoItem

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoWidgetFactory(applicationContext)
    }
}

class TodoWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var todoList = listOf<TodoItem>()

    override fun onCreate() {
        // Initialization can happen in onDataSetChanged
    }

    override fun onDataSetChanged() {
        // Query database synchronistically to avoid Flow blockages on the remote rendering binder
        try {
            val db = TodoDatabase.getDatabase(context)
            todoList = db.todoDao().getUncompletedTodosSync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        todoList = emptyList()
    }

    override fun getCount(): Int {
        return todoList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= todoList.size) return null

        val item = todoList[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item)
        views.setTextViewText(R.id.item_title, item.title)
        views.setTextViewText(R.id.item_time, item.time.ifBlank { "No time specified" })
        views.setTextViewText(R.id.item_category, item.category)

        // Set a fill-in intent so that clicking on a specific list item opens the app
        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return todoList.getOrNull(position)?.id?.toLong() ?: position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
