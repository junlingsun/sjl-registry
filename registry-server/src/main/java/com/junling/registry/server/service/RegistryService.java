package com.junling.registry.server.service;

import com.junling.registry.common.result.R;
import org.springframework.web.context.request.async.DeferredResult;

public interface RegistryService {
    R register(String data);

    R remove(String data);

    R discover(String data);

    DeferredResult<R> monitor(String data);
}
