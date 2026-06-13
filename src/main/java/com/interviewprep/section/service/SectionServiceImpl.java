package com.interviewprep.section.service;

import com.interviewprep.common.exception.ResourceNotFoundException;
import com.interviewprep.section.dto.SectionCreateRequest;
import com.interviewprep.section.dto.SectionResponse;
import com.interviewprep.section.dto.SectionUpdateRequest;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.section.mapper.SectionMapper;
import com.interviewprep.section.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository repository;
    private final SectionMapper mapper;

    public SectionServiceImpl(SectionRepository repository, SectionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SectionResponse create(SectionCreateRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    public SectionResponse update(Long id, SectionUpdateRequest request) {
        MainSection entity = getOrThrow(id);
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private MainSection getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainSection", id));
    }
}
