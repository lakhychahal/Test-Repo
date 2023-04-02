package com.example.taskman;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Objects;

public class Task implements Comparable<Task> {

    private final String task;
    private final boolean completed;
    private final Date startDate;
    private final TaskPriority priority;

    public Task(String task, boolean completed, Date startDate, TaskPriority priority) {
        this.task = task;
        this.completed = completed;
        this.startDate = startDate;
        this.priority = priority;
    }

    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Task updateTask(String task) {
        return new Task(task, completed, startDate, priority);
    }

    public Task setStartDate(Date startDate) {
        return new Task(task, completed, startDate, priority);
    }

    public Task setIsCompleted(boolean isCompleted) {
        return new Task(task, isCompleted, startDate, priority);
    }
    public Task setPriority(TaskPriority priority) {
        return new Task(task, completed, startDate, priority);
    }

    public TaskPriority getPriority() {
        return priority;
    }

    @Nullable
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int compareTo(@NonNull Task o) {


        int t = this.task.compareTo(o.task);
        if (t != 0) return t;
        int c = Boolean.compare(completed, o.completed);
        if (c != 0) return c;
        Date startDate1 = getStartDate();
        Date startDate2 = o.getStartDate();
        if (startDate1 == null) {
            return -1;
        }
        if (startDate2 == null) {
            return -1;
        }
        return startDate1.compareTo(startDate2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task1 = (Task) o;
        boolean b = completed == task1.completed && task.equals(task1.task);

        if (startDate == null ^ task1.startDate == null) return false;


        if (startDate == null) return b;
        return b && Objects.equals(startDate, task1.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, completed, startDate);
    }

    @NonNull
    @Override
    public String toString() {
        return "Task{" +
                "task='" + task + '\'' +
                ", completed=" + completed +
                ", startDate=" + startDate +
                '}';
    }
}
