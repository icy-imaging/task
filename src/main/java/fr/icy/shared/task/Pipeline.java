package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Represents a pipeline that consists of a sequence of tasks. Each task has a unique name and
 * identifier and may have dependencies on other tasks within the pipeline. The pipeline
 * provides methods for task management, validation, and execution state monitoring.
 * <p>
 * This class is immutable in terms of the pipeline name, while tasks can be added and their
 * statuses can be updated dynamically.
 */
public final class Pipeline {
    private final @NonNull String name;
    private final @NonNull List<Task> tasks = new ArrayList<>();

    /**
     * Constructs a new {@code Pipeline} instance with the specified name.
     *
     * @param name the name of the pipeline; must not be null
     * @throws NullPointerException if the provided name is null
     */
    public Pipeline(final @NonNull String name) {
        this.name = Objects.requireNonNull(name, "Pipeline name must not be null");
    }

    /**
     * Adds a new task to the pipeline. The task must have a unique name and a unique identifier (UID).
     * If a task with the same name or UID already exists in the pipeline, an exception is thrown.
     *
     * @param task the task to be added to the pipeline; must not be null and must have a unique name and UID
     * @return the updated pipeline instance
     * @throws NullPointerException     if the provided task is null
     * @throws IllegalArgumentException if the task has a duplicate name or UID within the pipeline
     */
    @Contract("_ -> this")
    public Pipeline addTask(final @NonNull Task task) {
        Objects.requireNonNull(task, "SubTask must not be null");

        final boolean duplicateName = tasks.stream().anyMatch(s -> s.getName().equals(task.getName()));
        if (duplicateName)
            throw new IllegalArgumentException("Duplicate subtask name: '" + task.getName() + "'");

        final boolean duplicateUid = tasks.stream().anyMatch(s -> s.getUid() == task.getUid());
        if (duplicateUid)
            throw new IllegalArgumentException("Duplicate task ID: " + task.getUid());

        tasks.add(task);
        return this;
    }

    /**
     * Validates the pipeline configuration.
     * <ul>
     *   <li>Constructs an index of tasks based on their unique IDs.</li>
     *   <li>Ensures that all dependencies referenced by the tasks exist within the index.
     *       If a task references a missing dependency, an {@link IllegalStateException} is thrown.</li>
     *   <li>Checks for cyclic dependencies among the tasks. If any cycles are detected,
     *       an {@link IllegalStateException} is thrown.</li>
     * </ul>
     * <p>
     * This method ensures that the pipeline is in a valid state before execution by
     * verifying task dependencies and detecting any structural issues.
     *
     * @throws IllegalStateException if any task has an unknown dependency or if a cycle is detected.
     */
    public void validate() {
        final Map<Long, Task> index = buildIndex();
        checkUnknownDependencies(index);
        checkForCycles(index);
    }

    /**
     * Constructs an index of tasks based on their unique IDs.
     * The method iterates through the list of tasks and populates a map,
     * where each key is the unique identifier (UID) of a task and the corresponding value
     * is the {@link Task} object associated with that UID. The order of tasks in the index
     * respects the order of tasks in the input list.
     *
     * @return A non-null {@link Map} with task IDs as keys and {@link Task} objects as values,
     * preserving the insertion order of the tasks.
     */
    private @NonNull Map<Long, Task> buildIndex() {
        final Map<Long, Task> index = new LinkedHashMap<>();
        tasks.forEach(st -> index.put(st.getUid(), st));
        return index;
    }

    /**
     * Validates that all dependencies referenced by the tasks in the current pipeline exist
     * within the provided index. If a task references a dependency that is not present in
     * the index, an {@link IllegalStateException} is thrown.
     *
     * @param index A map where keys are unique IDs of tasks and values are corresponding
     *              {@link Task} objects. This map represents the available tasks
     *              and their associated metadata in the pipeline.
     */
    private void checkUnknownDependencies(final @NonNull Map<Long, Task> index) {
        for (final Task st : tasks) {
            for (final Long dep : st.getDependencies()) {
                if (!index.containsKey(dep))
                    throw new IllegalStateException(String.format("Task '%s' references unknown dependency '%s'", st.getName(), dep));
            }
        }
    }

    /**
     * Checks for cycles in a directed graph of tasks represented by the given index.
     * Throws an {@link IllegalStateException} if any circular dependency is detected.
     *
     * @param index A map where keys are task IDs and values are corresponding {@link Task} objects.
     *              This represents the directed graph of tasks and their dependencies.
     *              Each task may have dependencies on other tasks within the graph.
     */
    private void checkForCycles(final @NonNull Map<Long, Task> index) {
        final Set<Long> visited = new HashSet<>();
        final Set<Long> inStack = new HashSet<>();
        for (final long uid : index.keySet()) {
            if (dfs(uid, index, visited, inStack))
                throw new IllegalStateException("Circular dependency detected in pipeline '" + this.name + "'");
        }
    }

    /**
     * Performs a depth-first search to detect cycles in a directed graph of subtasks.
     *
     * @param node    The name of the current subtask node being visited.
     * @param index   A map representing the graph, where keys are subtask names and
     *                values are corresponding SubTask objects.
     * @param visited A set of nodes that have already been fully visited.
     * @param inStack A set of nodes currently in the recursion stack, used to detect cycles.
     * @return {@code true} if a cycle is detected during the traversal, {@code false} otherwise.
     */
    private boolean dfs(final Long node, final Map<Long, Task> index, final Set<Long> visited, final @NonNull Set<Long> inStack) {
        if (inStack.contains(node)) return true;
        if (visited.contains(node)) return false;
        visited.add(node);
        inStack.add(node);
        for (final Long dep : index.get(node).getDependencies())
            if (dfs(dep, index, visited, inStack)) return true;
        inStack.remove(node);
        return false;
    }

    /**
     * Retrieves the name of the pipeline.
     *
     * @return a non-null string representing the name of the pipeline.
     */
    @Contract(pure = true)
    public @NonNull String getName() {
        return name;
    }

    /**
     * Retrieves an unmodifiable list of tasks associated with the pipeline.
     *
     * @return a non-null, unmodifiable list of {@link Task} objects representing the tasks in the pipeline.
     */
    @Contract(pure = true)
    @Unmodifiable
    public @NonNull List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Calculates the overall progress of the pipeline by averaging the progress
     * percentages of all tasks within the pipeline. If no tasks are present,
     * the method returns 100%, indicating the pipeline is fully complete.
     *
     * @return an integer representing the total progress of the pipeline.
     *         The value ranges from 0 to 100, where 0 indicates no progress
     *         has been made and 100 indicates all tasks are complete.
     */
    public int getTotalProgress() {
        if (tasks.isEmpty())
            return 100;

        int total = 0;
        for (final Task t : tasks) {
            total += t.getProgress();
        }
        return Math.min(100, total / tasks.size());
    }

    /**
     * Checks whether all tasks in the pipeline have been completed.
     *
     * @return {@code true} if all tasks in the pipeline have a status of {@code TaskStatus.COMPLETED};
     *         {@code false} otherwise.
     */
    public boolean isCompleted() {
        for (final Task task : tasks) {
            if (task.getStatus() != TaskStatus.COMPLETED)
                return false;
        }
        return true;
    }

    /**
     * Checks whether any task in the pipeline has a status of {@code TaskStatus.FAILED}.
     *
     * @return {@code true} if at least one task in the pipeline has a status of {@code TaskStatus.FAILED};
     *         {@code false} otherwise.
     */
    public boolean isFailed() {
        for (final Task task : tasks) {
            if (task.getStatus() == TaskStatus.FAILED)
                return true;
        }
        return false;
    }

    /**
     * Checks if any task within the pipeline is currently in a running state.
     *
     * @return {@code true} if at least one task in the pipeline has a status of {@code TaskStatus.RUNNING};
     *         {@code false} otherwise.
     */
    public boolean isRunning() {
        for (final Task task : tasks) {
            if (task.getStatus() == TaskStatus.RUNNING)
                return true;
        }
        return false;
    }

    /**
     * Checks whether any task in the pipeline has been cancelled.
     *
     * @return {@code true} if at least one task in the pipeline has a status of {@code TaskStatus.CANCELLED};
     *         {@code false} otherwise.
     */
    public boolean isCancelled() {
        for (final Task task : tasks) {
            if (task.getStatus() == TaskStatus.CANCELLED)
                return true;
        }
        return false;
    }

    /**
     * Determines if all tasks in the pipeline have reached a terminal state.
     * A task is considered to be in a terminal state if its status is classified as terminal.
     *
     * @return {@code true} if all tasks in the pipeline have a terminal status;
     *         {@code false} otherwise.
     */
    public boolean isTerminated() {
        for (final Task task : tasks) {
            if (!task.getStatus().isTerminal())
                return false;
        }
        return true;
    }
}
