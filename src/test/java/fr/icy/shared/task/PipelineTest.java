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

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {
    /**
     * Tests the `addTask` method to ensure a task can be successfully added to a pipeline.
     */
    @Test
    void testAddTaskSuccessfully() {
        final Pipeline pipeline = new Pipeline("TestPipeline");
        final Task task = new MockTask(1L, "Task1");

        final Pipeline result = pipeline.addTask(task);

        assertEquals(1, pipeline.getTasks().size());
        assertEquals(task, pipeline.getTasks().get(0));
        assertSame(pipeline, result, "The addTask method should return the same pipeline instance.");
    }

    /**
     * Tests the `addTask` method to ensure an exception is thrown when adding a task with a duplicate name.
     */
    @Test
    void testAddTaskDuplicateName() {
        final Pipeline pipeline = new Pipeline("TestPipeline");
        final Task task1 = new MockTask(1L, "Task1");
        final Task task2 = new MockTask(2L, "Task1"); // Same name as task1 but different UID

        pipeline.addTask(task1);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pipeline.addTask(task2);
        });

        assertEquals("Duplicate subtask name: 'Task1'", exception.getMessage());
    }

    /**
     * Tests the `addTask` method to ensure an exception is thrown when adding a task with a duplicate UID.
     */
    @Test
    void testAddTaskDuplicateUid() {
        final Pipeline pipeline = new Pipeline("TestPipeline");
        final Task task1 = new MockTask(1L, "Task1");
        final Task task2 = new MockTask(1L, "Task2"); // Same UID as task1 but different name

        pipeline.addTask(task1);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pipeline.addTask(task2);
        });

        assertEquals("Duplicate task ID: 1", exception.getMessage());
    }

    /**
     * Tests the `addTask` method to ensure an exception is thrown when adding a null task.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testAddTaskNullTask() {
        final Pipeline pipeline = new Pipeline("TestPipeline");

        final NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            pipeline.addTask(null);
        });

        assertEquals("SubTask must not be null", exception.getMessage());
    }

    /**
     * Mock Task implementation for testing purposes.
     */
    static class MockTask extends Task {
        private final long uid;
        private final String name;
        private final Set<Long> dependencies;
        private final int progress;
        private final TaskStatus status;

        MockTask(final long uid, final String name, final Long... dependencies) {
            super(uid, name, dependencies);
            this.uid = uid;
            this.name = name;
            this.dependencies = Set.of(dependencies);
            this.progress = 0;
            this.status = TaskStatus.PENDING;
        }

        @Override
        public long getUid() {
            return uid;
        }

        @Override
        public @NonNull String getName() {
            return name;
        }

        @Override
        public @NonNull Set<Long> getDependencies() {
            return dependencies;
        }

        @Override
        public void execute() {
            // Mock implementation
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public @NonNull TaskStatus getStatus() {
            return status;
        }
    }
}