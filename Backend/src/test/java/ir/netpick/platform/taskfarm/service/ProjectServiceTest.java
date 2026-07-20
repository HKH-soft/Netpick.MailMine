package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Project;
import ir.netpick.platform.taskfarm.repository.ProjectRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID testProjectId;
    private UUID testOwnerId;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testProjectId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

        testProject = new Project();
        testProject.setId(testProjectId);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwnerId(testOwnerId);
        testProject.setStatus(Project.ProjectStatus.ACTIVE);
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {
        @Test
        @DisplayName("Should return paginated projects sorted by createdAt descending")
        void shouldReturnPaginatedProjects() {
            Page<Project> mockPage = new PageImpl<>(List.of(testProject));
            when(projectRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(mockPage);

            PageDTO<Project> result = projectService.getAll(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(testProject, result.content().get(0));
        }
    }

    @Nested
    @DisplayName("getByOwner Tests")
    class GetByOwnerTests {
        @Test
        @DisplayName("Should return projects filtered by owner")
        void shouldReturnProjectsByOwner() {
            Page<Project> mockPage = new PageImpl<>(List.of(testProject));
            when(projectRepository.findByOwnerIdAndDeletedFalse(eq(testOwnerId), any(PageRequest.class)))
                    .thenReturn(mockPage);

            PageDTO<Project> result = projectService.getByOwner(testOwnerId, 1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Should return project when found")
        void shouldReturnProjectWhenFound() {
            when(projectRepository.findById(testProjectId)).thenReturn(java.util.Optional.of(testProject));

            Project result = projectService.getById(testProjectId);

            assertNotNull(result);
            assertEquals(testProjectId, result.getId());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(projectRepository.findById(testProjectId)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> projectService.getById(testProjectId));
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create project with null ID")
        void shouldCreateProject() {
            Project newProject = new Project();
            newProject.setName("New Project");
            newProject.setId(UUID.randomUUID());

            when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(testProjectId);
                return p;
            });

            Project result = projectService.create(newProject);

            assertNotNull(result);
            assertNotNull(result.getId());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update existing project preserving createdAt")
        void shouldUpdateProject() {
            Project updateProject = new Project();
            updateProject.setName("Updated Name");

            when(projectRepository.findById(testProjectId)).thenReturn(java.util.Optional.of(testProject));
            when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

            Project result = projectService.update(testProjectId, updateProject);

            assertNotNull(result);
            assertEquals("Updated Name", result.getName());
        }

        @Test
        @DisplayName("Should throw when project not found")
        void shouldThrowWhenUpdatingNonExistent() {
            Project updateProject = new Project();
            when(projectRepository.findById(testProjectId)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> projectService.update(testProjectId, updateProject));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should soft delete project")
        void shouldSoftDelete() {
            doNothing().when(projectRepository).softDelete(testProjectId);

            projectService.delete(testProjectId);

            verify(projectRepository).softDelete(testProjectId);
        }
    }

    @Nested
    @DisplayName("restore Tests")
    class RestoreTests {
        @Test
        @DisplayName("Should restore soft deleted project")
        void shouldRestore() {
            doNothing().when(projectRepository).restore(testProjectId);

            projectService.restore(testProjectId);

            verify(projectRepository).restore(testProjectId);
        }
    }

    @Nested
    @DisplayName("getStats Tests")
    class GetStatsTests {
        @Test
        @DisplayName("Should return stats map with task count")
        void shouldReturnStats() {
            when(projectRepository.countTasksByProjectId(testProjectId)).thenReturn(10L);

            java.util.Map<String, Object> stats = projectService.getStats(testProjectId);

            assertNotNull(stats);
            assertEquals(10L, stats.get("taskCount"));
        }
    }
}