package com.example.taskman;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    public static final String APP_PREFS = "app-preferences";
    public static final String TASKS = "tasks";
    public static final String START_DATES = "start-dates";
    public static final String TASKS_COMPLETED = "tasks-completed";

    public static final String SORTED = "tasks-sorted";

    private static final String CHANNEL_ID = "my_channel_id";
    private EditText taskEditText;

    private final List<Task> tasks = new ArrayList<>();

    private TasksAdapter adapter;
    TextView emptytasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My notification channel description");


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        emptytasks = findViewById(R.id.emptytasks);

        taskEditText = findViewById(R.id.task_edit_text);
        ImageButton addButton = findViewById(R.id.add_button);


        adapter = new TasksAdapter(this::updateCompletionStatus,
                this::updateStartDate,
                this::deleteTask,
                this::updateTask,
                this::taskNotification);
        RecyclerView tasksRecyclerView = findViewById(R.id.tasks);
        tasksRecyclerView.setAdapter(adapter);


        loadTasks();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String task = taskEditText.getText().toString();
                if (!task.isEmpty()) {
                    saveTask(task);
                }

            }
        });
        Button clearAllButton = findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllTasks();
            }
        });
        ImageButton sortButton = findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = preferences().edit();
                editor.putBoolean(SORTED, true);
                editor.apply();
                reconstructTasks();
            }
        });

        Button viewCompletedTasks = findViewById(R.id.view_completed_tasks);
        viewCompletedTasks.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CompletedTasksActivity.class));
        });

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("task man_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showReminderDialog(final Task myTask) {
        final String task = myTask.getTask();
        final View dialogView = getLayoutInflater().inflate(R.layout.reminder_dialog, null);
        final DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.set_reminder));
        builder.setView(dialogView);
        builder.setPositiveButton("Alarm", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_DAYS, day);
                intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
                intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
                startActivity(intent);


            }
        });
        builder.setNegativeButton("Alert", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                setReminder(task, calendar.getTimeInMillis());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setReminder(String task, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("task", task);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Log.d("Reminder", "Alarm set for " + new Date(timeInMillis));
            Toast.makeText(this, getString(R.string.reminder_set), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Reminder", "Error setting alarm", e);
        }
    }

    private void saveTask(String task) {

        Log.i(TAG, "saveTask: saving task " + task);
        SharedPreferences preferences = preferences();
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set = new HashSet<>(preferences.getStringSet(TASKS, new HashSet<>()));
        Log.i(TAG, "saveTask: old set size = " + set.size());
        String oldSet = Arrays.deepToString(set.toArray(new String[0]));
        Log.i(TAG, "saveTask: old set = \n" + oldSet);
        set.add(task);
        Log.i(TAG, "saveTask: new set size = " + set.size());
        editor.putStringSet(TASKS, set);
        editor.apply();

        reconstructTasks();

        taskEditText.setText("");
    }


    private void loadTasks() {

        reconstructTasks();
    }

    private void clearAllTasks() {

        SharedPreferences[] preferences = new SharedPreferences[]{
                preferences(),
                taskStartDatesPreferences(),
                taskCompletionStatusPreferences()
        };
        for (SharedPreferences preference : preferences) {

            Set<String> keys = preference.getAll().keySet();
            SharedPreferences.Editor editor = preference.edit();
            for (String key : keys) {
                editor.remove(key);
            }
            editor.apply();
        }
        reconstructTasks();
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

    @Nullable
    private Void updateCompletionStatus(@NonNull Task task) {


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

        showReminderDialog(task);
        return null;
    }

    private void reconstructTasks() {

        tasks.clear();
        final SharedPreferences preferences = preferences();
        Set<String> taskNamesSet = preferences.getStringSet(TASKS, new HashSet<>());
        final SharedPreferences taskStartDatesPreferences = taskStartDatesPreferences();
        final SharedPreferences taskCompletionStatusPreferences = taskCompletionStatusPreferences();

        List<Task> tasks = new ArrayList<>();
        for (String s : taskNamesSet) {
            boolean isTaskCompleted = taskCompletionStatusPreferences.getBoolean(s, false);
            long date = taskStartDatesPreferences.getLong(s, -1);
            Date d;
            if (date == -1) d = null;
            else d = new Date(date);
            if (!isTaskCompleted)
                tasks.add(new Task(s, false, d));
        }

        if (preferences.getBoolean(SORTED, false)) {

            Collections.sort(tasks);
        }

        adapter.submitList(tasks);


        this.tasks.addAll(tasks);

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
}
