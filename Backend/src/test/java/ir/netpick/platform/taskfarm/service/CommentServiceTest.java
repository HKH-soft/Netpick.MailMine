package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Comment;
import ir.netpick.platform.taskfarm.repository.CommentRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private UUID testCommentId;
    private UUID testTaskId;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testCommentId = UUID.randomUUID();
        testTaskId = UUID.randomUUID();

        testComment = new Comment();
        testComment.setId(testCommentId);
        testComment.setContent("Test comment");
        testComment.setTaskId(testTaskId);
    }

    @Nested
    @DisplayName("getByTask Tests")
    class GetByTaskTests {
        @Test
        @DisplayName("Should return comments for task")
        void shouldReturnCommentsForTask() {
            when(commentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(testTaskId))
                    .thenReturn(List.of(testComment));

            List<Comment> result = commentService.getByTask(testTaskId);

            assertEquals(1, result.size());
            assertEquals(testComment, result.get(0));
        }

        @Test
        @DisplayName("Should return empty list when no comments")
        void shouldReturnEmptyList() {
            when(commentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(testTaskId))
                    .thenReturn(List.of());

            List<Comment> result = commentService.getByTask(testTaskId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {
        @Test
        @DisplayName("Should return paginated comments")
        void shouldReturnPaginatedComments() {
            Page<Comment> mockPage = new PageImpl<>(List.of(testComment));
            when(commentRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(mockPage);

            PageDTO<Comment> result = commentService.getAll(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Should return comment when found")
        void shouldReturnCommentWhenFound() {
            when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

            Comment result = commentService.getById(testCommentId);

            assertNotNull(result);
            assertEquals(testCommentId, result.getId());
        }

        @Test
        @DisplayName("Should throw when comment not found")
        void shouldThrowWhenNotFound() {
            when(commentRepository.findById(testCommentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> commentService.getById(testCommentId));
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create comment with null ID")
        void shouldCreateComment() {
            Comment newComment = new Comment();
            newComment.setContent("New comment");
            newComment.setId(UUID.randomUUID());

            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
                Comment c = inv.getArgument(0);
                c.setId(testCommentId);
                return c;
            });

            Comment result = commentService.create(newComment);

            assertNotNull(result.getId());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update existing comment")
        void shouldUpdateComment() {
            Comment updateComment = new Comment();
            updateComment.setContent("Updated content");

            when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

            Comment result = commentService.update(testCommentId, updateComment);

            assertEquals("Updated content", result.getContent());
        }

        @Test
        @DisplayName("Should throw when comment not found")
        void shouldThrowWhenUpdatingNonExistent() {
            when(commentRepository.findById(testCommentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> commentService.update(testCommentId, new Comment()));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should delete comment")
        void shouldDeleteComment() {
            commentService.delete(testCommentId);

            verify(commentRepository).deleteById(testCommentId);
        }
    }
}