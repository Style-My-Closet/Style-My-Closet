package com.stylemycloset.cloth.entity;

import com.stylemycloset.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "closets")
@Getter
@NoArgsConstructor
public class Closet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long closetId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;


    @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cloth> clothes = new ArrayList<>();

}
