package com.interviewprep.subsection.service;

import com.interviewprep.common.exception.ResourceNotFoundException;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.section.repository.SectionRepository;
import com.interviewprep.subsection.dto.SubSectionCreateRequest;
import com.interviewprep.subsection.dto.SubSectionResponse;
import com.interviewprep.subsection.dto.SubSectionUpdateRequest;
import com.interviewprep.subsection.entity.SubSection;
import com.interviewprep.subsection.mapper.SubSectionMapper;
import com.interviewprep.subsection.repository.SubSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubSectionServiceImpl implements SubSectionService {

    private final SubSectionRepository repository;
    private final SectionRepository sectionRepository;
    private final SubSectionMapper mapper;

    public SubSectionServiceImpl(SubSectionRepository repository,
                                 SectionRepository sectionRepository,
                                 SubSectionMapper mapper) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
        this.mapper = mapper;
    }

    @Override
    public SubSectionResponse create(SubSectionCreateRequest request) {
        MainSection section = getMainSectionOrThrow(request.mainSectionId());
        SubSection entity = SubSection.builder()
                .title(request.title())
                .description(request.description())
                .mainSection(section)
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubSectionResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubSectionResponse> findBySection(Long sectionId) {
        return repository.findByMainSectionId(sectionId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public SubSectionResponse update(Long id, SubSectionUpdateRequest request) {
        SubSection entity = getOrThrow(id);
        mapper.updateEntity(request, entity);
        entity.setMainSection(getMainSectionOrThrow(request.mainSectionId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private SubSection getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubSection", id));
    }

    private MainSection getMainSectionOrThrow(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainSection", id));
    }
}
