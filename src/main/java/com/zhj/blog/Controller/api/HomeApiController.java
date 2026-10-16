package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Service.BlogService;
import com.zhj.blog.Service.PictureService;
import com.zhj.blog.Pojo.Picture;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.Min;

@RestController
@RequiredArgsConstructor
@Validated
public class HomeApiController {
    private final BlogService blogService;
    private final PictureService pictureService;

    @GetMapping("/api/background")
    public ApiResponse<HomeDto.BackgroundDto> background(@RequestParam(required = false) @Min(1) Long excludeId) {
        Picture picture = pictureService.getRandomBackground(excludeId);
        return ApiResponse.success(picture == null ? null : new HomeDto.BackgroundDto(
                picture.getId().toString(), picture.getPicName(), picture.getPiclink()));
    }

    @GetMapping("/api/home")
    public ApiResponse<HomeDto> home() {
        return ApiResponse.success(blogService.getPublicHome());
    }
}
