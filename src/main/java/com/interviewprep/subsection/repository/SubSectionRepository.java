package com.interviewprep.subsection.repository;

import com.interviewprep.subsection.entity.SubSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubSectionRepository extends JpaRepository<SubSection, Long> {

    List<SubSection> findByMainSectionId(Long mainSectionId);
}
