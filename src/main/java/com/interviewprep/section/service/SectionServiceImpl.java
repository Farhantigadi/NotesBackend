package com.interviewprep.section.service;

import com.interviewprep.auth.User;
import com.interviewprep.common.util.CurrentUserResolver;
import com.interviewprep.section.dto.SectionCreateRequest;
import com.interviewprep.section.dto.SectionResponse;
import com.interviewprep.section.dto.SectionUpdateRequest;
import com.interviewprep.section.entity.MainSection;
import com.interviewprep.section.mapper.SectionMapper;
import com.interviewprep.section.repository.SectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository repository;
    private final SectionMapper mapper;
    private final CurrentUserResolver currentUserResolver;

    public SectionServiceImpl(SectionRepository repository, SectionMapper mapper,
                              CurrentUserResolver currentUserResolver) {
        this.repository = repository;
        this.mapper = mapper;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public SectionResponse create(SectionCreateRequest request) {
        MainSection entity = mapper.toEntity(request);
        entity.setUser(currentUserResolver.get());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> findAll() {
        return repository.findAllByUserOrderByDisplayOrderAsc(currentUserResolver.get())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id, currentUserResolver.get()));
    }

    @Override
    public SectionResponse update(Long id, SectionUpdateRequest request) {
        MainSection entity = getOrThrow(id, currentUserResolver.get());
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        MainSection entity = getOrThrow(id, currentUserResolver.get());
        repository.delete(entity);
    }

    private MainSection getOrThrow(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
    }
}
