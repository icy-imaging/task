package fr.icy.lib.task;

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