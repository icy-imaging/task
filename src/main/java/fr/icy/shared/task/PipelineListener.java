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
