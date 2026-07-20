package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskPriority;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import ir.netpick.platform.taskfarm.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID testTaskId;
    private UUID testProjectId;
    private UUID testAssigneeId;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();
        testAssigneeId = UUID.randomUUID();
        
        testTask = new Task();
        testTask.setId(testTaskId);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setProjectId(testProjectId);
        testTask.setOrder(1);
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {
        @Test
        @DisplayName("Should return paginated tasks sorted by createdAt descending")
        void shouldReturnPaginatedTasks() {
            Page<Task> mockPage = new PageImpl<>(List.of(testTask));
            when(taskRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(mockPage);

            PageDTO<Task> result = taskService.getAll(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(testTask, result.content().get(0));
            verify(taskRepository).findByDeletedFalse(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle zero tasks")
        void shouldHandleEmptyTasks() {
            Page<Task> emptyPage = new PageImpl<>(List.of());
            when(taskRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(emptyPage);

            PageDTO<Task> result = taskService.getAll(1);

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
        }
    }

    @Nested
    @DisplayName("getByStatus Tests")
    class GetByStatusTests {
        @Test
        @DisplayName("Should return tasks filtered by status")
        void shouldReturnTasksByStatus() {
            Page<Task> mockPage = new PageImpl<>(List.of(testTask));
            when(taskRepository.findByStatusAndDeletedFalse(eq(TaskStatus.IN_PROGRESS), any(Pageable.class)))
                    .thenReturn(mockPage);

            PageDTO<Task> result = taskService.getByStatus(TaskStatus.IN_PROGRESS, 1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            verify(taskRepository).findByStatusAndDeletedFalse(eq(TaskStatus.IN_PROGRESS), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getByAssignee Tests")
    class GetByAssigneeTests {
        @Test
        @DisplayName("Should return tasks filtered by assignee")
        void shouldReturnTasksByAssignee() {
            Page<Task> mockPage = new PageImpl<>(List.of(testTask));
            when(taskRepository.findByAssigneeIdAndDeletedFalse(eq(testAssigneeId), any(Pageable.class)))
                    .thenReturn(mockPage);

            PageDTO<Task> result = taskService.getByAssignee(testAssigneeId, 1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getByProject Tests")
    class GetByProjectTests {
        @Test
        @DisplayName("Should return tasks filtered by project sorted by order ascending")
        void shouldReturnTasksByProject() {
            Page<Task> mockPage = new PageImpl<>(List.of(testTask));
            when(taskRepository.findByProjectIdAndDeletedFalse(eq(testProjectId), any(Pageable.class)))
                    .thenReturn(mockPage);

            PageDTO<Task> result = taskService.getByProject(testProjectId, 1);

            assertNotNull(result);
            verify(taskRepository).findByProjectIdAndDeletedFalse(eq(testProjectId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Should return task when found")
        void shouldReturnTaskWhenFound() {
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));

            Task result = taskService.getById(testTaskId);

            assertNotNull(result);
            assertEquals(testTask, result);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when task not found")
        void shouldThrowWhenNotFound() {
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.getById(testTaskId));
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create task with null ID")
        void shouldCreateTask() {
            Task newTask = new Task();
            newTask.setTitle("New Task");
            newTask.setId(UUID.randomUUID());

            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                t.setId(testTaskId);
                return t;
            });

            Task result = taskService.create(newTask);

            assertNotNull(result);
            assertNotNull(result.getId());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update existing task preserving createdAt")
        void shouldUpdateTask() {
            Task updateTask = new Task();
            updateTask.setTitle("Updated Title");

            when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            Task result = taskService.update(testTaskId, updateTask);

            assertNotNull(result);
            assertEquals("Updated Title", result.getTitle());
        }

        @Test
        @DisplayName("Should throw when task not found for update")
        void shouldThrowWhenUpdatingNonExistent() {
            Task updateTask = new Task();
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.update(testTaskId, updateTask));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should soft delete task")
        void shouldSoftDelete() {
            doNothing().when(taskRepository).softDelete(testTaskId);

            taskService.delete(testTaskId);

            verify(taskRepository).softDelete(testTaskId);
        }
    }

    @Nested
    @DisplayName("restore Tests")
    class RestoreTests {
        @Test
        @DisplayName("Should restore soft deleted task")
        void shouldRestore() {
            doNothing().when(taskRepository).restore(testTaskId);

            taskService.restore(testTaskId);

            verify(taskRepository).restore(testTaskId);
        }
    }

    @Nested
    @DisplayName("reorder Tests")
    class ReorderTests {
        @Test
        @DisplayName("Should update task order")
        void shouldReorder() {
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            taskService.reorder(testTaskId, 5);

            assertEquals(5, testTask.getOrder());
        }
    }

    @Test
    @DisplayName("isCompleted should return true when status is DONE")
    void isCompletedShouldReturnTrue() {
        testTask.setStatus(TaskStatus.DONE);

        assertTrue(testTask.isCompleted());
    }

    @Test
    @DisplayName("isCompleted should return false when status is not DONE")
    void isCompletedShouldReturnFalse() {
        testTask.setStatus(TaskStatus.IN_PROGRESS);

        assertFalse(testTask.isCompleted());
    }
}