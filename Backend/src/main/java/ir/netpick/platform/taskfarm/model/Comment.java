package ir.netpick.platform.taskfarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "parent_id")
    private UUID parentId;

    public Comment() {
    }
}