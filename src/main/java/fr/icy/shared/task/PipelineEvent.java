package fr.icy.shared.task;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Represents an event in a processing pipeline. This class acts as a base for events
 * that are associated with a specific {@link Pipeline} instance.
 *
 * <p>PipelineEvent is immutable and holds a reference to the {@link Pipeline}
 * that is related to the event. It provides access to the associated pipeline object.
 */
public final class PipelineEvent {
    private final @NonNull Pipeline pipeline;

    /**
     * Constructs a new PipelineEvent associated with the specified pipeline.
     *
     * @param pipeline the {@link Pipeline} instance that this event is associated with;
     *                 must not be null
     */
    @Contract(pure = true)
    PipelineEvent(final @NonNull Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Retrieves the {@link Pipeline} instance associated with this event.
     *
     * @return the non-null {@link Pipeline} instance linked to this event.
     */
    @Contract(pure = true)
    public @NonNull Pipeline getPipeline() {
        return pipeline;
    }
}
