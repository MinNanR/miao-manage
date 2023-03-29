package site.minnan.miao.infrastructure.exception;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import site.minnan.miao.userinterface.response.ResponseCode;
import site.minnan.miao.userinterface.response.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    /**
     * 参数非法或缺失时的异常
     *
     * @param ex 异常
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(StrUtil.format("Parameter Error,execute in : {},target : {}", ex.getParameter().getExecutable(),
                ex.getBindingResult().getTarget()), ex);
        List<Map<Object, Object>> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> (FieldError) error)
                .map(error -> MapBuilder.create().put("field", error.getField()).put("message",
                        error.getDefaultMessage()).build())
                .collect(Collectors.toList());
        return ResponseEntity.fail(ResponseCode.INVALID_PARAM, MapBuilder.create().put("details", details).build());
    }

    /**
     * 实体已存在异常（唯一约束不通过）
     *
     * @param ex 异常
     * @return
     */
    @ExceptionHandler(EntityAlreadyExistException.class)
    @ResponseBody
    public ResponseEntity<?> handleEntityAlreadyExistException(EntityAlreadyExistException ex,
                                                               HandlerMethod method) {
        log.error("", ex);
        return ResponseEntity.fail(ex.getMessage());
    }

    /**
     * 处理实体不存在异常，通常发生在查询详情或更新实体时
     *
     * @param ex 异常
     * @return
     */
    @ExceptionHandler(EntityNotExistException.class)
    @ResponseBody
    public ResponseEntity<?> handleEntityNotExistException(EntityNotExistException ex, HandlerMethod method) {
        log.error("", ex);
        return ResponseEntity.fail(ex.getMessage());
    }

    @ExceptionHandler(UnmodifiableException.class)
    @ResponseBody
    public ResponseEntity<?> handleUnmodifiableException(UnmodifiableException ex) {
        log.error("实体不可修改", ex);
        return ResponseEntity.fail(ResponseCode.FAIL, ex.getMessage());
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseBody
    public ResponseEntity<?> handleInvalidOperationException(UnmodifiableException ex) {
        log.error("非法操作用户", ex);
        return ResponseEntity.invalid(ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<?> handleUnknownException(Exception ex) {
        log.error("unknown error", ex);
        return ResponseEntity.fail(ResponseCode.UNKNOWN_ERROR);
    }
}
