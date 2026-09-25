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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskTest {
    static class ConcreteTask extends Task {
        private final boolean shouldThrowException;

        public ConcreteTask(final String name, final boolean shouldThrowException, final Long... dependencies) {
            super(name, dependencies);
            this.shouldThrowException = shouldThrowException;
        }

        @Override
        public void execute() throws RuntimeException {
            if (shouldThrowException) {
                throw new RuntimeException("Execution failed.");
            }
            reportProgress(50, "Halfway there");
            reportProgress(100, "Completed");
            markCompleted();
        }
    }

    @Test
    void testExecuteSuccessfulExecution() {
        // Arrange
        final ConcreteTask task = new ConcreteTask("Successful Task", false);
        final ProgressListener mockListener = mock(ProgressListener.class);
        task.addProgressListener(mockListener);

        // Act
        task.execute();

        // Assert
        assertEquals(100, task.getProgress());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals("Completed", task.getMessage());
        assertTrue(task.getError().isEmpty());

        verify(mockListener, atLeast(1)).onProgress(any(ProgressEvent.class));
    }

    @Test
    void testExecuteFailedExecution() {
        // Arrange
        final ConcreteTask task = new ConcreteTask("Failing Task", true);
        final ProgressListener mockListener = mock(ProgressListener.class);
        task.addProgressListener(mockListener);

        final Pipeline pipeline = new Pipeline("TestPipeline");
        pipeline.addTask(task);

        final TaskExecutor executor = TaskExecutor.getInstance();

        // Act & Assert
        final TaskExecutionException thrown = assertThrows(TaskExecutionException.class, () -> executor.execute(pipeline));
        assertEquals("Pipeline 'TestPipeline' failed – failed tasks: [Failing Task]", thrown.getMessage());
        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertTrue(task.getError().isPresent());
        assertEquals("Execution failed.", task.getError().get().getMessage());

        verify(mockListener, atLeast(1)).onProgress(any(ProgressEvent.class));
    }

    @Test
    void testExecuteProgressReporting() {
        // Arrange
        final ConcreteTask task = new ConcreteTask("Progress Task", false);
        final ProgressListener mockListener = mock(ProgressListener.class);
        task.addProgressListener(mockListener);

        // Act
        task.execute();

        // Assert
        verify(mockListener, atLeastOnce()).onProgress(argThat(event -> event.getProgress() == 50 && "Halfway there".equals(event.getMessage())));
        verify(mockListener, atLeastOnce()).onProgress(argThat(event -> event.getProgress() == 100 && "Completed".equals(event.getMessage())));
    }
}