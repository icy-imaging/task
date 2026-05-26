package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.*;

/**
 * A singleton class responsible for executing pipelines in a concurrent and parallelized manner.
 * The {@code TaskExecutor} manages thread pools for pipelines, executes tasks with dependency handling,
 * and notifies registered listeners of pipeline events such as additions and removals.
 * It ensures efficient resource utilization and provides robust error handling during pipeline execution.
 */
public final class TaskExecutor {
    private static volatile TaskExecutor instance;

    /**
     * Provides a thread-safe singleton instance of the {@code TaskExecutor} class.
     * If the instance does not exist, it initializes a new {@code TaskExecutor} object.
     * Ensures that only one instance of {@code TaskExecutor} is created throughout the application lifecycle.
     *
     * @return a non-null singleton instance of {@code TaskExecutor}.
     */
    public static synchronized @NonNull TaskExecutor getInstance() {
        final TaskExecutor i = instance;
        if (i != null)
            return i;
        synchronized (TaskExecutor.class) {
            if (instance == null)
                instance = new TaskExecutor();
            return instance;
        }
    }

    private final int nProc;

    private final @NonNull Map<Pipeline, ExecutorService> pipelinePools = new ConcurrentHashMap<>();
    private final @NonNull List<PipelineListener> pipelineListeners = new CopyOnWriteArrayList<>();

    /**
     * Initializes an instance of the {@code TaskExecutor} class.
     * <p>
     * This constructor determines the number of available processors in the
     * current runtime environment. It ensures that there is at least one
     * processor available for task execution. If the determined processor count
     * is less than 1, an {@link IllegalArgumentException} is thrown to indicate
     * an invalid state.
     * <p>
     * This method is private to enforce the Singleton design pattern, preventing
     * external instantiation of the {@code TaskExecutor} class.
     *
     * @throws IllegalArgumentException if the detected number of processors is less than 1
     */
    private TaskExecutor() {
        nProc = Runtime.getRuntime().availableProcessors();
        if (nProc < 1) throw new IllegalArgumentException("Thread count must be >= 1");
    }

    /**
     * Registers a {@link PipelineListener} to receive notifications about pipeline lifecycle events.
     * The provided listener will be notified when pipelines are added or removed.
     *
     * @param listener the {@code PipelineListener} to be added; must not be null
     *                 and must implement the {@link PipelineListener} interface
     * @throws NullPointerException if the provided listener is null
     */
    public void addPipelineListener(final @NonNull PipelineListener listener) {
        pipelineListeners.add(Objects.requireNonNull(listener, "Pipeline listener cannot be null"));
    }

    /**
     * Removes a registered {@link PipelineListener} from the collection of listeners
     * receiving notifications about pipeline lifecycle events.
     *
     * @param listener the {@code PipelineListener} to be removed; must not be null
     * @throws NullPointerException if the provided listener is null
     */
    public void removePipelineListener(final @NonNull PipelineListener listener) {
        pipelineListeners.remove(Objects.requireNonNull(listener, "Pipeline listener cannot be null"));
    }

    /**
     * Executes the specified pipeline by validating it, initializing necessary resources,
     * and processing its tasks in a parallelized manner using a thread pool. Blocks until
     * all tasks in the pipeline are completed or if an exception occurs during execution.
     * Once execution is complete or an error occurs, the pipeline is cleaned up and removed.
     *
     * @param pipeline the pipeline to be executed; must not be null
     * @throws TaskExecutionException if one or more tasks in the pipeline fail to execute
     */
    public void execute(final @NonNull Pipeline pipeline) throws TaskExecutionException {
        Objects.requireNonNull(pipeline, "Cannot execute if pipeline is null");
        pipeline.validate();
        pipelinePools.put(pipeline, Executors.newFixedThreadPool(nProc, r -> {
            final Thread t = new Thread(r, "pipeline-executor");
            t.setDaemon(true);
            return t;
        }));

        final PipelineEvent pe = new PipelineEvent(pipeline);

        for (final PipelineListener listener : pipelineListeners)
            listener.onAdded(pe);

        // Build index & attach global listeners
        final Map<Long, Task> index = new LinkedHashMap<>();
        for (final Task task : pipeline.getTasks()) {
            index.put(task.getUid(), task);
        }

        // Build a CompletableFuture for each subtask (recursive, memoized)
        final Map<String, CompletableFuture<Void>> futures = new LinkedHashMap<>();
        for (final Task task : pipeline.getTasks())
            buildFuture(pipeline, task, index, futures);

        // Block until every future settles
        try {
            CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();
        }
        catch (final CompletionException ex) {
            final List<Task> failed = new ArrayList<>();
            for (final Task task : pipeline.getTasks())
                if (task.getStatus() == TaskStatus.FAILED) failed.add(task);

            removePipeline(pipeline, pe);

            throw new TaskExecutionException(pipeline, failed, ex.getCause() != null ? ex.getCause() : ex);
        }

        removePipeline(pipeline, pe);
    }

    /**
     * Removes the specified pipeline and cleans up associated resources.
     * Shuts down the thread pool associated with the pipeline if it exists,
     * waits for its termination, and notifies all registered pipeline listeners
     * about the removal event.
     *
     * @param pipeline the pipeline to be removed; must not be null
     * @param pe       the pipeline event associated with the removal; must not be null
     */
    @SuppressWarnings("resource")
    private void removePipeline(final @NonNull Pipeline pipeline, final @NonNull PipelineEvent pe) {
        final ExecutorService es = pipelinePools.get(pipeline);
        if (es == null) {
            pipelinePools.remove(pipeline);
        }
        else {
            es.shutdown();
            try {
                if (!es.awaitTermination(5, TimeUnit.SECONDS))
                    es.shutdownNow();
            }
            catch (final InterruptedException e) {
                es.shutdownNow();
            }
            finally {
                pipelinePools.remove(pipeline);
            }
        }

        for (final PipelineListener listener : pipelineListeners)
            listener.onRemoved(pe);
    }

    /**
     * Builds a {@link CompletableFuture} for the specified task within a pipeline, ensuring that the task's
     * dependencies are resolved recursively. If the task has no dependencies, it is scheduled for execution
     * immediately. Otherwise, the task waits for its dependencies to complete before being scheduled. Tasks
     * are memoized, ensuring that each task gets exactly one future.
     *
     * @param pipeline the pipeline related to the task; must not be null
     * @param st       the task for which the future is being built; must not be null
     * @param index    a map of task IDs to their associated tasks; must not be null
     * @param futures  a map of task names to their respective CompletableFutures, used for memoization; must not be null
     * @return a {@link CompletableFuture} representing the task's execution, which completes once the task
     * and its dependencies (if any) are executed
     */
    private CompletableFuture<Void> buildFuture(final @NonNull Pipeline pipeline, final @NonNull Task st, final @NonNull Map<Long, Task> index, final @NonNull Map<String, CompletableFuture<Void>> futures) {
        // Memoized – each subtask gets exactly one future
        if (futures.containsKey(st.getName())) return futures.get(st.getName());

        // Recursively resolve dependency futures
        final List<CompletableFuture<Void>> depFutures = new ArrayList<>();
        for (final Long dep : st.getDependencies())
            depFutures.add(buildFuture(pipeline, index.get(dep), index, futures));

        final CompletableFuture<Void> future;

        if (depFutures.isEmpty()) {
            // No deps → schedule immediately
            future = CompletableFuture.runAsync(() -> runTask(st), pipelinePools.get(pipeline));
        }
        else {
            // Wait for all deps; if any dep failed → cancel this subtask
            future = CompletableFuture
                    .allOf(depFutures.toArray(new CompletableFuture[0]))
                    .handle((v, ex) -> {
                        if (ex != null) {
                            st.setStatus(TaskStatus.CANCELLED);
                            throw (ex instanceof CompletionException) ? (CompletionException) ex : new CompletionException(ex);
                        }
                        return v;
                    })
                    .thenRunAsync(() -> runTask(st), pipelinePools.get(pipeline));
        }

        futures.put(st.getName(), future);
        return future;
    }

    /**
     * Executes the provided task by updating its status, performing the task's
     * execution logic, and handling any exceptions that may occur during the process.
     *
     * @param task the task to be executed; must not be null
     * @throws CompletionException if an exception occurs during task execution
     */
    private void runTask(final @NonNull Task task) {
        task.setStatus(TaskStatus.RUNNING);
        try {
            task.execute();
            task.markCompleted();
        }
        catch (final Exception e) {
            task.markFailed(e);
            throw new CompletionException(e);
        }
    }

    /**
     * Stops the specified pipeline by removing it from the pool of active pipelines
     * and cleaning up associated resources. If the system does not currently
     * manage the given pipeline, this method returns without performing any operation.
     *
     * @param pipeline the pipeline to be stopped; must not be null
     */
    public synchronized void stop(final @NonNull Pipeline pipeline) {
        synchronized (pipelinePools) {
            if (!pipelinePools.containsKey(pipeline))
                return;

            // Do not wait for shutdown
            pipelinePools.get(pipeline).shutdownNow();

            removePipeline(pipeline, new PipelineEvent(pipeline));
        }
    }

    /**
     * Retrieves a synchronized view of all currently active pipelines managed by the system.
     * The active pipelines are represented as a set of unique pipeline instances.
     *
     * @return a non-null set of active pipelines; an empty set if no pipelines are active.
     */
    @Contract(pure = true)
    public synchronized @NonNull Set<Pipeline> getActivePipelines() {
        synchronized (pipelinePools) {
            return pipelinePools.keySet();
        }
    }

    /**
     * Calculates and retrieves the total progress, in percentage, of all currently active pipelines
     * managed by the system. The progress is computed as the average progress of all active pipelines.
     * If no pipelines are active, the method returns 0. The returned value is capped at a maximum of 100.
     *
     * @return the total progress of all active pipelines, as an integer between 0 and 100 inclusive.
     */
    public synchronized int getTotalProgress() {
        synchronized (pipelinePools) {
            if (pipelinePools.isEmpty())
                return 0;

            int total = 0;
            final Set<Pipeline> pipelines = getActivePipelines();
            for (final Pipeline pipeline : pipelines) {
                total += pipeline.getTotalProgress();
            }
            return Math.min(100, total / pipelines.size());
        }
    }
}
