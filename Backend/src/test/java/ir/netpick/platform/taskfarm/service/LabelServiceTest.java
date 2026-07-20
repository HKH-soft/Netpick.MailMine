package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Label;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskLabel;
import ir.netpick.platform.taskfarm.repository.LabelRepository;
import ir.netpick.platform.taskfarm.repository.TaskLabelRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private TaskLabelRepository taskLabelRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private LabelService labelService;

    private UUID testLabelId;
    private UUID testProjectId;
    private UUID testTaskId;
    private Label testLabel;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testLabelId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();
        testTaskId = UUID.randomUUID();

        testLabel = new Label();
        testLabel.setId(testLabelId);
        testLabel.setName("Test Label");
        testLabel.setProjectId(testProjectId);

        testTask = new Task();
        testTask.setId(testTaskId);
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {
        @Test
        @DisplayName("Should return paginated labels")
        void shouldReturnPaginatedLabels() {
            Page<Label> mockPage = new PageImpl<>(List.of(testLabel));
            when(labelRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(mockPage);

            PageDTO<Label> result = labelService.getAll(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getByProject Tests")
    class GetByProjectTests {
        @Test
        @DisplayName("Should return labels filtered by project")
        void shouldReturnLabelsByProject() {
            when(labelRepository.findByProjectIdAndDeletedFalse(testProjectId)).thenReturn(List.of(testLabel));

            List<Label> result = labelService.getByProject(testProjectId);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Should return label when found")
        void shouldReturnLabelWhenFound() {
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));

            Label result = labelService.getById(testLabelId);

            assertNotNull(result);
            assertEquals(testLabelId, result.getId());
        }

        @Test
        @DisplayName("Should throw when label not found")
        void shouldThrowWhenNotFound() {
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> labelService.getById(testLabelId));
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create label with null ID")
        void shouldCreateLabel() {
            Label newLabel = new Label();
            newLabel.setName("New Label");
            newLabel.setId(UUID.randomUUID());

            when(labelRepository.save(any(Label.class))).thenAnswer(inv -> {
                Label l = inv.getArgument(0);
                l.setId(testLabelId);
                return l;
            });

            Label result = labelService.create(newLabel);

            assertNotNull(result.getId());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update existing label")
        void shouldUpdateLabel() {
            Label updateLabel = new Label();
            updateLabel.setName("Updated Name");

            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(labelRepository.save(any(Label.class))).thenAnswer(inv -> inv.getArgument(0));

            Label result = labelService.update(testLabelId, updateLabel);

            assertEquals("Updated Name", result.getName());
        }

        @Test
        @DisplayName("Should throw when label not found for update")
        void shouldThrowWhenUpdatingNonExistent() {
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> labelService.update(testLabelId, new Label()));
        }
    }

    @Nested
    @DisplayName("addLabelToTask Tests")
    class AddLabelToTaskTests {
        @Test
        @DisplayName("Should throw when task not found")
        void shouldThrowWhenTaskNotFound() {
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> labelService.addLabelToTask(testTaskId, testLabelId));
        }

        @Test
        @DisplayName("Should add label to task")
        void shouldAddLabelToTask() {
            when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
            when(labelRepository.findById(testLabelId)).thenReturn(Optional.of(testLabel));
            when(taskLabelRepository.save(any(TaskLabel.class))).thenAnswer(inv -> inv.getArgument(0));

            labelService.addLabelToTask(testTaskId, testLabelId);

            verify(taskLabelRepository).save(any(TaskLabel.class));
        }
    }

    @Nested
    @DisplayName("getLabelsForTask Tests")
    class GetLabelsForTaskTests {
        @Test
        @DisplayName("Should return empty list when no labels")
        void shouldReturnEmptyWhenNoLabels() {
            when(taskLabelRepository.findByTaskId(testTaskId)).thenReturn(List.of());

            List<Label> result = labelService.getLabelsForTask(testTaskId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return labels for task")
        void shouldReturnLabelsForTask() {
            TaskLabel taskLabel = new TaskLabel(testTaskId, testLabelId);
            taskLabel.setLabel(testLabel);

            when(taskLabelRepository.findByTaskId(testTaskId)).thenReturn(List.of(taskLabel));

            List<Label> result = labelService.getLabelsForTask(testTaskId);

            assertEquals(1, result.size());
        }
    }
}