package com.example.taskman;

import android.content.Context;

import androidx.annotation.StringRes;

public enum TaskPriority {
    NONE(0),

    LOW(R.string.low),

    MEDIUM(R.string.medium),

    IMPORTANT(R.string.important);

    private final int nameRes;

    TaskPriority(@StringRes int nameRes) {
        this.nameRes = nameRes;
    }

    public String text(Context context) {
        return context.getString(nameRes);
    }
}
