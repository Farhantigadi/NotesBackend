package com.interviewprep.question.repository;

import com.interviewprep.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.subSection.id = :subSectionId ORDER BY CASE WHEN q.displayOrder IS NULL THEN 1 ELSE 0 END ASC, q.displayOrder ASC")
    List<Question> findBySubSectionIdOrderByDisplayOrderAsc(@Param("subSectionId") Long subSectionId);

    @Query("SELECT q FROM Question q JOIN q.subSection ss WHERE ss.mainSection.id = :sectionId ORDER BY ss.displayOrder ASC, q.displayOrder ASC")
    List<Question> findAllByMainSectionId(@Param("sectionId") Long sectionId);
}
