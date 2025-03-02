package br.com.order.exceptions;

import br.com.order.application.base.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Operation(hidden = true)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {

        var error = getFieldErrors(ex.getBindingResult(), ((ServletWebRequest) request).getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Operation(hidden = true)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    public ErrorDTO handleNotFoundException(HttpServletRequest req, NotFoundException ex) {
        return getFieldError(ex, req.getRequestURI());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UniqueConstraintViolationException.class)
    public ErrorDTO handleUniqueConstraintViolationException(HttpServletRequest req, UniqueConstraintViolationException ex) {
        return getFieldError(ex, req.getRequestURI());
    }

    @Operation(hidden = true)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    public ErrorDTO handleBadRequest(HttpServletRequest req, BadRequestException ex) {
        return getFieldError(ex, req.getRequestURI());
    }

    private ErrorDTO getFieldErrors(BindingResult bindingResult, String requestURI) {
        var messages = bindingResult.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        log.error("Field errors: {} {}", messages, requestURI);
        return new ErrorDTO(requestURI, messages);
    }

    private ErrorDTO getFieldError(RuntimeException ex, String requestURI) {
        log.error("Field error: {} {}", ex.getMessage(), requestURI);
        return new ErrorDTO(requestURI, List.of(ex.getMessage()));
    }

    @Operation(hidden = true)
    @ResponseBody
    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handlePessimisticLockException(HttpServletRequest req, PessimisticLockingFailureException ex) {
        return new ErrorDTO(req.getRequestURI(), List.of("O registro está sendo atualizado por outro processo. Tente novamente mais tarde."));
    }
}
