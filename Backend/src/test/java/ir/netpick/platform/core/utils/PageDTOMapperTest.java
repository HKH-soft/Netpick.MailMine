package ir.netpick.platform.core.utils;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PageDTOMapperTest {

    @Test
    @DisplayName("map should convert Page to PageDTO correctly")
    void mapPageToDTO() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);

        Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 25);

        PageDTO<Task> result = PageDTOMapper.map(page);

        assertEquals(1, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(1, result.numberOfElements());
        assertEquals(1, result.content().size());
        assertSame(task, result.content().get(0));
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }

    @Test
    @DisplayName("map with mapper should transform content")
    void mapWithMapper() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test Task");

        Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 5), 1);

        PageDTO<String> result = PageDTOMapper.map(page, t -> t.getTitle().toUpperCase());

        assertEquals(1, result.content().size());
        assertEquals("TEST TASK", result.content().get(0));
    }

    @Test
    @DisplayName("map empty page should work correctly")
    void mapEmptyPage() {
        Page<Task> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageDTO<Task> result = PageDTOMapper.map(emptyPage);

        assertEquals(1, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(0L, result.totalElements());
        assertTrue(result.numberOfElements() == 0);
        assertTrue(result.content().isEmpty());
    }

    @Test
    @DisplayName("map should handle pagination flags correctly")
    void mapPaginationFlags() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        Task task2 = new Task();
        task2.setTitle("Task 2");

        Page<Task> page = new PageImpl<>(List.of(task1, task2), PageRequest.of(1, 2), 5);

        PageDTO<Task> result = PageDTOMapper.map(page);

        assertEquals(2, result.currentPage());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.hasPrevious());
    }
}