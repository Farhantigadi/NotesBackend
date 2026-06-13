package com.interviewprep.question.service;

import com.interviewprep.question.dto.QuestionCreateRequest;
import com.interviewprep.question.dto.QuestionResponse;
import com.interviewprep.question.dto.QuestionUpdateRequest;

import java.util.List;

public interface QuestionService {

    QuestionResponse create(QuestionCreateRequest request);

    List<QuestionResponse> findAll();

    QuestionResponse findById(Long id);

    List<QuestionResponse> findBySubSection(Long subSectionId);

    QuestionResponse update(Long id, QuestionUpdateRequest request);

    void delete(Long id);
}
