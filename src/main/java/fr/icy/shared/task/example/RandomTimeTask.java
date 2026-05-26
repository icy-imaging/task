package fr.icy.shared.task.example;

import fr.icy.shared.task.Task;
import org.jspecify.annotations.NonNull;

import java.util.Random;

/**
 * A specialized implementation of the {@code Task} class that performs work for a random duration
 * of time, reporting progress in discrete steps. This class represents an asynchronous task
 * that sleeps for a proportional amount of time at each step, then notifies progress listeners
 * with details about the progress state.
 * <p>
 * The task is characterized by:
 * <ul>
 *     <li>A randomly generated total duration between 3 and 5 seconds, divided into a fixed number of steps.</li>
 *     <li>Periodic progress reporting with step-by-step updates.</li>
 *     <li>Completion when all steps are executed.</li>
 * </ul>
 */
public final class RandomTimeTask extends Task {
    private final int totalMs;
    private static final int STEPS = 20;

    /**
     * Constructs a RandomTimeTask with an optional array of dependency identifiers.
     * This task is designed to execute for a random total duration, determined at
     * initialization, and divided into discrete steps with periodic progress reporting.
     * The `dependencies` parameter defines tasks that this task depends on.
     *
     * @param dependencies an optional array of dependency identifiers; must not be null,
     *                     but individual elements can be empty if no dependencies are provided
     */
    public RandomTimeTask(@NonNull final Long... dependencies) {
        super(String.valueOf(System.nanoTime()), dependencies);
        totalMs = new Random().nextInt(30, 51) * 100;
    }

    /**
     * Executes the task by dividing a randomly determined total duration into a fixed number of steps
     * and reporting progress at the end of each step. During each step, the method introduces a delay
     * proportional to the total duration, then notifies listeners of the current progress percentage
     * and step details.
     * <p>
     * Each step's duration is decided by dividing the total execution time evenly among all steps.
     * Progress is calculated as a percentage based on completed steps relative to the total number
     * of steps. A descriptive message is included with each progress update to provide step information.
     *
     * @throws Exception if the thread sleep operation is interrupted during execution
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
