package ir.netpick.mailmine.auth.model;

import ir.netpick.mailmine.common.BaseEntity;
import ir.netpick.mailmine.common.enums.RoleEnum;
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

}
