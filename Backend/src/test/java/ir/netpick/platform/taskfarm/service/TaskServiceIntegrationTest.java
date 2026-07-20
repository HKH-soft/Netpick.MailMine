package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskPriority;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import ir.netpick.platform.taskfarm.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({ir.netpick.platform.core.constants.GeneralConstants.class, ir.netpick.platform.core.utils.PageDTOMapper.class})
class TaskServiceIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Nested
    @DisplayName("Full CRUD Flow")
    class CrudFlow {

        @Test
        @DisplayName("Should create and retrieve task")
        void shouldCreateAndRetrieveTask() {
            Task task = new Task();
            task.setTitle("Integration Test Task");
            task.setDescription("Testing task creation");
            task.setStatus(TaskStatus.TODO);
            task.setPriority(TaskPriority.HIGH);

            Task saved = taskService.create(task);
            assertNotNull(saved.getId());

            Task retrieved = taskService.getById(saved.getId());
            assertEquals("Integration Test Task", retrieved.getTitle());
        }

        @Test
        @DisplayName("Should update task")
        void shouldUpdateTask() {
            Task task = new Task();
            task.setTitle("Original Title");
            task.setStatus(TaskStatus.TODO);
            task.setPriority(TaskPriority.MEDIUM);

            Task saved = taskService.create(task);

            Task update = new Task();
            update.setTitle("Updated Title");
            update.setStatus(TaskStatus.IN_PROGRESS);
            update.setPriority(TaskPriority.MEDIUM);

            Task updated = taskService.update(saved.getId(), update);
            assertEquals("Updated Title", updated.getTitle());
            assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        }

        @Test
        @DisplayName("Should soft delete task")
        void shouldSoftDeleteTask() {
            Task task = new Task();
            task.setTitle("Task to Delete");
            task.setPriority(TaskPriority.LOW);
            task.setStatus(TaskStatus.TODO);

            Task saved = taskService.create(task);
            taskService.delete(saved.getId());

            assertThrows(ResourceNotFoundException.class, 
                    () -> taskService.getById(saved.getId()));
        }

        @Test
        @DisplayName("Should restore soft deleted task")
        void shouldRestoreTask() {
            Task task = new Task();
            task.setTitle("Task to Restore");
            task.setPriority(TaskPriority.LOW);
            task.setStatus(TaskStatus.TODO);

            Task saved = taskService.create(task);
            taskService.delete(saved.getId());
            taskService.restore(saved.getId());

            Task restored = taskService.getById(saved.getId());
            assertNotNull(restored);
            assertEquals("Task to Restore", restored.getTitle());
        }

        @Test
        @DisplayName("Should find by status")
        void shouldFindByStatus() {
            Task task = new Task();
            task.setTitle("Status Test Task");
            task.setPriority(TaskPriority.MEDIUM);
            task.setStatus(TaskStatus.DONE);

            taskService.create(task);

            var result = taskService.getByStatus(TaskStatus.DONE, 1);
            assertTrue(result.content().stream().anyMatch(t -> t.getTitle().equals("Status Test Task")));
        }

        @Test
        @DisplayName("Should find by project")
        void shouldFindByProject() {
            UUID projectId = UUID.randomUUID();

            Task task = new Task();
            task.setTitle("Project Task");
            task.setPriority(TaskPriority.MEDIUM);
            task.setStatus(TaskStatus.TODO);
            task.setProjectId(projectId);

            taskService.create(task);

            var result = taskService.getByProject(projectId, 1);
            assertTrue(result.content().stream().anyMatch(t -> t.getTitle().equals("Project Task")));
        }
    }
}