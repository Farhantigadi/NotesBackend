package com.interviewprep.section.repository;

import com.interviewprep.auth.User;
import com.interviewprep.section.entity.MainSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<MainSection, Long> {

    List<MainSection> findAllByUserOrderByDisplayOrderAsc(User user);

    Optional<MainSection> findByIdAndUser(Long id, User user);
}
