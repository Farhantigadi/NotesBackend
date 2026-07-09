package com.interviewprep.question.entity;

import com.interviewprep.auth.User;
import com.interviewprep.common.util.Auditable;
import com.interviewprep.subsection.entity.SubSection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    @Column(columnDefinition = "LONGTEXT")
    private String codeSnippet;

    @Column(length = 100)
    private String codeLanguage;

    @Column(columnDefinition = "LONGTEXT")
    private String codeBlocks; // JSON array: [{"code":"...","language":"..."}]

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    private Integer displayOrder;

    @Column(length = 512)
    private String imageUrl;

    private Integer imageWidth;

    @Column(length = 10)
    private String imageAlign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_section_id", nullable = false)
    private SubSection subSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
