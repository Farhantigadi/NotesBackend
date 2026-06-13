package com.interviewprep.subsection.entity;

import com.interviewprep.common.util.Auditable;
import com.interviewprep.section.entity.MainSection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubSection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_section_id", nullable = false)
    private MainSection mainSection;
}
