package com.interviewprep.subsection.mapper;

import com.interviewprep.subsection.dto.SubSectionCreateRequest;
import com.interviewprep.subsection.dto.SubSectionResponse;
import com.interviewprep.subsection.dto.SubSectionUpdateRequest;
import com.interviewprep.subsection.entity.SubSection;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubSectionMapper {

    @Mapping(target = "mainSectionId", source = "mainSection.id")
    @Mapping(target = "mainSectionTitle", source = "mainSection.title")
    SubSectionResponse toResponse(SubSection entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "mainSection", ignore = true)
    void updateEntity(SubSectionUpdateRequest request, @MappingTarget SubSection entity);
}
