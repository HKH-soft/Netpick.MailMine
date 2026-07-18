package ir.netpick.platform.taskfarm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task_labels")
public class TaskLabel {

    @EmbeddedId
    private TaskLabelId id = new TaskLabelId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("labelId")
    @JoinColumn(name = "label_id")
    private Label label;

    public TaskLabel() {
    }

    public TaskLabel(UUID taskId, UUID labelId) {
        this.id = new TaskLabelId(taskId, labelId);
    }

    @Getter
    @Setter
    @Embeddable
    public static class TaskLabelId implements java.io.Serializable {
        @Column(name = "task_id")
        private UUID taskId;

        @Column(name = "label_id")
        private UUID labelId;

        public TaskLabelId() {
        }

        public TaskLabelId(UUID taskId, UUID labelId) {
            this.taskId = taskId;
            this.labelId = labelId;
        }
    }
}