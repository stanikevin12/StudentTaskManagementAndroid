# StudentTaskManagement — Project Overview

## Short Overview
- Android app (Java + Gradle) for managing tasks, tracking study sessions, and showing a dashboard summary from SQLite data.
- Data access is organized through DAO classes (`TaskDao`, `StudySessionDao`, `TaskNotificationDao`) on top of `AppDatabaseHelper`.
- Task CRUD mainly flows through `TasksActivity`, `AddTaskActivity`, `TaskDetailActivity`, and `EditTaskActivity`.
- Notifications are WorkManager-based: startup initializes channel/scheduling, and a periodic worker reads pending reminder rows and posts notifications.

## Project Structure (Key Paths)
- **Activities (UI screens):** `app/src/main/java/com/example/studenttaskmanagement/activities/`
  - `MainActivity`, `TasksActivity`, `AddTaskActivity`, `TaskDetailActivity`, `EditTaskActivity`, `SettingsActivity`, `StudySessionActivity`.
- **Adapters (RecyclerView binding/navigation):** `app/src/main/java/com/example/studenttaskmanagement/adapter/` (`TaskAdapter`, `StudySessionAdapter`).
- **DAO / DB layer:** `app/src/main/java/com/example/studenttaskmanagement/database/` and `.../database/dao/`.
- **Models:** `app/src/main/java/com/example/studenttaskmanagement/model/` (e.g., `Task`, `TaskNotification`, `StudySession`).
- **Notifications:** `app/src/main/java/com/example/studenttaskmanagement/notifications/`.
- **Dashboard presentation:** `app/src/main/java/com/example/studenttaskmanagement/presentation/dashboard/`.
- **Layouts:** `app/src/main/res/layout/` (notably `activity_tasks.xml`, `activity_add_task.xml`, `activity_task_detail.xml`, `activity_edit_task.xml`, `item_task.xml`).

## Data Model & Tables
Defined in `DatabaseContract` and created in `AppDatabaseHelper.onCreate(...)`.

- **users**: `_id`, `name`, `email`, `created_at`
- **categories**: `_id`, `name`, `color`
- **priorities**: `_id`, `label`
- **tasks**: `_id`, `title`, `description`, `deadline`, `status`, `category_id`, `priority_id`, `user_id`
- **study_sessions**: `_id`, `task_id`, `start_time`, `end_time`, `duration`
- **attachments**: `_id`, `task_id`, `file_path`, `type`
- **notifications**: `_id`, `task_id`, `notify_time`, `is_sent`

Notes:
- Foreign keys are enabled (`onConfigure`) and include task-linked cascades for `study_sessions`, `attachments`, and `notifications`.
- Default seed data includes user, priorities, and categories.

## CRUD Traces

### Create Task
**Flow:**
`activity_add_task.xml` (`editTextTaskTitle`, `editTextTaskDescription`, `editTextTaskDeadline`, `spinnerTaskReminder`, `buttonSaveTask`)  
→ `AddTaskActivity.saveTask()`  
→ `TaskDao.insertTask(Task)`  
→ SQLite `tasks` table (`DatabaseContract.Tasks.TABLE_NAME`)

What happens:
- `buttonSaveTask` triggers `saveTask()`, validates title, builds a `Task`, and inserts it via `taskDao.insertTask(task)`.
- If insert succeeds, `saveReminder(...)` optionally writes/upserts reminder row via `TaskNotificationDao.upsertNotificationForTask(...)` and refreshes worker scheduling.

### Read Tasks (list + detail)
**List flow:**
`activity_tasks.xml` (`recyclerViewTasks`) + `item_task.xml`  
→ `TasksActivity.initDaoAndLoad()` / `reloadTasksAsync()`  
→ `TaskDao.getAllTasks()`  
→ SQLite `tasks` table  
→ `TaskAdapter.onBindViewHolder(...)` binds rows and opens detail screen.

What happens:
- Tasks screen loads all tasks in background and calls `taskAdapter.setTasks(...)`.
- Tapping an item sends `TaskDetailActivity.EXTRA_TASK_ID` in `TaskAdapter` intent.

**Detail flow:**
`activity_task_detail.xml`  
→ `TaskDetailActivity.loadTask()`  
→ `TaskDao.getTaskById(taskId)`  
→ SQLite `tasks` table (plus session count from `StudySessionDao.getCompletedSessionCountForTask(taskId)`).

### Update Task
**Flow:**
`activity_edit_task.xml` (`editTextTaskTitle`, `editTextTaskDescription`, `editTextTaskDeadline`, `spinnerTaskStatus`, `spinnerTaskReminder`, `buttonUpdateTask`)  
→ `EditTaskActivity.updateTask()`  
→ `TaskDao.updateTask(Task)`  
→ SQLite `tasks` table

What happens:
- `EditTaskActivity.loadTask()` first fetches current task via `taskDao.getTaskById(taskId)`.
- On update click, `updateTask()` applies edits and status, calls `taskDao.updateTask(currentTask)`, then updates/deletes reminder row and reschedules worker.

### Delete Task
**Flow:**
`activity_task_detail.xml` (`buttonDeleteTask`)  
→ `TaskDetailActivity.showDeleteConfirmation()` / `deleteTask()`  
→ `TaskDao.deleteTask(taskId)`  
→ SQLite `tasks` table

What happens:
- User confirms in dialog; `deleteTask()` executes DAO delete.
- Due to FK `ON DELETE CASCADE`, linked `study_sessions`, `attachments`, and `notifications` rows are also removed.

## Notifications Flow
- **App startup:** `NotificationStartup.initialize(context)` is called from `MainActivity.onCreate(...)`; it creates the channel and then calls `updateReminderWorkerSchedule(...)`.
- **Worker execution:** `TaskReminderWorker.doWork()`:
  - exits early if `NotificationPreferences.areRemindersEnabled(context)` is false,
  - reads due reminders with `TaskNotificationDao.getPendingNotifications(nowMillis)`,
  - fetches task data via `TaskDao.getTaskById(...)`, posts notification, then calls `TaskNotificationDao.markNotificationAsSent(...)`.
- **Enable/disable prefs:** reminders are toggled in `SettingsActivity.setReminderEnabledInternal(boolean)` via `NotificationPreferences.setRemindersEnabled(...)`; lead-time is stored with `NotificationPreferences.setDefaultLeadTimeMinutes(...)`.
- **Pending-reminder selection from DB:** done in `TaskNotificationDao.getPendingNotifications(long nowMillis)` using `is_sent = 0 AND notify_time <= nowMillis`.

## Quick Run / Entry Points
- **Launcher activity:** `MainActivity` (declared with `MAIN` + `LAUNCHER` in `AndroidManifest.xml`).
- Main navigation:
  - `MainActivity` → `TasksActivity` (see all tasks)
  - `TasksActivity` → `AddTaskActivity` (FAB / empty-state action)
  - `TaskAdapter` item tap → `TaskDetailActivity`
  - `TaskDetailActivity` → `EditTaskActivity` / `StudySessionActivity`
  - Settings available from menu (`SettingsActivity`)
