package com.interviewprep.section.service;

import com.interviewprep.section.dto.SectionCreateRequest;
import com.interviewprep.section.dto.SectionResponse;
import com.interviewprep.section.dto.SectionUpdateRequest;

import java.util.List;

public interface SectionService {

    SectionResponse create(SectionCreateRequest request);

    List<SectionResponse> findAll();

    SectionResponse findById(Long id);

    SectionResponse update(Long id, SectionUpdateRequest request);

    void delete(Long id);
}
