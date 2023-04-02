package com.example.taskman;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
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
    @NotNull
    private final Function<Task, Void> onTaskPriorityUpdateListener;


    public TasksAdapter(@NotNull Function<Task, Void> onTaskCheckedListener,
                        @NotNull Function<Task, Void> onTaskStartDateSetListener,
                        @NotNull Function<Task, Void> onTaskDeletedListener,
                        @NotNull BiFunction<Task, Task, Void> onTaskUpdatedListener,
                        @NotNull Function<Task, Void> onTaskNotificationListener,
                        @NotNull Function<Task, Void> onTaskPriorityUpdateListener) {
        super(DIFF_UTIL);
        this.onTaskCheckedListener = onTaskCheckedListener;
        this.onTaskStartDateSetListener = onTaskStartDateSetListener;
        this.onTaskDeletedListener = onTaskDeletedListener;
        this.onTaskUpdatedListener = onTaskUpdatedListener;
        this.onTaskNotificationListener = onTaskNotificationListener;
        this.onTaskPriorityUpdateListener = onTaskPriorityUpdateListener;
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
        private final MaterialAutoCompleteTextView prioritySelector;

        private final TextWatcher prioritySelectionWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: starting");

            }
        };

        public TaskViewHolder(View view) {
            super(view);
            taskTextView = itemView.findViewById(R.id.task_text_view);
            setStartDate = itemView.findViewById(R.id.button);
            reminderButton = itemView.findViewById(R.id.reminder_button);
            taskCompletedCheckBox = itemView.findViewById(R.id.task_checkbox);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            prioritySelector = itemView.findViewById(R.id.set_priority);
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
                taskTextView.setOnClickListener(v1 -> {
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

                });

                Toast.makeText(this.itemView.getContext(), "Edit Enable ", Toast.LENGTH_SHORT).show();
            });
            deleteButton.setOnClickListener(v -> {
                Task task1 = getTask();
                if (task1 != null) {
                    Log.i(TAG, "bind: deleting a task");
                    onTaskDeletedListener.apply(task1);
                }
                Toast.makeText(this.itemView.getContext(), "Task Deleted ", Toast.LENGTH_SHORT).show();
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


            // setting up the initial task priority
            Context context = itemView.getContext();
            TaskPriority priority = task.getPriority();
            if (priority != TaskPriority.NONE) {
                String currentPriority = priority.text(context);
                String[] priorities = context.getResources().getStringArray(R.array.priorities);

                // finding the priority that matches the set one to ensure confluence of the value set
                for (int i = priorities.length - 1; i >= 0; i--) {
                    String priority1 = priorities[i];
                    if (Objects.equals(currentPriority, priority1)) {
                        // first remove this watcher then make the update
                        // this prevents an infinite flow of updates
//                        prioritySelector.removeTextChangedListener(prioritySelectionWatcher);
                        Log.i(TAG, "bind: text changing");
                        prioritySelector.setText(priority1);
                        // after setting the text, you need to refresh the selection list otherwise
                        // some misbehaviour will occur.
                        // comment the following line to see the misbehaviour
                        prioritySelector.setSimpleItems(R.array.priorities);
                        break;
                    }
                }
            }
            // Listen to selection changes on the priority selector
//            prioritySelector.addTextChangedListener(prioritySelectionWatcher);
            prioritySelector.setOnItemClickListener((parent, view, position, id1) -> {
                String st = (String) parent.getItemAtPosition(position);
                Context context1 = itemView.getContext();
                if (st == null) {
                    // do nothing if the text is null
                    return;
                }

                st = st.trim();
                if (st.isEmpty()) {
                    // also do nothing if the text is empty, recall to trim to remove whitespaces in the text
                    return;
                }

                final String low = context1.getString(R.string.low);
                final String medium = context1.getString(R.string.medium);
                TaskPriority priority12;
                if (st.equals(low)) {
                    priority12 = TaskPriority.LOW;
                } else if (st.equals(medium)) {
                    priority12 = TaskPriority.MEDIUM;
                } else {
                    priority12 = TaskPriority.IMPORTANT;
                }
                Task task1 = getTask();
                if (task1 == null) {
                    return;
                }

                // update the priority selection to the respective task record

                Task task2 = task1.setPriority(priority12);
                onTaskPriorityUpdateListener.apply(task2);
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

