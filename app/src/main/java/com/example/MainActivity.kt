package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TodoItem
import com.example.ui.TodoViewModel
import com.example.ui.theme.MyApplicationTheme

// Theme color constants from the Sleek Interface spec
val ColorSleekBg = Color(0xFFF7F9FC)
val ColorSleekTextDark = Color(0xFF0F172A)
val ColorSleekTextMuted = Color(0xFF64748B)
val ColorSleekBlueBg = Color(0xFFD3E3FD)
val ColorSleekBlueBorder = Color(0xFFA8C7FA)
val ColorSleekBlueText = Color(0xFF041E49)
val ColorSleekAccent = Color(0xFF0B57D0)
val ColorSleekNavBg = Color(0xFFF3F4F9)
val ColorSleekCardWhite = Color(0xFFFFFFFF)
val ColorSleekBorderLine = Color(0xFFEDEFEF)
val ColorSleekGreen = Color(0xFF16A34A)

class MainActivity : ComponentActivity() {
  private val viewModel: TodoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        TodoAppScreen(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAppScreen(viewModel: TodoViewModel) {
  val todos by viewModel.allTodos.collectAsStateWithLifecycle()
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier
          .fillMaxSize()
          .background(ColorSleekBg),
      bottomBar = {
        SleekNavigationBar(
            currentTab = currentTab,
            onTabSelected = { viewModel.setTab(it) }
        )
      },
      floatingActionButton = {
        if (currentTab != 2) { // Hide FAB on profile/info tab
          FloatingActionButton(
              onClick = { showAddDialog = true },
              containerColor = ColorSleekBlueBg,
              contentColor = ColorSleekBlueText,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                  .padding(bottom = 12.dp)
                  .border(1.dp, ColorSleekBlueBorder, RoundedCornerShape(16.dp))
                  .testTag("add_task_fab"),
              elevation = FloatingActionButtonDefaults.elevation(8.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Task",
                modifier = Modifier.size(28.dp)
            )
          }
        }
      }
  ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSleekBg)
            .padding(innerPadding)
    ) {
      Column(
          modifier = Modifier.fillMaxSize()
      ) {
        // App top Header Bar
        SleekHeader(todos = todos)

        // Main Swipeable/Animated Content based on Tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
          when (currentTab) {
            0 -> HomeScreen(
                todos = todos,
                onToggle = { viewModel.toggleTask(it) },
                onDelete = { viewModel.deleteTask(it) }
            )
            1 -> TasksScreen(
                todos = todos,
                onToggle = { viewModel.toggleTask(it) },
                onDelete = { viewModel.deleteTask(it) }
            )
            2 -> ProfileExplanationScreen(
                todos = todos
            )
          }
        }
      }

      if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, category, time ->
              viewModel.addTask(title, category, time)
              showAddDialog = false
            }
        )
      }
    }
  }
}

@Composable
fun SleekHeader(todos: List<TodoItem>) {
  val completedCount = todos.count { it.completed }
  val totalCount = todos.size

  Row(
      modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Menu Icon block
      Box(
          modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(ColorSleekNavBg)
              .clickable { },
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = ColorSleekTextDark,
            modifier = Modifier.size(22.dp)
        )
      }

      Column {
        Text(
            text = "To-Do List",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSleekTextDark,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Sleek Interface",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ColorSleekAccent,
            modifier = Modifier.offset(y = (-2).dp)
        )
      }
    }

    // Avatar Icon block with completion circle indicator
    Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
      // Draw a subtle radial circular progress around avatar if tasks exist
      if (totalCount > 0) {
        val sweepAngle = (completedCount.toFloat() / totalCount.toFloat()) * 360f
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawCircle(
              color = ColorSleekBlueBorder.copy(alpha = 0.3f),
              radius = size.minDimension / 2,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
          )
          drawArc(
              color = ColorSleekAccent,
              startAngle = -90f,
              sweepAngle = sweepAngle,
              useCenter = false,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
          )
        }
      }

      Box(
          modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(ColorSleekBlueBg)
              .border(2.dp, Color.White, CircleShape),
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = ColorSleekAccent,
            modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun HomeScreen(
    todos: List<TodoItem>,
    onToggle: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
  val quickTasks = todos.filter { it.category == "Quick Task" }
  val plans = todos.filter { it.category == "Today's Plan" }

  LazyColumn(
      modifier = Modifier
          .fillMaxSize()
          .testTag("home_screen_lazy_column"),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
  ) {
    // 1. Widget Preview Section
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Widget Preview",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSleekTextMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        // Widget Container Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSleekBlueBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ColorSleekBlueBorder, RoundedCornerShape(28.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                  text = "Quick Task (Widget)",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = ColorSleekBlueText
              )
              Surface(
                  shape = RoundedCornerShape(100.dp),
                  color = ColorSleekAccent,
                  modifier = Modifier.testTag("widget_active_badge")
              ) {
                Text(
                    text = "Active",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            if (quickTasks.isEmpty()) {
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 12.dp),
                  contentAlignment = Alignment.Center
              ) {
                Text(
                    text = "No quick tasks!",
                    color = ColorSleekBlueText.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
              }
            } else {
              Column(
                  verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                quickTasks.take(3).forEach { item ->
                  WidgetListItem(
                      item = item,
                      onCompletedToggle = { onToggle(item) }
                  )
                }
                if (quickTasks.size > 3) {
                  Text(
                      text = "+ ${quickTasks.size - 3} more widget tasks...",
                      fontSize = 11.sp,
                      color = ColorSleekAccent,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2. Today's Plan Section
    item {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
            text = "Today's Plan",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSleekTextMuted,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Text(
            text = "See All",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorSleekAccent,
            modifier = Modifier.padding(end = 4.dp)
        )
      }
    }

    if (plans.isEmpty()) {
      item {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSleekCardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ColorSleekBorderLine, RoundedCornerShape(16.dp))
        ) {
          Box(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
              contentAlignment = Alignment.Center
          ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                  imageVector = Icons.Outlined.CheckCircle,
                  contentDescription = "Empty List",
                  tint = ColorSleekTextMuted.copy(alpha = 0.5f),
                  modifier = Modifier.size(40.dp)
              )
              Text(
                  text = "No plans set for today.",
                  color = ColorSleekTextMuted,
                  fontSize = 14.sp
              )
            }
          }
        }
      }
    } else {
      items(plans, key = { it.id }) { item ->
        SleekPlanItem(
            item = item,
            onToggle = { onToggle(item) },
            onDelete = { onDelete(item) }
        )
      }
    }
  }
}

@Composable
fun WidgetListItem(
    item: TodoItem,
    onCompletedToggle: () -> Unit
) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color.White.copy(alpha = 0.6f))
          .clickable { onCompletedToggle() }
          .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Sleek Custom Square Box for Quick Check
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(2.dp, ColorSleekAccent, RoundedCornerShape(6.dp))
            .background(
                if (item.completed) ColorSleekAccent else Color.Transparent,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
      if (item.completed) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completed",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
      }
    }

    Text(
        text = item.title,
        color = ColorSleekBlueText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun SleekPlanItem(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
  Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ColorSleekCardWhite),
      modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, ColorSleekBorderLine, RoundedCornerShape(20.dp))
          .testTag("plan_item_${item.id}"),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Circle layout checker
      Box(
          modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .border(
                  width = if (item.completed) 0.dp else 2.dp,
                  color = if (item.completed) Color.Transparent else ColorSleekTextMuted.copy(alpha = 0.4f),
                  shape = CircleShape
              )
              .background(
                  color = if (item.completed) ColorSleekBlueBg else Color.Transparent
              ),
          contentAlignment = Alignment.Center
      ) {
        if (item.completed) {
          Box(
              modifier = Modifier
                  .size(10.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(ColorSleekAccent)
          )
        }
      }

      Column(
          modifier = Modifier.weight(1f)
      ) {
        Text(
            text = item.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.completed) ColorSleekTextMuted else ColorSleekTextDark,
            textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (item.time.isNotEmpty()) {
          Text(
              text = if (item.completed) "${item.time} (Done)" else item.time,
              fontSize = 11.sp,
              fontWeight = FontWeight.Normal,
              color = ColorSleekTextMuted,
              modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      // Action Row containing Delete and check done
      Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        if (item.completed) {
          Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Completed Task",
              tint = ColorSleekGreen,
              modifier = Modifier.size(22.dp)
          )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
          Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = ColorSleekTextMuted.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun TasksScreen(
    todos: List<TodoItem>,
    onToggle: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
  var selectedFilter by remember { mutableStateOf("All") } // All, Plan, Quick

  val filteredTodos = when (selectedFilter) {
    "Plan" -> todos.filter { it.category == "Today's Plan" }
    "Quick" -> todos.filter { it.category == "Quick Task" }
    else -> todos
  }

  val total = filteredTodos.size
  val completed = filteredTodos.count { it.completed }
  val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

  Column(
      modifier = Modifier
          .fillMaxSize()
          .testTag("tasks_screen_root"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Progress Card (Dynamic stats dashboard)
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSleekBlueBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ColorSleekBlueBorder, RoundedCornerShape(24.dp))
    ) {
      Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
                text = "Today's Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSleekBlueText
            )
            Text(
                text = if (total > 0) {
                  "You have completed ${(progress * 100).toInt()}% of your tasks!"
                } else {
                  "No tasks listed"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ColorSleekAccent
            )
          }

          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(50.dp)
          ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = ColorSleekAccent,
                strokeWidth = 6.dp,
                trackColor = ColorSleekBlueBorder.copy(alpha = 0.4f),
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSleekBlueText
            )
          }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = ColorSleekAccent,
            trackColor = ColorSleekBlueBorder.copy(alpha = 0.4f),
        )
      }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SleekFilterChip(
          text = "All Tasks ($total)",
          isSelected = selectedFilter == "All",
          onClick = { selectedFilter = "All" }
      )
      SleekFilterChip(
          text = "Today's Plan",
          isSelected = selectedFilter == "Plan",
          onClick = { selectedFilter = "Plan" }
      )
      SleekFilterChip(
          text = "Quick Tasks",
          isSelected = selectedFilter == "Quick",
          onClick = { selectedFilter = "Quick" }
      )
    }

    // Tasks List
    if (filteredTodos.isEmpty()) {
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
          contentAlignment = Alignment.Center
      ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
              imageVector = Icons.Outlined.List,
              contentDescription = "Empty List",
              tint = ColorSleekTextMuted.copy(alpha = 0.4f),
              modifier = Modifier.size(52.dp)
          )
          Text(
              text = "No tasks found in this category.",
              color = ColorSleekTextMuted,
              fontSize = 14.sp
          )
        }
      }
    } else {
      LazyColumn(
          modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(filteredTodos, key = { it.id }) { item ->
          SleekPlanItem(
              item = item,
              onToggle = { onToggle(item) },
              onDelete = { onDelete(item) }
          )
        }
      }
    }
  }
}

@Composable
fun SleekFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
  Box(
      modifier = Modifier
          .clip(RoundedCornerShape(100.dp))
          .background(if (isSelected) ColorSleekAccent else ColorSleekNavBg)
          .border(
              width = 1.dp,
              color = if (isSelected) ColorSleekAccent else ColorSleekBorderLine,
              shape = RoundedCornerShape(100.dp)
          )
          .clickable { onClick() }
          .padding(horizontal = 14.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center
  ) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else ColorSleekTextMuted
    )
  }
}

@Composable
fun ProfileExplanationScreen(todos: List<TodoItem>) {
  val completed = todos.count { it.completed }
  val total = todos.size
  val remaining = total - completed

  LazyColumn(
      modifier = Modifier
          .fillMaxSize()
          .testTag("profile_explanation_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
  ) {
    // Profile Header Card
    item {
      Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = ColorSleekCardWhite),
          modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, ColorSleekBorderLine, RoundedCornerShape(24.dp))
      ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Large Avatar Block
          Box(
              modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(ColorSleekBlueBg),
              contentAlignment = Alignment.Center
          ) {
            Text(
                text = "M",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSleekAccent
            )
          }

          Column {
            Text(
                text = "Mahadi Hasan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSleekTextDark
            )
            Text(
                text = "mahadimailid@gmail.com",
                fontSize = 13.sp,
                color = ColorSleekTextMuted
            )
          }
        }
      }
    }

    // Stats Grid
    item {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatCard(title = "Total Tasks", value = total.toString(), modifier = Modifier.weight(1f))
        StatCard(title = "Completed", value = completed.toString(), modifier = Modifier.weight(1f))
        StatCard(title = "Remaining", value = remaining.toString(), modifier = Modifier.weight(1f))
      }
    }

    // Widget Guide and App Info Card
    item {
      Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = ColorSleekBlueBg),
          modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, ColorSleekBlueBorder, RoundedCornerShape(24.dp))
      ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "App Widget Guide",
                tint = ColorSleekAccent,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "How to use the Widget",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSleekBlueText
            )
          }

          Divider(color = ColorSleekBlueBorder.copy(alpha = 0.5f))

          Text(
              text = "Follow these steps to add the unchecked tasks list to your Android Home Screen:",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = ColorSleekBlueText
          )

          Surface(
              color = Color.White.copy(alpha = 0.6f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                  text = "Step-by-Step Guide:",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = ColorSleekTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                  text = "1. Long press any blank area on your Home Screen.\n" +
                         "2. Select 'Widgets' or 'Add widgets' from the pop-up menu.\n" +
                         "3. Find 'To-Do List' in the list of available apps.\n" +
                         "4. Drag the 'Unchecked Tasks' widget onto your Home Screen.\n" +
                         "5. Resize the widget to your liking!",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = ColorSleekTextDark,
                  lineHeight = 18.sp
              )
            }
          }

          Text(
              text = "Features & Functionality:",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = ColorSleekBlueText
          )

          Column(
              verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            BulletItem(text = "Unchecked Task Sync: Shows only incomplete tasks directly on your home screen.")
            BulletItem(text = "Categorization Support: Supports 'Quick Tasks' or 'Today's Plan' items seamlessly.")
            BulletItem(text = "One-Tap Launch: Tap any item or the 'Open App' button to instantly open the main tracker.")
          }
        }
      }
    }
  }
}

@Composable
fun BulletItem(text: String) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.Top
  ) {
    Text(
        text = "•",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = ColorSleekAccent
    )
    Text(
        text = text,
        fontSize = 13.sp,
        color = ColorSleekBlueText.copy(alpha = 0.85f),
        lineHeight = 18.sp
    )
  }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = ColorSleekCardWhite),
      modifier = modifier.border(1.dp, ColorSleekBorderLine, RoundedCornerShape(16.dp))
  ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = ColorSleekTextMuted
      )
      Text(
          text = value,
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          color = ColorSleekAccent
      )
    }
  }
}

@Composable
fun SleekNavigationBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
  // Navigation component built perfectly as M3 specifications matching the Sleek layout
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .background(ColorSleekNavBg)
  ) {
    Divider(color = ColorSleekBorderLine, thickness = 1.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
      SleekNavItem(
          iconSelected = Icons.Filled.Home,
          iconUnselected = Icons.Outlined.Home,
          label = "Home",
          isSelected = currentTab == 0,
          onClick = { onTabSelected(0) },
          testTag = "nav_home_tab"
      )

      SleekNavItem(
          iconSelected = Icons.Filled.List,
          iconUnselected = Icons.Outlined.List,
          label = "Tasks",
          isSelected = currentTab == 1,
          onClick = { onTabSelected(1) },
          testTag = "nav_tasks_tab"
      )

      SleekNavItem(
          iconSelected = Icons.Filled.Person,
          iconUnselected = Icons.Outlined.Person,
          label = "Profile",
          isSelected = currentTab == 2,
          onClick = { onTabSelected(2) },
          testTag = "nav_profile_tab"
      )
    }
  }
}

// Adjusting custom navigation method compile helper
@Composable
fun SleekNavItem(
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
  Column(
      modifier = Modifier
          .testTag(testTag)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onClick() }
          .width(80.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
  ) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ColorSleekBlueBg else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
      Icon(
          imageVector = if (isSelected) iconSelected else iconUnselected,
          contentDescription = label,
          tint = if (isSelected) ColorSleekAccent else ColorSleekTextDark.copy(alpha = 0.6f),
          modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = ColorSleekTextDark.copy(alpha = if (isSelected) 1f else 0.5f),
        letterSpacing = (-0.1).sp
    )
  }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Today's Plan") } // "Today's Plan" or "Quick Task"
  var time by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSleekCardWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ColorSleekBorderLine, RoundedCornerShape(28.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
            text = "Add New Task",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSleekTextDark
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title", color = ColorSleekTextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorSleekAccent,
                unfocusedBorderColor = ColorSleekBorderLine,
                focusedLabelColor = ColorSleekAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_task_title_input")
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time or Note (e.g., at 2:00 PM)", color = ColorSleekTextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorSleekAccent,
                unfocusedBorderColor = ColorSleekBorderLine,
                focusedLabelColor = ColorSleekAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_task_time_input")
        )

        // Category Chooser Pills
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
              text = "Category",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = ColorSleekTextMuted
          )
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (category == "Today's Plan") ColorSleekBlueBg else ColorSleekNavBg)
                    .border(
                        1.dp,
                        if (category == "Today's Plan") ColorSleekBlueBorder else ColorSleekBorderLine,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { category = "Today's Plan" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
              Text(
                  text = "Today's Plan",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (category == "Today's Plan") ColorSleekAccent else ColorSleekTextMuted
              )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (category == "Quick Task") ColorSleekBlueBg else ColorSleekNavBg)
                    .border(
                        1.dp,
                        if (category == "Quick Task") ColorSleekBlueBorder else ColorSleekBorderLine,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { category = "Quick Task" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
              Text(
                  text = "Quick Task (Widget)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (category == "Quick Task") ColorSleekAccent else ColorSleekTextMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorSleekTextMuted),
              modifier = Modifier.weight(1f),
              border = androidx.compose.foundation.BorderStroke(1.dp, ColorSleekBorderLine)
          ) {
            Text("Cancel", fontWeight = FontWeight.Bold)
          }

          Button(
              onClick = {
                if (title.isNotBlank()) {
                  val timeStr = time.ifBlank { "Any time" }
                  onAdd(title, category, timeStr)
                }
              },
              enabled = title.isNotBlank(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                  containerColor = ColorSleekAccent,
                  contentColor = Color.White
              ),
              modifier = Modifier
                  .weight(1f)
                  .testTag("add_task_confirm_button")
          ) {
            Text("Add Task", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
