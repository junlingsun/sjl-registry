package com.junling.registry.server.controller;

import com.junling.registry.common.result.R;
import com.junling.registry.server.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
@RequestMapping("/registry")
public class RegistryController {

    @Autowired
    private RegistryService registryService;

    @PostMapping("/register")
    @ResponseBody
    public R register(String data){
        R res = registryService.register(data);
        return res;
    }

    @PostMapping("/remove")
    @ResponseBody
    public R remove(String data) {
        R res = registryService.remove(data);
        return res;
    }

    @PostMapping("/discover")
    @ResponseBody
    public R discover(String data){
        R res = registryService.discover(data);
        return res;
    }

    @PostMapping("/mpnitor")
    @ResponseBody
    public DeferredResult<R> monitor(String data){

        return registryService.monitor(data);

    }


}
