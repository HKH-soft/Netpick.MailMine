package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.taskfarm.model.Comment;
import ir.netpick.platform.taskfarm.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getByTask(UUID taskId) {
        return commentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(taskId);
    }

    public PageDTO<Comment> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<Comment> page = commentRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public Comment getById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id [%s] not found".formatted(commentId)));
    }

    public Comment create(Comment comment) {
        comment.setId(null);
        return commentRepository.save(comment);
    }

    public Comment update(UUID commentId, Comment comment) {
        Comment existing = getById(commentId);
        comment.setId(commentId);
        comment.setCreatedAt(existing.getCreatedAt());
        return commentRepository.save(comment);
    }

    public void delete(UUID commentId) {
        commentRepository.deleteById(commentId);
    }
}