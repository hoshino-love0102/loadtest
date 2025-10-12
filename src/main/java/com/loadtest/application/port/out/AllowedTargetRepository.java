package com.loadtest.application.port.out;

import java.util.Set;

public interface AllowedTargetRepository {
    Set<String> getAllowedHosts();
}
