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
 * Functional interface for monitoring the progress of a task.
 * Implementations of this interface can be used to receive updates on
 * task progress, status, and optional details such as messages or errors.
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * Handles the progress of a task, providing updates about the task's current state, progress percentage,
     * and optional status details or errors.
     *
     * @param event an instance of {@code ProgressEvent} containing information about the task's progress,
     *              including the task name, progress percentage (0–100), task status, an optional message,
     *              and an optional error if applicable.
     */
    void onProgress(ProgressEvent event);
}
