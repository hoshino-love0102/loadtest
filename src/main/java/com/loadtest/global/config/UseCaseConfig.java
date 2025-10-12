package com.loadtest.global.config;

import com.loadtest.application.port.in.RunUseCase;
import com.loadtest.application.port.in.TestUseCase;
import com.loadtest.application.port.out.*;
import com.loadtest.application.service.RunService;
import com.loadtest.application.service.TargetValidator;
import com.loadtest.application.service.TestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UseCaseConfig {

    @Bean
    public TestUseCase testUseCase(TestDefinitionRepository testDefinitionRepository) {
        return new TestService(testDefinitionRepository);
    }

    @Bean
    public TargetValidator targetValidator(
            AllowedTargetRepository allowedTargetRepository,
            @Value("${loadtest.security.allowed-ports}") List<Integer> allowedPorts
    ) {
        return new TargetValidator(allowedTargetRepository, allowedPorts);
    }

    @Bean
    public RunUseCase runUseCase(
            TestDefinitionRepository testDefinitionRepository,
            TestRunRepository testRunRepository,
            RunRuntimeStore runtimeStore,
            LoadTestRunner loadTestRunner,
            TestReportRepository reportRepository,
            RunSampleRepository sampleRepository,
            TargetValidator targetValidator
    ) {
        return new RunService(
                testDefinitionRepository,
                testRunRepository,
                runtimeStore,
                loadTestRunner,
                reportRepository,
                sampleRepository,
                targetValidator
        );
    }
}