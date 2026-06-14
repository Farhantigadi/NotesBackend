package com.interviewprep.section.entity;

import com.interviewprep.common.util.Auditable;
import com.interviewprep.subsection.entity.SubSection;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private Integer displayOrder;

    @OneToMany(mappedBy = "mainSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubSection> subSections = new ArrayList<>();
}
