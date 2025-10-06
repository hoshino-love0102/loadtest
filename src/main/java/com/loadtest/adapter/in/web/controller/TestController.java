package com.loadtest.adapter.in.web.controller;

import com.loadtest.adapter.in.web.dto.request.TestDefinitionCreateRequest;
import com.loadtest.adapter.in.web.dto.response.CreateTestResponse;
import com.loadtest.application.port.in.TestUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestUseCase testUseCase;

    public TestController(TestUseCase testUseCase) {
        this.testUseCase = testUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTestResponse create(@Valid @RequestBody TestDefinitionCreateRequest request) {
        UUID testId = testUseCase.create(request.toDomain());
        return new CreateTestResponse(testId);
    }
}
