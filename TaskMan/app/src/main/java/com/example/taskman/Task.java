package com.example.taskman;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Objects;

public class Task implements Comparable<Task> {
    //created to hold the task data
    private final String task;
    private final boolean completed;
    private final Date startDate;

    public Task(String task, boolean completed, Date startDate) {
        this.task = task;
        this.completed = completed;
        this.startDate = startDate;
    }

    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Task updateTask(String task) {
        return new Task(task, completed, startDate);
    }

    public Task setStartDate(Date startDate) {
        return new Task(task, completed, startDate);
    }

    public Task setIsCompleted(boolean isCompleted) {
        return new Task(task, isCompleted, startDate);
    }

    @Nullable
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int compareTo(@NonNull Task o) {

        // this is used in sorting the tasks according to the name, completion status and lastly, the start date set
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
        //when either dates is exclusively but not both null return false
        if (startDate == null ^ task1.startDate == null) return false;

        // at this point, it's either the dates are null or neither is null
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
