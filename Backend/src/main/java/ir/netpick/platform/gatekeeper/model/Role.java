package ir.netpick.platform.gatekeeper.model;

import ir.netpick.platform.core.BaseEntity;
import ir.netpick.platform.core.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    public Role() {
    }

    public Role(RoleEnum name) {
        this.name = name;
    }

    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

}









