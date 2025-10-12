package com.loadtest.adapter.out.persistence;

import com.loadtest.application.port.out.AllowedTargetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryAllowedTargetRepository implements AllowedTargetRepository {

    private final Set<String> allowedHosts;

    public InMemoryAllowedTargetRepository(
            @Value("${loadtest.security.allowed-hosts}") List<String> hosts
    ) {
        if (hosts == null || hosts.isEmpty()) {
            this.allowedHosts = Collections.emptySet();
            return;
        }

        Set<String> normalized = new HashSet<>();
        for (String host : hosts) {
            if (host != null && !host.isBlank()) {
                normalized.add(host.trim().toLowerCase(Locale.ROOT));
            }
        }

        this.allowedHosts = Collections.unmodifiableSet(normalized);
    }

    @Override
    public Set<String> getAllowedHosts() {
        return allowedHosts;
    }
}
