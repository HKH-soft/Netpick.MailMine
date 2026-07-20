package ir.netpick.platform.taskfarm.controller;

import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskPriority;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import ir.netpick.platform.taskfarm.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ir.netpick.platform.taskfarm.service.TaskService taskService;

    private UUID testTaskId = UUID.randomUUID();
    private UUID testProjectId = UUID.randomUUID();

    @Nested
    @DisplayName("GET /api/v1/taskfarm/tasks")
    class GetAllTasks {
        @Test
        @DisplayName("Should return all tasks with admin role")
        void shouldReturnAllTasks() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setTitle("Test Task");
            task.setStatus(TaskStatus.TODO);

            Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
            when(taskService.getAll(1)).thenReturn(
                    new ir.netpick.platform.core.PageDTO<>(page.getContent(), 1, 10, 1, 1L, 1, false, false, true, true));

            mockMvc.perform(get("/api/v1/taskfarm/tasks"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/taskfarm/tasks/{id}")
    class GetTaskById {
        @Test
        @DisplayName("Should return task when found")
        void shouldReturnTask() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setTitle("Test Task");

            when(taskService.getById(testTaskId)).thenReturn(task);

            mockMvc.perform(get("/api/v1/taskfarm/tasks/{id}", testTaskId))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/taskfarm/tasks/status/{status}")
    class GetTasksByStatus {
        @Test
        @DisplayName("Should return tasks by status")
        void shouldReturnTasksByStatus() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setStatus(TaskStatus.IN_PROGRESS);

            Page<Task> page = new PageImpl<>(List.of(task));
            when(taskService.getByStatus(eq(TaskStatus.IN_PROGRESS), eq(1)))
                    .thenReturn(new ir.netpick.platform.core.PageDTO<>(page.getContent(), 1, 10, 1, 1L, 1, false, false, true, true));

            mockMvc.perform(get("/api/v1/taskfarm/tasks/status/IN_PROGRESS"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturnBadRequestForInvalidStatus() throws Exception {
            mockMvc.perform(get("/api/v1/taskfarm/tasks/status/INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/taskfarm/tasks/project/{projectId}")
    class GetTasksByProject {
        @Test
        @DisplayName("Should return tasks by project")
        void shouldReturnTasksByProject() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setProjectId(testProjectId);

            Page<Task> page = new PageImpl<>(List.of(task));
            when(taskService.getByProject(eq(testProjectId), eq(1)))
                    .thenReturn(new ir.netpick.platform.core.PageDTO<>(page.getContent(), 1, 10, 1, 1L, 1, false, false, true, true));

            mockMvc.perform(get("/api/v1/taskfarm/tasks/project/{projectId}", testProjectId))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/taskfarm/tasks")
    class CreateTask {
        @Test
        @DisplayName("Should create task with valid request")
        void shouldCreateTask() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setTitle("New Task");
            task.setStatus(TaskStatus.TODO);

            when(taskService.create(any(Task.class))).thenReturn(task);

            mockMvc.perform(post("/api/v1/taskfarm/tasks")
                            .contentType("application/json")
                            .content("{\"title\":\"New Task\",\"description\":\"Desc\",\"status\":\"TODO\",\"priority\":\"MEDIUM\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 for invalid status in request")
        void shouldReturnBadRequestForInvalidStatus() throws Exception {
            mockMvc.perform(post("/api/v1/taskfarm/tasks")
                            .contentType("application/json")
                            .content("{\"title\":\"New Task\",\"status\":\"INVALID\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/taskfarm/tasks/{id}")
    class UpdateTask {
        @Test
        @DisplayName("Should update task with valid request")
        void shouldUpdateTask() throws Exception {
            Task task = new Task();
            task.setId(testTaskId);
            task.setTitle("Updated Task");

            when(taskService.update(eq(testTaskId), any(Task.class))).thenReturn(task);

            mockMvc.perform(post("/api/v1/taskfarm/tasks/{id}", testTaskId)
                            .contentType("application/json")
                            .content("{\"title\":\"Updated Task\",\"status\":\"IN_PROGRESS\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/taskfarm/tasks/{id}")
    class DeleteTask {
        @Test
        @DisplayName("Should delete task")
        void shouldDeleteTask() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/taskfarm/tasks/{id}", testTaskId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/taskfarm/tasks/{id}/restore")
    class RestoreTask {
        @Test
        @DisplayName("Should restore soft deleted task")
        void shouldRestoreTask() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/taskfarm/tasks/{id}/restore", testTaskId))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/taskfarm/tasks/{id}/reorder")
    class ReorderTask {
        @Test
        @DisplayName("Should reorder task")
        void shouldReorderTask() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/taskfarm/tasks/{id}/reorder", testTaskId)
                            .contentType("application/json")
                            .content("{\"status\":\"DONE\",\"order\":5}"))
                    .andExpect(status().isOk());
        }
    }
}