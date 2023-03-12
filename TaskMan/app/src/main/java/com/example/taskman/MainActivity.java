package com.example.taskman;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText taskEditText;
    private Button addButton;
    private LinearLayout taskListLayout;
    private ArrayList<String> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskEditText = findViewById(R.id.task_edit_text);
        ImageButton addButton = (ImageButton) findViewById(R.id.add_button);
        taskListLayout = findViewById(R.id.task_list_layout);

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
        Button clearAllButton = findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to clear all the tasks
                clearAllTasks();
            }
        });
        ImageButton sortButton = (ImageButton) findViewById(R.id.sort_button);
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

        final CheckBox checkBox = taskView.findViewById(R.id.task_checkbox);
        boolean isChecked = checkBox.isChecked();
        checkBox.setChecked(isChecked);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveTaskState(task, isChecked);

            }
        });


        ImageButton editButton = taskView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(taskView, task);
            }
        });

        ImageButton deleteButton = taskView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTaskView(taskView, task);
            }
        });

        taskListLayout.addView(taskView);
        Toast.makeText(this, "Task added: ", Toast.LENGTH_SHORT).show();
    }

    private void removeTaskView(View taskView, String task) {
        taskListLayout.removeView(taskView);
        taskList.remove(task);
        saveTasks();
        Toast.makeText(this, "Task removed: ", Toast.LENGTH_SHORT).show();
    }

    private View selectedTaskView = null;

    private void editTask(final View taskView, final String task) {
        final EditText taskEditText = taskView.findViewById(R.id.task_text_view);
        taskEditText.setEnabled(!taskEditText.isEnabled());
        final ImageButton editButton = taskView.findViewById(R.id.edit_button);
        final ImageButton deleteButton = taskView.findViewById(R.id.delete_button);

        if (selectedTaskView != null && selectedTaskView != taskView) {
            EditText selectedEditText = selectedTaskView.findViewById(R.id.task_text_view);
            selectedEditText.setEnabled(false);
        }
        taskEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = taskEditText.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(text)
                        .setTitle("Full View of Task")
                        .setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTask = taskEditText.getText().toString();
                if (!newTask.isEmpty()) {
                    taskList.set(taskList.indexOf(task), newTask);
                    taskEditText.setEnabled(true);
                    saveTasks();
                }
            }
        });
        taskEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) { // If the EditText loses focus
                    EditText editText = (EditText) view;
                    if (TextUtils.isEmpty(editText.getText())) { // If the EditText is empty
                        editText.setEnabled(false); // Lock the EditText again
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTaskView(taskView, task);
            }
        });

        selectedTaskView = taskView;

        Toast.makeText(this, "Edit Enable " , Toast.LENGTH_SHORT).show();
    }


    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> taskSet = new HashSet<>(taskList);
        editor.putStringSet("taskList", taskSet);
        editor.apply();

    }

    private void loadTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE);
        Set<String> taskSet = sharedPreferences.getStringSet("taskList", new HashSet<String>());
        taskList = new ArrayList<>(taskSet);
        for (String task : taskList) {
            addTaskView(task);
            getTaskState(task);
        }
    }
    private void saveTaskState(String task, boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences("task_state", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(task, isChecked);

        editor.apply();
    }

    private boolean getTaskState(String task) {
        SharedPreferences sharedPreferences = getSharedPreferences("task_state", MODE_PRIVATE);
        return sharedPreferences.getBoolean(task, false);
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
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskList.remove(task);
                    updateUI();
                }
            });

            taskListLayout.addView(itemView);
        }
    }


}
