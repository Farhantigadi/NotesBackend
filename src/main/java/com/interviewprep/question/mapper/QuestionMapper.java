package com.interviewprep.question.mapper;

import com.interviewprep.question.dto.QuestionResponse;
import com.interviewprep.question.dto.QuestionUpdateRequest;
import com.interviewprep.question.entity.Question;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "subSectionId", source = "subSection.id")
    @Mapping(target = "subSectionTitle", source = "subSection.title")
    QuestionResponse toResponse(Question entity);

    @Mapping(target = "subSection", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(QuestionUpdateRequest request, @MappingTarget Question entity);
}
