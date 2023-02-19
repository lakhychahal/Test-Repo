package com.example.taskman;

import static com.example.taskman.R.id.task_list_layout;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
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

        taskListLayout.addView(taskView);
    }
    private void editTask(final View taskView, final String task) {
        final EditText taskEditText = taskView.findViewById(R.id.task_text_view);
        final Button editButton = taskView.findViewById(R.id.edit_button);


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
    }
    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> taskSet = new HashSet<>(taskList);
        editor.putStringSet("taskList", taskSet);
        editor.apply();
    }
}
