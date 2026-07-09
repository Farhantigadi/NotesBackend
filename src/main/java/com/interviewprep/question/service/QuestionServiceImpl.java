package com.interviewprep.question.service;

import com.interviewprep.auth.User;
import com.interviewprep.common.storage.ImageStorageService;
import com.interviewprep.common.util.CurrentUserResolver;
import com.interviewprep.question.dto.QuestionCreateRequest;
import com.interviewprep.question.dto.QuestionResponse;
import com.interviewprep.question.dto.QuestionUpdateRequest;
import com.interviewprep.question.entity.Question;
import com.interviewprep.question.mapper.QuestionMapper;
import com.interviewprep.question.repository.QuestionRepository;
import com.interviewprep.subsection.entity.SubSection;
import com.interviewprep.subsection.repository.SubSectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository repository;
    private final SubSectionRepository subSectionRepository;
    private final QuestionMapper mapper;
    private final ImageStorageService imageStorageService;
    private final CurrentUserResolver currentUserResolver;

    public QuestionServiceImpl(QuestionRepository repository,
                               SubSectionRepository subSectionRepository,
                               QuestionMapper mapper,
                               ImageStorageService imageStorageService,
                               CurrentUserResolver currentUserResolver) {
        this.repository = repository;
        this.subSectionRepository = subSectionRepository;
        this.mapper = mapper;
        this.imageStorageService = imageStorageService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public QuestionResponse create(QuestionCreateRequest request) {
        User user = currentUserResolver.get();
        SubSection subSection = getSubSectionOrThrow(request.subSectionId(), user);
        Question entity = Question.builder()
                .title(request.title())
                .answer(request.answer())
                .codeSnippet(request.codeSnippet())
                .codeLanguage(request.codeLanguage())
                .codeBlocks(request.codeBlocks())
                .explanation(request.explanation())
                .displayOrder(request.displayOrder())
                .imageUrl(request.imageUrl())
                .imageWidth(request.imageWidth())
                .imageAlign(request.imageAlign())
                .subSection(subSection)
                .user(user)
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findAll() {
        throw new UnsupportedOperationException("Use findBySubSection instead");
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id, currentUserResolver.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findBySubSection(Long subSectionId) {
        User user = currentUserResolver.get();
        SubSection subSection = getSubSectionOrThrow(subSectionId, user);
        return repository.findAllBySubSectionAndUserOrderByDisplayOrderAsc(subSection, user)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public QuestionResponse update(Long id, QuestionUpdateRequest request) {
        User user = currentUserResolver.get();
        Question entity = getOrThrow(id, user);
        mapper.updateEntity(request, entity);
        entity.setSubSection(getSubSectionOrThrow(request.subSectionId(), user));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        Question entity = getOrThrow(id, currentUserResolver.get());
        imageStorageService.delete(entity.getImageUrl());
        repository.delete(entity);
    }

    @Override
    public QuestionResponse uploadImage(Long id, MultipartFile file) {
        Question entity = getOrThrow(id, currentUserResolver.get());
        imageStorageService.delete(entity.getImageUrl());
        entity.setImageUrl(imageStorageService.store(file));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public QuestionResponse deleteImage(Long id) {
        Question entity = getOrThrow(id, currentUserResolver.get());
        imageStorageService.delete(entity.getImageUrl());
        entity.setImageUrl(null);
        return mapper.toResponse(repository.save(entity));
    }

    private Question getOrThrow(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private SubSection getSubSectionOrThrow(Long id, User user) {
        return subSectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubSection not found"));
    }
}
