package fr.icy.shared.task.example;

import fr.icy.shared.task.Task;
import org.jspecify.annotations.NonNull;

/**
 * Represents a task that executes for a fixed amount of time with periodic progress reporting.
 * This task divides the total execution duration into a predefined number of steps, during which
 * it reports incremental progress until completion.
 * <p>
 * This class is a concrete implementation of the {@link Task} abstract class, designed for scenarios
 * where the duration of the task is fixed and progress can be tracked by time.
 */
public final class FixedTimeTask extends Task {
    private final int totalMs;
    private static final int STEPS = 20;

    /**
     * Constructs a FixedTimeTask with a specified total execution time and optional dependencies.
     * The `totalMs` parameter defines the total duration of the task, while the `dependencies`
     * parameter represents the identifiers of tasks that this task depends on.
     *
     * @param totalMs      the total duration of the task in milliseconds
     * @param dependencies an optional array of dependency identifiers; must not be null,
     *                     but individual elements can be empty if no dependencies are provided
     */
    public FixedTimeTask(final int totalMs, @NonNull final Long... dependencies) {
        super(String.valueOf(System.nanoTime()), dependencies);
        this.totalMs = totalMs;
    }

    /**
     * Executes the task by dividing the total duration into a predefined number of steps
     * and reporting progress at each step. Each step involves a delay proportional to
     * the total execution time, after which progress is reported with the corresponding step number.
     *
     * @throws Exception if the thread sleep is interrupted during execution
     */
    @Override
    public void execute() throws Exception {
        final int stepMs = totalMs / STEPS;
        for (int step = 1; step <= STEPS; step++) {
            Thread.sleep(stepMs);
            reportProgress(step * (100 / STEPS), String.format("step %d/%d", step, STEPS));
        }
    }
}
