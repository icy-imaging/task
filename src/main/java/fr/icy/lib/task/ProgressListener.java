package fr.icy.lib.task;

/**
 * Functional interface for monitoring the progress of a task.
 * Implementations of this interface can be used to receive updates on
 * task progress, status, and optional details such as messages or errors.
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * Handles the progress of a task, providing updates about the task's current state, progress percentage,
     * and optional status details or errors.
     *
     * @param event an instance of {@code ProgressEvent} containing information about the task's progress,
     *              including the task name, progress percentage (0–100), task status, an optional message,
     *              and an optional error if applicable.
     */
    void onProgress(ProgressEvent event);
}
