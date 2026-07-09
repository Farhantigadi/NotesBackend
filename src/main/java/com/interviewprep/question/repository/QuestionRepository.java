package com.interviewprep.question.repository;

import com.interviewprep.auth.User;
import com.interviewprep.question.entity.Question;
import com.interviewprep.subsection.entity.SubSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndUser(Long id, User user);

    List<Question> findAllBySubSectionAndUserOrderByDisplayOrderAsc(SubSection subSection, User user);

    @Query("SELECT q FROM Question q JOIN q.subSection ss WHERE ss.mainSection.id = :sectionId AND q.user = :user ORDER BY ss.displayOrder ASC, q.displayOrder ASC")
    List<Question> findAllByMainSectionIdAndUser(@Param("sectionId") Long sectionId, @Param("user") User user);
}
