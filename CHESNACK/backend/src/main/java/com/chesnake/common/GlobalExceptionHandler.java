package com.chesnake.common;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数校验失败" : fe.getDefaultMessage();
        return R.fail(msg);
    }

    /** Sentinel 限流/熔断兜底（未走 blockHandler 的场景） */
    @ExceptionHandler(BlockException.class)
    public R<Void> handleBlock(BlockException e) {
        log.warn("[Sentinel] 触发限流/熔断: {}", e.getMessage());
        return R.fail(429, "系统繁忙，请稍后再试");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统繁忙，请稍后重试");
    }
}
