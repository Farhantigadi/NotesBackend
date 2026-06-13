package com.interviewprep.section.entity;

import com.interviewprep.common.util.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "main_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainSection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
}
