package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;

/**
 * Represents the various statuses a task can hold during its lifecycle.
 * The task statuses are used to track and manage the state of a task.
 * <p>
 * The available statuses include:
 * <ul>
 *     <li>PENDING: The task has been created but not yet started.</li>
 *     <li>RUNNING: The task is currently in progress.</li>
 *     <li>COMPLETED: The task has finished successfully.</li>
 *     <li>FAILED: The task has finished with an error or failed to complete successfully.</li>
 *     <li>CANCELLED: The task was intentionally halted before completion.</li>
 * </ul>
 */
public enum TaskStatus {
    /**
     * Indicates that the task has been created but has not yet started.
     * This status represents the initial state of a task before any processing begins.
     */
    PENDING("Pending"),
    /**
     * Represents a task currently in progress.
     * This status indicates that the task has been started and is actively being executed.
     */
    RUNNING("Running"),
    /**
     * Indicates that the task has finished successfully.
     * This status signifies the successful completion of all processes or operations
     * associated with the task, with no errors encountered.
     */
    COMPLETED("Completed"),
    /**
     * Indicates that the task has finished with an error or failed to complete successfully.
     * This status represents the unsuccessful termination of a task due to an issue
     * during its execution or processing.
     */
    FAILED("Failed"),
    /**
     * Indicates that the task was intentionally halted before completion.
     * This status represents a deliberate cancellation of the task, which
     * may occur due to user intervention, system constraints, or other
     * external factors. Unlike FAILED, the task did not encounter an error
     * during its execution but was purposefully stopped instead.
     */
    CANCELLED("Cancelled");

    private final String label;

    /**
     * Constructs a TaskStatus instance with the specified label.
     * The label provides a human-readable representation of the task status.
     *
     * @param label the string representation of the task status
     */
    @Contract(pure = true)
    TaskStatus(final String label) {
        this.label = label;
    }

    /**
     * Retrieves the label associated with the task status.
     * The label provides a human-readable description of the status.
     *
     * @return the label of the task status.
     */
    @Contract(pure = true)
    public String getLabel() {
        return label;
    }

    /**
     * Checks if the current task status is considered terminal.
     * A terminal status indicates that the task is no longer active and does
     * not require further processing. Specifically, a status is terminal if it
     * is one of the following: COMPLETED, FAILED, or CANCELLED.
     *
     * @return true if the task status is COMPLETED, FAILED, or CANCELLED; false otherwise.
     */
    @Contract(pure = true)
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
