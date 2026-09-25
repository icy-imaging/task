/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents an event indicating the progress and state of a specific task.
 * Instances of this class are used to convey information about a task's current
 * progress, status, and any relevant messages or errors associated with the task.
 * This class is immutable and thread-safe.
 */
public final class ProgressEvent {
    private final @NonNull String taskName;
    private final int progress;      // 0–100
    private final @NonNull TaskStatus status;
    private final @NonNull String message;
    private final @Nullable Throwable error;

    /**
     * Constructs a new {@code ProgressEvent} instance representing the progress and state
     * of a specific task.
     *
     * @param taskName the name of the task associated with this event; must not be null
     * @param progress the task's progress as a percentage, ranging from 0 to 100
     * @param status the current status of the task; must not be null
     * @param message an optional message providing additional details about the task; can be null
     * @param error an optional exception representing any errors encountered during the task's execution; can be null
     */
    @Contract(pure = true)
    ProgressEvent(final @NonNull String taskName, final int progress, final @NonNull TaskStatus status, final @Nullable String message, final @Nullable Throwable error) {
        this.taskName = taskName;
        this.progress = progress;
        this.status = status;
        this.message = message != null ? message : "";
        this.error = error;
    }

    /**
     * Retrieves the name of the task associated with this progress event.
     *
     * @return the name of the task; never null
     */
    @Contract(pure = true)
    public @NonNull String getTaskName() {
        return taskName;
    }

    /**
     * Retrieves the progress of the associated task as a percentage.
     * The value is guaranteed to be within the range of 0 to 100 (inclusive),
     * where 0 represents no progress and 100 represents completion.
     *
     * @return the task's progress as a percentage
     */
    @Contract(pure = true)
    public int getProgress() {
        return progress;
    }

    /**
     * Retrieves the current status of the task associated with this progress event.
     * The status represents the stage or condition of the task, such as whether it is
     * pending, in progress, completed, failed, or cancelled.
     *
     * @return the current task status; never null
     */
    @Contract(pure = true)
    public @NonNull TaskStatus getStatus() {
        return status;
    }

    /**
     * Retrieves the message associated with this progress event.
     * The message typically provides additional context or details about the task's
     * state or progress. It could describe the current step in the process, provide
     * user-facing information, or elaborate on any conditions or updates related to
     * the task.
     *
     * @return the message associated with this progress event; never null
     */
    @Contract(pure = true)
    public @NonNull String getMessage() {
        return message;
    }

    /**
     * Retrieves the error associated with this progress event, if any.
     * The error represents an exception encountered during the execution
     * of the task tied to this progress event. If no error occurred, this method
     * returns {@code null}.
     *
     * @return an instance of {@link Throwable} representing the error, or {@code null} if no error is present
     */
    @Contract(pure = true)
    public @Nullable Throwable getError() {
        return error;
    }
}
