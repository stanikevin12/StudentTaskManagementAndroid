package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.adapter.TaskAdapter;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen that displays all tasks in a RecyclerView.
 * This activity is responsible only for loading tasks and displaying them.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;

    private TaskAdapter taskAdapter;
    private TaskDao taskDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        fabAddTask = findViewById(R.id.fabAddTask);

        taskDao = new TaskDao(this);

        setupRecyclerView();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void setupRecyclerView() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<Task>());
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupActions() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Loads tasks from the DAO and updates the adapter.
     */
    private void loadTasks() {
        List<Task> tasks = taskDao.getAllTasks();
        taskAdapter.setTasks(tasks);
    }
}
