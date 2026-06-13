package com.interviewprep.subsection.controller;

import com.interviewprep.common.response.ApiResponse;
import com.interviewprep.subsection.dto.SubSectionCreateRequest;
import com.interviewprep.subsection.dto.SubSectionResponse;
import com.interviewprep.subsection.dto.SubSectionUpdateRequest;
import com.interviewprep.subsection.service.SubSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subsections")
@Tag(name = "SubSections", description = "Sub Section management APIs")
public class SubSectionController {

    private final SubSectionService service;

    public SubSectionController(SubSectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new sub-section")
    public ApiResponse<SubSectionResponse> create(@Valid @RequestBody SubSectionCreateRequest request) {
        return ApiResponse.success("SubSection created successfully", service.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all sub-sections")
    public ApiResponse<List<SubSectionResponse>> findAll() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sub-section by ID")
    public ApiResponse<SubSectionResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get sub-sections by section ID")
    public ApiResponse<List<SubSectionResponse>> findBySection(@PathVariable Long sectionId) {
        return ApiResponse.success(service.findBySection(sectionId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sub-section")
    public ApiResponse<SubSectionResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody SubSectionUpdateRequest request) {
        return ApiResponse.success("SubSection updated successfully", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete sub-section")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
