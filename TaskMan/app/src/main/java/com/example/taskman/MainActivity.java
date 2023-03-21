package com.example.taskman;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText taskEditText;
    private Button addButton;
    private LinearLayout taskListLayout;
    private ArrayList<String> taskList;
    private static final String CHANNEL_ID = "my_channel_id";
    TextView emptytasks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My notification channel description");

            // Step 5: Register the notification channel
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        emptytasks = findViewById(R.id.emptytasks);

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


    private void addTaskView(final String task) {
        if (task.isEmpty()) {
            emptytasks.setVisibility(View.VISIBLE);
            taskListLayout.setVisibility(View.GONE);
        } else {
            emptytasks.setVisibility(View.GONE);
            taskListLayout.setVisibility(View.VISIBLE);
        }
        final View taskView = getLayoutInflater().inflate(R.layout.task_layout, null, false);
        TextView taskTextView = taskView.findViewById(R.id.task_text_view);
        taskTextView.setText(task);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton remindButton = taskView.findViewById(R.id.reminder_button);
        remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReminderDialog(task);
            }
        });

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


    private void showReminderDialog(final String task) {
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
        builder.setNegativeButton("Alert",new DialogInterface.OnClickListener(){

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Log.d("Reminder", "Alarm set for " + new Date(timeInMillis));
            Toast.makeText(this, getString(R.string.reminder_set), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Reminder", "Error setting alarm", e);
        }
    }

    private void removeTaskView(View taskView, String task) {
        taskListLayout.removeView(taskView);
        taskList.remove(task);
        saveTasks();
        Toast.makeText(this, "Task removed: ", Toast.LENGTH_SHORT).show();
        if (taskList.isEmpty()) {
            emptytasks.setVisibility(View.VISIBLE);
            taskListLayout.setVisibility(View.GONE);
        } else {
            emptytasks.setVisibility(View.GONE);
            taskListLayout.setVisibility(View.VISIBLE);
        }
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
        if (taskList.isEmpty()) {
            emptytasks.setVisibility(View.VISIBLE);
            taskListLayout.setVisibility(View.GONE);
        } else {
            emptytasks.setVisibility(View.GONE);
            taskListLayout.setVisibility(View.VISIBLE);
        }

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
