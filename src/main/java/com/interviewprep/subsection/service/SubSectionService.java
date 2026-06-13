package com.interviewprep.subsection.service;

import com.interviewprep.subsection.dto.SubSectionCreateRequest;
import com.interviewprep.subsection.dto.SubSectionResponse;
import com.interviewprep.subsection.dto.SubSectionUpdateRequest;

import java.util.List;

public interface SubSectionService {

    SubSectionResponse create(SubSectionCreateRequest request);

    List<SubSectionResponse> findAll();

    SubSectionResponse findById(Long id);

    List<SubSectionResponse> findBySection(Long sectionId);

    SubSectionResponse update(Long id, SubSectionUpdateRequest request);

    void delete(Long id);
}
