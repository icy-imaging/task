package fr.icy.shared.task;

/**
 * The {@code PipelineListener} interface should be implemented by components that need to respond
 * to changes in a pipeline system. This includes receiving notifications when pipelines are added or removed.
 * The interface serves as a mechanism for event-driven updates to ensure processing components are
 * aware of pipeline lifecycle changes.
 */
public interface PipelineListener {
    /**
     * Handles the event when a pipeline is added. This method is invoked when a {@link PipelineEvent}
     * occurs, specifically to notify listeners about the addition of a new pipeline.
     *
     * @param event the pipeline event containing information about the added pipeline; must not be null
     */
    void onAdded(PipelineEvent event);

    /**
     * Handles the event when a pipeline is removed. This method is invoked to notify listeners
     * about the removal of a pipeline identified by the associated {@link PipelineEvent}.
     *
     * @param event the pipeline event containing information about the removed pipeline; must not be null
     */
    void onRemoved(PipelineEvent event);
}
