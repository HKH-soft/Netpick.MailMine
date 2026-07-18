package ir.netpick.platform.taskfarm.model;

import ir.netpick.platform.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "labels")
public class Label extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "created_by_id")
    private UUID createdById;

    public Label() {
    }
}