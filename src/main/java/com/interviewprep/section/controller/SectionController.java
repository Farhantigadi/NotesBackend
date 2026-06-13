package com.interviewprep.section.controller;

import com.interviewprep.common.response.ApiResponse;
import com.interviewprep.section.dto.SectionCreateRequest;
import com.interviewprep.section.dto.SectionResponse;
import com.interviewprep.section.dto.SectionUpdateRequest;
import com.interviewprep.section.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@Tag(name = "Sections", description = "Main Section management APIs")
public class SectionController {

    private final SectionService service;

    public SectionController(SectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new section")
    public ApiResponse<SectionResponse> create(@Valid @RequestBody SectionCreateRequest request) {
        return ApiResponse.success("Section created successfully", service.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all sections")
    public ApiResponse<List<SectionResponse>> findAll() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get section by ID")
    public ApiResponse<SectionResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update section")
    public ApiResponse<SectionResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody SectionUpdateRequest request) {
        return ApiResponse.success("Section updated successfully", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete section")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
