package com.zhihuixueyuan.content.feignClient;

import com.zhihuixueyuan.base.model.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("熔断了，走了降级逻辑，索引信息：{}，熔断异常信息：{}",courseIndex,throwable.toString(),throwable);
                return false;
            }
        };
    }
}
