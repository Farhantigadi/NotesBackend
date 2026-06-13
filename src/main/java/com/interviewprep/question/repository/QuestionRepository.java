package com.interviewprep.question.repository;

import com.interviewprep.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubSectionIdOrderByDisplayOrderAsc(Long subSectionId);
}
