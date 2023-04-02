package com.example.taskman;

import static com.example.taskman.MainActivity.APP_PREFS;
import static com.example.taskman.MainActivity.PRIORITIES;
import static com.example.taskman.MainActivity.START_DATES;
import static com.example.taskman.MainActivity.TASKS;
import static com.example.taskman.MainActivity.TASKS_COMPLETED;
import static com.example.taskman.TaskPriority.NONE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompletedTasksActivity extends AppCompatActivity {
    private static final String TAG = "CompletedTasksActivity";

    private TasksAdapter adapter;
    private TextView emptytasks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        emptytasks = findViewById(R.id.empty_completed_tasks);

        adapter = new TasksAdapter(this::updateCompletionStatus,
                this::updateStartDate,
                this::deleteTask,
                this::updateTask,
                this::taskNotification,
                this::updatePriority);
        RecyclerView recyclerView = findViewById(R.id.completed_tasks);
        recyclerView.setAdapter(adapter);

        reconstructTasks();

        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Nullable
    private Void updateTask(@NonNull Task oldTask, @NonNull Task newTask) {

        SharedPreferences preferences = preferences();
        Set<String> taskNamesSet = preferences.getStringSet(TASKS, new HashSet<>());
        List<String> taskNames = new ArrayList<>(taskNamesSet);
        int i = taskNames.indexOf(oldTask.getTask());
        taskNames.set(i, newTask.getTask());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(TASKS, new HashSet<>(taskNames));
        editor.apply();

        reconstructTasks();
        return null;
    }

    @Nullable
    private Void updateStartDate(@NonNull Task task) {


        SharedPreferences datesPreferences = taskStartDatesPreferences();
        SharedPreferences.Editor editor = datesPreferences.edit();
        Date startDate = task.getStartDate();
        assert startDate != null;
        editor.putLong(task.getTask(), startDate.getTime());
        editor.apply();
        reconstructTasks();
        return null;
    }

    public Void updateCompletionStatus(@NonNull Task task) {

        SharedPreferences preferences = taskCompletionStatusPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(task.getTask(), task.isCompleted());
        editor.apply();
        Log.i(TAG, "updateCompletionStatus: task = " + task);
        reconstructTasks();
        return null;
    }

    @Nullable
    private Void deleteTask(@NonNull Task task) {


        SharedPreferences preferences = preferences();
        SharedPreferences completionStatusPreferences = taskCompletionStatusPreferences();
        SharedPreferences taskStartDatesPreferences = taskStartDatesPreferences();

        final String taskName = task.getTask();
        HashSet<String> tasks = new HashSet<>(preferences.getStringSet(TASKS, new HashSet<>()));
        tasks.remove(taskName);
        SharedPreferences.Editor editor1 = preferences.edit();
        editor1.putStringSet(TASKS, tasks);
        editor1.apply();


        SharedPreferences[] prefs = new SharedPreferences[]{completionStatusPreferences, taskStartDatesPreferences};
        for (SharedPreferences pref : prefs) {
            SharedPreferences.Editor editor = pref.edit();
            editor.remove(taskName);
            editor.apply();
        }
        reconstructTasks();
        return null;
    }

    @Nullable
    private Void taskNotification(Task task) {

        return null;
    }

    @Nullable
    private Void updatePriority(@NonNull Task task) {
        SharedPreferences priorityPreferences = taskPriorityPreferences();
        SharedPreferences.Editor editor = priorityPreferences.edit();
        editor.putString(task.getTask(), task.getPriority().name());
        editor.apply();
        reconstructTasks();
        return null;
    }

    private void reconstructTasks() {


        final SharedPreferences preferences = preferences();
        Set<String> taskNamesSet = preferences.getStringSet(TASKS, new HashSet<>());
        final SharedPreferences taskStartDatesPreferences = taskStartDatesPreferences();
        final SharedPreferences taskCompletionStatusPreferences = taskCompletionStatusPreferences();
        final SharedPreferences priorityPreferences = taskPriorityPreferences();

        List<Task> tasks = new ArrayList<>();
        for (String s : taskNamesSet) {
            boolean isTaskCompleted = taskCompletionStatusPreferences.getBoolean(s, false);
            long date = taskStartDatesPreferences.getLong(s, -1);
            Date d;
            if (date == -1) d = null;
            else d = new Date(date);
            TaskPriority priority = TaskPriority.valueOf(priorityPreferences.getString(s, NONE.name()));
            if (isTaskCompleted)
                tasks.add(new Task(s, true, d, priority));
        }
        Log.i(TAG, "reconstructTasks: list size " + tasks.size());
        adapter.submitList(tasks);

        int taskMessageVisibility;
        if (tasks.isEmpty()) taskMessageVisibility = View.VISIBLE;
        else taskMessageVisibility = View.GONE;
        emptytasks.setVisibility(taskMessageVisibility);
    }


    private SharedPreferences preferences() {

        return getSharedPreferences(APP_PREFS, MODE_PRIVATE);
    }

    private SharedPreferences taskCompletionStatusPreferences() {

        return getSharedPreferences(TASKS_COMPLETED, MODE_PRIVATE);
    }

    private SharedPreferences taskStartDatesPreferences() {

        return getSharedPreferences(START_DATES, MODE_PRIVATE);
    }

    private SharedPreferences taskPriorityPreferences() {
        return getSharedPreferences(PRIORITIES, MODE_PRIVATE);
    }
}

