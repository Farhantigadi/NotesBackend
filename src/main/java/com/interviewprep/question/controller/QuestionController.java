package com.interviewprep.question.controller;

import com.interviewprep.common.response.ApiResponse;
import com.interviewprep.question.dto.QuestionCreateRequest;
import com.interviewprep.question.dto.QuestionResponse;
import com.interviewprep.question.dto.QuestionUpdateRequest;
import com.interviewprep.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Question management APIs")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new question")
    public ApiResponse<QuestionResponse> create(@Valid @RequestBody QuestionCreateRequest request) {
        return ApiResponse.success("Question created successfully", service.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all questions")
    public ApiResponse<List<QuestionResponse>> findAll() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID")
    public ApiResponse<QuestionResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @GetMapping("/subsection/{subSectionId}")
    @Operation(summary = "Get questions by sub-section ID")
    public ApiResponse<List<QuestionResponse>> findBySubSection(@PathVariable Long subSectionId) {
        return ApiResponse.success(service.findBySubSection(subSectionId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update question")
    public ApiResponse<QuestionResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody QuestionUpdateRequest request) {
        return ApiResponse.success("Question updated successfully", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete question")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image for question")
    public ApiResponse<QuestionResponse> uploadImage(@PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Image uploaded successfully", service.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Delete image for question")
    public ApiResponse<QuestionResponse> deleteImage(@PathVariable Long id) {
        return ApiResponse.success("Image deleted successfully", service.deleteImage(id));
    }
}
