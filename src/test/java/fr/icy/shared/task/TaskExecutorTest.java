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

public class TaskExecutorTest {
    /**
     * Test the Singleton property of the getInstance method.
     * Ensures that subsequent calls return the same instance.
     */
    @Test
    public void testGetInstanceReturnsSingleton() {
        final TaskExecutor instance1 = TaskExecutor.getInstance();
        final TaskExecutor instance2 = TaskExecutor.getInstance();

        assertNotNull(instance1, "Instance1 should not be null");
        assertNotNull(instance2, "Instance2 should not be null");
        assertSame(instance1, instance2, "Both instances should be the same (singleton)");
    }

    /**
     * Test if the instance is initialized correctly without any exceptions.
     */
    @Test
    public void testGetInstanceInitialization() {
        assertDoesNotThrow(() -> {
            final TaskExecutor instance = TaskExecutor.getInstance();
            assertNotNull(instance, "TaskExecutor instance should not be null");
        });
    }
}