package ir.netpick.platform.taskfarm.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Nested
    @DisplayName("TaskStatus Enum Tests")
    class TaskStatusTests {
        @Test
        @DisplayName("Should have all expected status values")
        void shouldHaveAllStatuses() {
            assertEquals(5, TaskStatus.values().length);
            assertNotNull(TaskStatus.valueOf("TODO"));
            assertNotNull(TaskStatus.valueOf("IN_PROGRESS"));
            assertNotNull(TaskStatus.valueOf("IN_REVIEW"));
            assertNotNull(TaskStatus.valueOf("BLOCKED"));
            assertNotNull(TaskStatus.valueOf("DONE"));
        }
    }

    @Nested
    @DisplayName("TaskPriority Enum Tests")
    class TaskPriorityTests {
        @Test
        @DisplayName("Should have all expected priority values")
        void shouldHaveAllPriorities() {
            assertNotNull(TaskPriority.valueOf("LOW"));
            assertNotNull(TaskPriority.valueOf("MEDIUM"));
            assertNotNull(TaskPriority.valueOf("HIGH"));
            assertNotNull(TaskPriority.valueOf("URGENT"));
        }
    }

    @Nested
    @DisplayName("Task Entity Tests")
    class TaskEntityTests {
        @Test
        @DisplayName("Should create task with default values")
        void shouldCreateWithDefaults() {
            Task task = new Task();

            assertEquals(TaskStatus.TODO, task.getStatus());
            assertEquals(TaskPriority.MEDIUM, task.getPriority());
            assertFalse(task.isCompleted());
        }

        @Test
        @DisplayName("isCompleted should return false for non-DONE status")
        void isCompletedFalseForNonDone() {
            Task task = new Task();

            task.setStatus(TaskStatus.TODO);
            assertFalse(task.isCompleted());

            task.setStatus(TaskStatus.IN_PROGRESS);
            assertFalse(task.isCompleted());

            task.setStatus(TaskStatus.IN_REVIEW);
            assertFalse(task.isCompleted());

            task.setStatus(TaskStatus.BLOCKED);
            assertFalse(task.isCompleted());
        }

        @Test
        @DisplayName("isCompleted should return true for DONE status")
        void isCompletedTrueForDone() {
            Task task = new Task();
            task.setStatus(TaskStatus.DONE);

            assertTrue(task.isCompleted());
        }

        @Test
        @DisplayName("Should set all fields correctly")
        void shouldSetAllFields() {
            Task task = new Task();
            UUID projectId = UUID.randomUUID();
            UUID assigneeId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            LocalDateTime dueDate = LocalDateTime.now().plusDays(7);

            task.setId(UUID.randomUUID());
            task.setTitle("Test Task");
            task.setDescription("Description");
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setPriority(TaskPriority.HIGH);
            task.setProjectId(projectId);
            task.setAssigneeId(assigneeId);
            task.setCreatorId(creatorId);
            task.setDueDate(dueDate);
            task.setOrder(5);
            task.setCompletedAt(LocalDateTime.now());

            assertEquals("Test Task", task.getTitle());
            assertEquals("Description", task.getDescription());
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
            assertEquals(TaskPriority.HIGH, task.getPriority());
            assertEquals(projectId, task.getProjectId());
            assertEquals(assigneeId, task.getAssigneeId());
            assertEquals(creatorId, task.getCreatorId());
            assertEquals(dueDate, task.getDueDate());
            assertEquals(5, task.getOrder());
            assertNotNull(task.getCompletedAt());
        }
    }
}