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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract unit of work that can be executed. Each task has a unique
 * identifier, a name, optional dependencies, progress tracking, and lifecycle management.
 * This class provides a framework for task execution with customizable behavior
 * through subclassing.
 * <p>
 * The task has the following key features:
 * <ul>
 *     <li>Unique identification of tasks.</li>
 *     <li>Dependency specification for execution ordering.</li>
 *     <li>Trackable progress reporting with optional messages.</li>
 *     <li>Status management that reflects the task lifecycle (e.g., pending, complete, failed).</li>
 *     <li>Mechanisms for handling errors and notifying listeners of progress and status changes.</li>
 * </ul>
 */
public abstract class Task {
    private final Long uid;
    private final @NonNull String name;
    private final Set<Long> dependencies;

    private volatile int progress = 0;
    private volatile @NonNull TaskStatus status = TaskStatus.PENDING;
    private volatile @NonNull String message = "";
    private volatile @Nullable Throwable error = null;

    private final @NonNull List<ProgressListener> listeners = new CopyOnWriteArrayList<>();

    private final Logger logger;

    /**
     * Constructs a new Task with a specified unique identifier (UID), name, and optional dependencies.
     *
     * @param uid          the unique identifier of the task
     * @param name         the name of the task; must not be null
     * @param dependencies an optional array of dependency identifiers; must not be null,
     *                     but individual elements can be empty if no dependencies are provided
     */
    protected Task(final long uid, final @NonNull String name, final @NonNull Long... dependencies) {
        this.uid = uid;
        this.name = Objects.requireNonNull(name, "Task name must not be null");
        this.dependencies = Set.of(dependencies);
        this.logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Constructs a new Task with a dynamically generated unique identifier, a specified name,
     * and optional dependencies. This constructor delegates to another constructor
     * that initializes the task with a provided UID, name, and dependencies.
     *
     * @param name         the name of the task, must not be null
     * @param dependencies an optional array of dependency identifiers; must not be null,
     *                     but individual elements can be empty if no dependencies are provided
     */
    protected Task(final @NonNull String name, final @NonNull Long... dependencies) {
        this(System.nanoTime(), name, dependencies);
    }

    /**
     * Executes the task's primary logic. This method is the entry point for task execution
     * and must be implemented by subclasses to define the specific behavior of the task.
     * <p>
     * Subclasses implementing this method should handle task-specific execution details
     * and may use other methods, such as progress and status reporting, to interact
     * with the task's lifecycle.
     *
     * @throws Exception if an error occurs during the execution of the task. Subclasses
     *                   should throw an appropriate exception to indicate specific failure scenarios.
     */
    public abstract void execute() throws Exception;

    /**
     * Reports the current progress of the task using the provided percentage value.
     * This method delegates the progress reporting logic to another method, which can
     * include additional details such as a descriptive message.
     *
     * @param percent the progress percentage of the task, clamped to the range 0–100
     */
    protected final void reportProgress(final int percent) {
        reportProgress(percent, this.message);
    }

    /**
     * Reports the progress of the task and triggers a progress event to notify listeners.
     * The progress value is clamped between 0 and 100. The message is optional and can provide additional context.
     *
     * @param percent the progress percentage of the task, clamped to the range 0–100
     * @param message an optional descriptive message associated with the progress; may be null
     */
    protected final void reportProgress(final int percent, final @Nullable String message) {
        this.progress = Math.min(100, Math.max(0, percent));
        this.message = message != null ? message : "";
        //final Logger logger = Logger.getLogger(getClass().getName());
        if (logger.isLoggable(Level.FINE)) {
            if (this.message.isEmpty())
                logger.fine("Task progress: [" + name + "] (" + progress + "%).");
            else
                logger.fine("Task progress: [" + name + "] " + this.message + " (" + progress + "%).");
        }
        fireEvent();
    }

    /**
     * Updates the current status of the task and triggers the appropriate event to notify listeners.
     *
     * @param s the new status of the task; must not be null
     */
    void setStatus(final @NonNull TaskStatus s) {
        this.status = s;
        fireEvent();
    }

    /**
     * Marks the task as failed and updates its status to {@link TaskStatus#FAILED}.
     * Optionally, an associated {@link Throwable} can be provided to capture the
     * cause of the failure.
     *
     * @param cause an optional {@link Throwable} representing the reason for the task's failure;
     *              may be null if no specific cause is available
     */
    void markFailed(final @Nullable Throwable cause) {
        this.error = cause;
        setStatus(TaskStatus.FAILED);
    }

    /**
     * Marks the task as completed by updating its status to {@link TaskStatus#COMPLETED}
     * and ensuring the progress is set to 100%.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>If the current status of the task is not {@link TaskStatus#COMPLETED},
     *       it updates the status to {@link TaskStatus#COMPLETED}.</li>
     *   <li>If the task's progress is less than 100%, it reports the progress as 100%.</li>
     * </ul>
     * <p>
     * The method leverages the {@link #setStatus(TaskStatus)} and
     * {@link #reportProgress(int)} to handle these updates and to notify
     * relevant listeners or observers of the changes.
     */
    void markCompleted() {
        if (this.status != TaskStatus.COMPLETED)
            setStatus(TaskStatus.COMPLETED);

        if (this.progress < 100)
            reportProgress(100);

        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "Task completed: [" + name + "].");
    }

    /**
     * Adds a progress listener to the task. The listener will be notified of progress updates
     * during the execution of the task. This allows external components to monitor task
     * progress and respond to progress events as they occur.
     *
     * @param l the progress listener to be added; must not be null
     */
    public final void addProgressListener(final @NonNull ProgressListener l) {
        listeners.add(Objects.requireNonNull(l));
    }

    /**
     * Notifies all registered progress listeners of a progress event.
     * <p>
     * This method creates a {@link ProgressEvent} that encapsulates the current state of the task,
     * including its name, progress, status, message, and any associated error. The event is then
     * dispatched to each listener in the list of {@code listeners}.
     * <p>
     * If a listener throws an exception during the handling of the event, the exception is caught
     * and ignored to ensure that the notification process continues for all remaining listeners.
     * This design ensures that task execution is not disrupted by listener errors.
     */
    private void fireEvent() {
        final ProgressEvent evt = new ProgressEvent(name, progress, status, message, error);
        for (final ProgressListener l : listeners) {
            try {
                l.onProgress(evt);
            }
            catch (final Exception ignored) {
                // never crash a worker
            }
        }
    }

    /**
     * Retrieves the unique identifier (UID) of the task.
     *
     * @return the unique identifier of the task as a long value
     */
    public long getUid() {
        return uid;
    }

    /**
     * Retrieves the name of the task.
     *
     * @return the name of the task; guaranteed to be non-null
     */
    public @NonNull String getName() {
        return name;
    }

    /**
     * Retrieves the set of dependencies associated with the task.
     * Dependencies are represented as unique identifiers (UIDs) of other tasks
     * that must be completed or satisfied before this task can proceed.
     *
     * @return a non-null set of dependency UIDs associated with the task
     */
    public @NonNull Set<Long> getDependencies() {
        return dependencies;
    }

    /**
     * Retrieves the current progress of the task. The progress is represented
     * as an integer value that typically falls within the range of 0 to 100,
     * denoting the percentage of the task's completion.
     *
     * @return the current progress of the task as an integer
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Retrieves the current status of the task.
     *
     * @return the current status of the task as a non-null {@link TaskStatus} value
     */
    public @NonNull TaskStatus getStatus() {
        return status;
    }

    /**
     * Retrieves the message associated with the task, which provides additional
     * context or descriptive information about the task's current state.
     *
     * @return the message of the task; guaranteed to be non-null
     */
    public @NonNull String getMessage() {
        return message;
    }

    /**
     * Retrieves the error associated with the task, if one exists. This method
     * returns an {@code Optional} containing the associated {@code Throwable}
     * that represents the cause of the task's failure, or an empty {@code Optional}
     * if no error is present.
     *
     * @return an {@code Optional} containing the error of the task, or an empty {@code Optional} if no error exists
     */
    public @NonNull Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }
}
