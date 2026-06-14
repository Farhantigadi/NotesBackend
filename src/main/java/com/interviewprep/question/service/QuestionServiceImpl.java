package com.interviewprep.question.service;

import com.interviewprep.common.exception.ResourceNotFoundException;
import com.interviewprep.common.storage.ImageStorageService;
import com.interviewprep.question.dto.QuestionCreateRequest;
import com.interviewprep.question.dto.QuestionResponse;
import com.interviewprep.question.dto.QuestionUpdateRequest;
import com.interviewprep.question.entity.Question;
import com.interviewprep.question.mapper.QuestionMapper;
import com.interviewprep.question.repository.QuestionRepository;
import com.interviewprep.subsection.entity.SubSection;
import com.interviewprep.subsection.repository.SubSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository repository;
    private final SubSectionRepository subSectionRepository;
    private final QuestionMapper mapper;
    private final ImageStorageService imageStorageService;

    public QuestionServiceImpl(QuestionRepository repository,
                               SubSectionRepository subSectionRepository,
                               QuestionMapper mapper,
                               ImageStorageService imageStorageService) {
        this.repository = repository;
        this.subSectionRepository = subSectionRepository;
        this.mapper = mapper;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public QuestionResponse create(QuestionCreateRequest request) {
        SubSection subSection = getSubSectionOrThrow(request.subSectionId());
        Question entity = Question.builder()
                .title(request.title())
                .answer(request.answer())
                .codeSnippet(request.codeSnippet())
                .codeLanguage(request.codeLanguage())
                .explanation(request.explanation())
                .displayOrder(request.displayOrder())
                .imageWidth(request.imageWidth())
                .imageAlign(request.imageAlign())
                .subSection(subSection)
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findBySubSection(Long subSectionId) {
        return repository.findBySubSectionIdOrderByDisplayOrderAsc(subSectionId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public QuestionResponse update(Long id, QuestionUpdateRequest request) {
        Question entity = getOrThrow(id);
        mapper.updateEntity(request, entity);
        entity.setSubSection(getSubSectionOrThrow(request.subSectionId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        Question entity = getOrThrow(id);
        imageStorageService.delete(entity.getImageUrl());
        repository.deleteById(id);
    }

    @Override
    public QuestionResponse uploadImage(Long id, MultipartFile file) {
        Question entity = getOrThrow(id);
        imageStorageService.delete(entity.getImageUrl());
        entity.setImageUrl(imageStorageService.store(file));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public QuestionResponse deleteImage(Long id) {
        Question entity = getOrThrow(id);
        imageStorageService.delete(entity.getImageUrl());
        entity.setImageUrl(null);
        return mapper.toResponse(repository.save(entity));
    }

    private Question getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
    }

    private SubSection getSubSectionOrThrow(Long id) {
        return subSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubSection", id));
    }
}
