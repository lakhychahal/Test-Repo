package com.example.taskman;

import static com.example.taskman.R.id.task_list_layout;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private EditText taskEditText;
    private Button addButton;
    private LinearLayout taskListLayout;
    private ArrayList<String> taskList;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskEditText = findViewById(R.id.task_edit_text);
        addButton = findViewById(R.id.add_button);
        taskListLayout = findViewById(task_list_layout);

        taskList = new ArrayList<>();
        loadTasks();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = taskEditText.getText().toString();
                if (!task.isEmpty()) {
                    taskList.add(task);
                    addTaskView(task);
                    taskEditText.setText("");
                    saveTasks();

                }
            }
        });
        Button clearAllButton = findViewById(R.id.clearAllButton);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to clear all the tasks
                clearAllTasks();
            }
        });
        Button sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sort the task list alphabetically
                Collections.sort(taskList);

                // Clear the task list layout
                taskListLayout.removeAllViews();

                // Add the tasks to the task list layout
                for (String task : taskList) {
                    addTaskView(task);
                }
            }
        });
    }
    private void addTaskView(final String task) {
        final View taskView = getLayoutInflater().inflate(R.layout.task_layout, null, false);
        TextView taskTextView = taskView.findViewById(R.id.task_text_view);
        taskTextView.setText(task);

        Button editButton = taskView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(taskView, task);
            }
        });
        Button deleteButton = taskView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTaskView(taskView, task);
            }
        });

        taskListLayout.addView(taskView);
    }
    private void removeTaskView(View taskView, String task) {
        taskListLayout.removeView(taskView);
        taskList.remove(task);
        saveTasks();
    }
    private void editTask(final View taskView, final String task) {
        final EditText taskEditText = taskView.findViewById(R.id.task_text_view);
        final Button editButton = taskView.findViewById(R.id.edit_button);
        final Button deleteButton = taskView.findViewById(R.id.delete_button);

        taskEditText.setEnabled(true);
        editButton.setText("Save");

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTask = taskEditText.getText().toString();
                if (!newTask.isEmpty()) {
                    taskList.set(taskList.indexOf(task), newTask);
                    taskEditText.setEnabled(false);
                    editButton.setText("Edit");
                    saveTasks();
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTaskView(taskView, task);
            }
        });

    }

    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> taskSet = new HashSet<>(taskList);
        editor.putStringSet("taskList", taskSet);
        editor.apply();
    }
    private void clearAllTasks() {
        // Clear the HashSet and update the UI
        taskList.clear();
        saveTasks();
        updateUI();
        Toast.makeText(this, "All the tasks are cleared " , Toast.LENGTH_SHORT).show();
    }
    private void updateUI() {
        taskListLayout.removeAllViews();

        for (String task : taskList) {
            View itemView = getLayoutInflater().inflate(R.layout.task_layout, null);

            TextView taskTextView = itemView.findViewById(R.id.task_text_view);
            taskTextView.setText(task);

            ImageView deleteButton = itemView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(v -> {
                taskList.remove(task);
                updateUI();
            });

            taskListLayout.addView(itemView);
        }
    
        }
    private void loadTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE);
        Set<String> taskSet = sharedPreferences.getStringSet("taskList", new HashSet<String>());
        taskList = new ArrayList<>(taskSet);
        for (String task : taskList) {
            addTaskView(task);
        }
    }
}
