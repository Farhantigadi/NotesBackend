package com.interviewprep.subsection.repository;

import com.interviewprep.auth.User;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.subsection.entity.SubSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubSectionRepository extends JpaRepository<SubSection, Long> {

    List<SubSection> findAllByUserOrderByDisplayOrderAsc(User user);

    Optional<SubSection> findByIdAndUser(Long id, User user);

    List<SubSection> findAllByMainSectionAndUser(MainSection mainSection, User user);
}
