package com.interviewprep.auth;

import com.interviewprep.common.response.ApiResponse;
import com.interviewprep.common.util.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Per-user notes")
public class NotesController {

    private final CurrentUserResolver currentUserResolver;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get current user's notes")
    public ApiResponse<NoteResponse> get() {
        User user = currentUserResolver.get();
        return ApiResponse.success("OK", new NoteResponse(user.getNotes()));
    }

    @PutMapping
    @Operation(summary = "Save current user's notes")
    public ApiResponse<NoteResponse> save(@RequestBody NoteRequest request) {
        User user = currentUserResolver.get();
        user.setNotes(request.content());
        userRepository.save(user);
        return ApiResponse.success("Saved", new NoteResponse(user.getNotes()));
    }

    public record NoteRequest(String content) {}
    public record NoteResponse(String content) {}
}
