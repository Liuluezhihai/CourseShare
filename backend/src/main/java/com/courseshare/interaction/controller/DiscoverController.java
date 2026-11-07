package com.courseshare.interaction.controller;

import com.courseshare.common.api.ApiResponse;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.service.DiscoverService;
import com.courseshare.resource.vo.DiscoverResourceVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discover")
public class DiscoverController {

    private final DiscoverService discoverService;

    public DiscoverController(DiscoverService discoverService) {
        this.discoverService = discoverService;
    }

    @GetMapping("/resources")
    public ApiResponse<PageResult<DiscoverResourceVO>> page(
            @RequestParam(required = false, defaultValue = "popular") String sort,
            @ModelAttribute PageQuery query) {
        return ApiResponse.success(discoverService.page(sort, query));
    }
}
