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


    @NotNull
    private final Function<Task, Void> onTaskCheckedListener;

    @NotNull
    private final Function<Task, Void> onTaskStartDateSetListener;

    @NotNull
    private final Function<Task, Void> onTaskDeletedListener;

    @NotNull
    private final BiFunction<Task, Task, Void> onTaskUpdatedListener;

    @NotNull
    private final Function<Task, Void> onTaskNotificationListener;


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


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {


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



            taskTextView.setText(task.getTask());


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

                dateText = itemView.getContext().getString(R.string.start_date);
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


            taskCompletedCheckBox.setChecked(task.isCompleted());
            taskCompletedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

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


                    Calendar c1 = Calendar.getInstance();
                    c1.set(Calendar.YEAR, year1);
                    c1.set(Calendar.MONTH, month1);
                    c1.set(Calendar.DAY_OF_MONTH, dayOfMonth1);


                    Task item = getItem(p).setStartDate(c1.getTime());
                    onTaskStartDateSetListener.apply(item);
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            });
        }

        Task getTask() {

            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return null;
            }

            return getItem(position);
        }
    }
}
