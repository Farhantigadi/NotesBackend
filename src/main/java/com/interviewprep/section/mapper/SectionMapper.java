package com.interviewprep.section.mapper;

import com.interviewprep.section.dto.SectionCreateRequest;
import com.interviewprep.section.dto.SectionResponse;
import com.interviewprep.section.dto.SectionUpdateRequest;
import com.interviewprep.section.entity.MainSection;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    MainSection toEntity(SectionCreateRequest request);

    SectionResponse toResponse(MainSection entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SectionUpdateRequest request, @MappingTarget MainSection entity);
}
