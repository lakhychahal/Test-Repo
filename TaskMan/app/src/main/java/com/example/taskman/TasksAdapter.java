package com.example.taskman;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TasksAdapter extends ListAdapter<Task, TasksAdapter.TaskViewHolder> {
    // it handles the tasks and their views
    private static final String TAG = "TasksAdapter";


    private static final DiffUtil.ItemCallback<Task> DIFF_UTIL = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTask().equals(newItem.getTask());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.equals(newItem);
        }
    };

    // this function will listen to the checked status change, update the state and then update the view.
    @NotNull
    private final Function<Task, Void> onTaskCheckedListener;
    // this function listens to start date update events
    @NotNull
    private final Function<Task, Void> onTaskStartDateSetListener;
    // this function listens to task deletion events
    @NotNull
    private final Function<Task, Void> onTaskDeletedListener;
    // this functions has two parameters and it's the one use to listen to when a task is edited
    @NotNull
    private final BiFunction<Task, Task, Void> onTaskUpdatedListener;
    // this function is used to listen to the notification event
    @NotNull
    private final Function<Task, Void> onTaskNotificationListener;

    // this is constructor receives the said fuctions as parameters
    public TasksAdapter(@NotNull Function<Task, Void> onTaskCheckedListener,
                        @NotNull Function<Task, Void> onTaskStartDateSetListener,
                        @NotNull Function<Task, Void> onTaskDeletedListener,
                        @NotNull BiFunction<Task, Task, Void> onTaskUpdatedListener,
                        @NotNull Function<Task, Void> onTaskNotificationListener) {
        super(DIFF_UTIL);
        this.onTaskCheckedListener = onTaskCheckedListener;
        this.onTaskStartDateSetListener = onTaskStartDateSetListener;
        this.onTaskDeletedListener = onTaskDeletedListener;
        this.onTaskUpdatedListener = onTaskUpdatedListener;
        this.onTaskNotificationListener = onTaskNotificationListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // creates a viewholder
        // the viewholder holds the view of a task at a given position in a list
        // the viewholder is fed with data by the following method below.
        //ie. the onBindViewHolder


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        // feeds the respective data into the respective view
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final EditText taskTextView;
        private final Button setStartDate;
        private final ImageButton reminderButton;
        private final CheckBox taskCompletedCheckBox;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        public TaskViewHolder(View view) {
            super(view);

            taskTextView = itemView.findViewById(R.id.task_text_view);
            setStartDate = itemView.findViewById(R.id.button);
            reminderButton = itemView.findViewById(R.id.reminder_button);
            taskCompletedCheckBox = itemView.findViewById(R.id.task_checkbox);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(@NonNull Task task) {

            // this method sets up the the task to its respective view
            // e.g. setting the task name, background, checked status e.t.c
            // it also listens to the events on the given task eg deleting, editing,
            // updating the completion status etc

            taskTextView.setText(task.getTask());

            // get the updated task state and set the respective background for the task
            // based on the state.
            final int id;
            if (task.isCompleted()) {
                id = R.drawable.rounded_edittext_background1;
            } else {
                id = R.drawable.rounded_edittext_background2;
            }
            final Drawable background = ResourcesCompat.getDrawable(
                    this.itemView.getResources(),
                    id,
                    this.itemView.getContext().getTheme());
            this.itemView.setBackground(background);

            String dateText;
            Date date = task.getStartDate();
            if (date == null) {
                // there is no date set hence we prompt the user to set the date
                dateText = itemView.getContext().getString(R.string.start_date);
                setStartDate.setOnClickListener(v -> {
                    Log.d(TAG, "bind: set start date");
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            this.itemView.getContext(), (view, year1, month1, dayOfMonth1) -> {
                        int p = getAdapterPosition();
                        if (p == RecyclerView.NO_POSITION) return;

                        // extract the date set
                        Calendar c1 = Calendar.getInstance();
                        c1.set(Calendar.YEAR, year1);
                        c1.set(Calendar.MONTH, month1);
                        c1.set(Calendar.DAY_OF_MONTH, dayOfMonth1);

                        // update the task item to have the new date
                        Task item = getItem(p).setStartDate(c1.getTime());
                        onTaskStartDateSetListener.apply(item);
                    }, year, month, dayOfMonth);

                    datePickerDialog.show();
                });
            } else {
                dateText = new SimpleDateFormat("MMM d", Locale.getDefault()).format(task.getStartDate());
            }
            setStartDate.setText(dateText);
            reminderButton.setOnClickListener(v -> {
                Task task1 = getTask();
                if (task1 == null) {
                    return;
                }
                onTaskNotificationListener.apply(task1);
            });

            // listen to the task completion status
            taskCompletedCheckBox.setChecked(task.isCompleted());
            taskCompletedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // update the task state here
                Task task1 = getTask().setIsCompleted(isChecked);
                onTaskCheckedListener.apply(task1);
            });

            editButton.setOnClickListener(v -> {

                taskTextView.setEnabled(!taskTextView.isEnabled());
                taskTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = taskTextView.getText().toString();

                        Task task1 = getTask();
                        if (task1 != null) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(TaskViewHolder.this.itemView.getContext());
                            builder.setMessage(text)
                                    .setTitle("Full View of Task")
                                    .setPositiveButton("OK", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            // apply the update
                            onTaskUpdatedListener.apply(task1, task1.updateTask(text));
                        }

                    }
                });

                Toast.makeText(this.itemView.getContext(), "Edit Enable ", Toast.LENGTH_SHORT).show();
            });

            deleteButton.setOnClickListener(v -> {
                Task task1 = getTask();
                if (task1 != null) {
                    Log.i(TAG, "bind: deleting a task");
                    onTaskDeletedListener.apply(task1);
                }
            });
        }

        Task getTask() {
            // returns a task at a given position e.g when a user checks the checkbox, deletes the item etc
            // the no_position is crutial to prevent unwanted results and is well documented.
            // i.e it should be handled
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return null;
            }

            return getItem(position);
        }
    }
}
