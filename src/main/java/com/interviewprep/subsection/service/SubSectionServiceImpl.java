package com.interviewprep.subsection.service;

import com.interviewprep.auth.User;
import com.interviewprep.common.util.CurrentUserResolver;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.section.repository.SectionRepository;
import com.interviewprep.subsection.dto.SubSectionCreateRequest;
import com.interviewprep.subsection.dto.SubSectionResponse;
import com.interviewprep.subsection.dto.SubSectionUpdateRequest;
import com.interviewprep.subsection.entity.SubSection;
import com.interviewprep.subsection.mapper.SubSectionMapper;
import com.interviewprep.subsection.repository.SubSectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SubSectionServiceImpl implements SubSectionService {

    private final SubSectionRepository repository;
    private final SectionRepository sectionRepository;
    private final SubSectionMapper mapper;
    private final CurrentUserResolver currentUserResolver;

    public SubSectionServiceImpl(SubSectionRepository repository,
                                 SectionRepository sectionRepository,
                                 SubSectionMapper mapper,
                                 CurrentUserResolver currentUserResolver) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
        this.mapper = mapper;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public SubSectionResponse create(SubSectionCreateRequest request) {
        User user = currentUserResolver.get();
        MainSection section = getSectionOrThrow(request.mainSectionId(), user);
        SubSection entity = SubSection.builder()
                .title(request.title())
                .description(request.description())
                .displayOrder(request.displayOrder())
                .mainSection(section)
                .user(user)
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubSectionResponse> findAll() {
        return repository.findAllByUserOrderByDisplayOrderAsc(currentUserResolver.get())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id, currentUserResolver.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubSectionResponse> findBySection(Long sectionId) {
        User user = currentUserResolver.get();
        MainSection section = getSectionOrThrow(sectionId, user);
        return repository.findAllByMainSectionAndUser(section, user)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public SubSectionResponse update(Long id, SubSectionUpdateRequest request) {
        User user = currentUserResolver.get();
        SubSection entity = getOrThrow(id, user);
        mapper.updateEntity(request, entity);
        entity.setMainSection(getSectionOrThrow(request.mainSectionId(), user));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        SubSection entity = getOrThrow(id, currentUserResolver.get());
        repository.delete(entity);
    }

    private SubSection getOrThrow(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubSection not found"));
    }

    private MainSection getSectionOrThrow(Long id, User user) {
        return sectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
    }
}
