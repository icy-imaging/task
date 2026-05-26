package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown to indicate that the execution of a pipeline task has failed.
 * This exception encapsulates the pipeline that encountered the failure,
 * the specific tasks that failed, and an optional underlying cause.
 * <p>
 * Instances of this class are immutable and thread-safe.
 */
public final class TaskExecutionException extends Exception {
    /**
     * Represents the pipeline that encountered a failure during task execution.
     * This pipeline serves as the context for the exception, identifying the
     * sequence of operations that resulted in the error.
     * <p>
     * The associated pipeline cannot be null and provides details necessary
     * for diagnosing the failure, such as its name and configured tasks.
     */
    private final @NonNull Pipeline pipeline;

    /**
     * A list of tasks that failed during the execution of a pipeline.
     * <p>
     * This field represents the specific tasks that encountered errors or
     * were unable to complete successfully within the context of the pipeline
     * execution. It cannot be null and is immutable, ensuring thread safety
     * and consistent access across multiple threads.
     * <p>
     * The list encapsulates meaningful details about the failures, such as the
     * task names, descriptions, or any relevant metadata provided by the
     * {@link Task} objects.
     * <p>
     * As this list is unmodifiable, attempts to modify it will result in
     * {@code UnsupportedOperationException}. This guarantees that the state
     * of failed tasks remains consistent once a {@code TaskExecutionException}
     * is created.
     */
    @Unmodifiable
    private final @NonNull List<Task> failedTasks;

    /**
     * Constructs a new {@code TaskExecutionException} with the specified pipeline,
     * list of failed tasks, and the underlying cause of the failure.
     *
     * @param pipeline    the pipeline that encountered the failure; must not be null
     * @param failedTasks the list of tasks that failed during the pipeline execution; must not be null
     * @param cause       the underlying cause of the failure; can be null
     */
    public TaskExecutionException(final @NonNull Pipeline pipeline, final @NonNull List<Task> failedTasks, final Throwable cause) {
        super("Pipeline '" + pipeline.getName() + "' failed – failed tasks: " + failedTasks.stream().map(Task::getName).collect(Collectors.joining(", ", "[", "]")), cause);
        this.pipeline = pipeline;
        this.failedTasks = List.copyOf(failedTasks);
    }

    /**
     * Retrieves the pipeline associated with this exception.
     *
     * @return the pipeline that encountered the failure; never null
     */
    @Contract(pure = true)
    public @NonNull Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Retrieves an unmodifiable list of tasks that failed during the execution of the pipeline.
     *
     * @return a non-null, unmodifiable list containing the tasks that encountered failures
     */
    @Contract(pure = true)
    @Unmodifiable
    public @NonNull List<Task> getFailedTasks() {
        return failedTasks;
    }
}
