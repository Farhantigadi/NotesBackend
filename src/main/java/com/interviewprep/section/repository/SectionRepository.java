package com.interviewprep.section.repository;

import com.interviewprep.section.entity.MainSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<MainSection, Long> {
}
