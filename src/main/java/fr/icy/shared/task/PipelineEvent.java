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
