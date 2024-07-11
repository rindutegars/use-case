package com.tujuhsembilan.example.configuration;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import com.tujuhsembilan.example.exception.MultipleException;

import jakarta.persistence.EntityNotFoundException;
import lib.i18n.utility.MessageUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseEntityExceptionConfig extends ResponseEntityExceptionHandler {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ErrorInputParameters {
    private Exception exception;
    private Object body;
    private HttpHeaders headers;
    private HttpStatus status;
    private WebRequest request;

    protected ErrorInputParameters copy() {
      return ErrorInputParameters.builder()
          .exception(this.exception)
          .body(this.body)
          .headers(this.headers)
          .status(this.status)
          .request(this.request)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ErrorOutputParameters {
    private HttpStatus status;
    private HttpHeaders headers;
    private Object body;
  }

  @Data
  private static class ErrorDefinition {
    protected static final String PROP_PREFIX = "application.error.";
    protected static final String PROP_CODE = ".code";
    protected static final String PROP_TITLE = ".title";
    protected static final String PROP_DETAIL = ".detail";

    protected ErrorDefinition(MessageUtil msg, String errorType, Object... args) {
      this.code = msg.get(PROP_PREFIX + errorType + PROP_CODE);
      this.title = msg.get(PROP_PREFIX + errorType + PROP_TITLE);
      this.detail = msg.get(PROP_PREFIX + errorType + PROP_DETAIL, args);
    }

    private String code;
    private String title;
    private String detail;
  }

  private static String getStatusString(HttpStatus status) {
    return status.series().name() + ":" + status.value() + ":" + status.name();
  }

  // ---

  private final MessageUtil msg;

  private JsonApiError getJsonApiError(ErrorInputParameters param, String errorType, Object... args) {
    var err = new ErrorDefinition(msg, errorType, args);
    return JsonApiError.create()
        // HTTP Status
        .withStatus(getStatusString(param.getStatus()))
        // Application Specific Code
        .withCode(err.getCode())
        // Simple Human-Readable Summary of the Problem
        .withTitle(err.getTitle())
        // Human-Readable Explanation Specific to the Occurrence of the Problem
        .withDetail(err.getDetail());
  }

  private void populateErrorResponse(JsonApiErrors body, ErrorInputParameters param, boolean enableSystemError) {
    final Exception x = param.getException();
    JsonApiError error = null;

    if (x instanceof MultipleException) {
      param.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

      error = getJsonApiError(param, "multiple-error");
    }

    // ---

    else if (x instanceof MethodArgumentNotValidException) {
      param.setStatus(HttpStatus.BAD_REQUEST);

      var fieldErrors = new HashMap<String, String>();
      var manve = (MethodArgumentNotValidException) x;
      manve.getFieldErrors().forEach(oe -> fieldErrors.put(oe.getField(), oe.getDefaultMessage()));

      error = getJsonApiError(param, "invalid-arguments").withMeta(Map.of("errorSources", fieldErrors));
    }

    else if (x instanceof EntityNotFoundException) {
      param.setStatus(HttpStatus.NOT_FOUND);

      error = getJsonApiError(param, "entity-not-found");
    }

    else if (x instanceof HttpRequestMethodNotSupportedException) {
      param.setStatus(HttpStatus.METHOD_NOT_ALLOWED);

      error = getJsonApiError(param, "method-not-allowed");
    }

    else if (enableSystemError) {
      param.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

      error = JsonApiError.create()
          .withCode(x.getClass().getCanonicalName());

      log.debug("Unhandled Error Type: {}", x.getClass());
      if (log.isDebugEnabled()) {
        x.printStackTrace();
      }
    }

    // ---

    if (error != null) {
      if (x.getMessage() != null) {
        var meta = new HashMap<String, Object>(Optional.ofNullable(error.getMeta()).orElse(Collections.emptyMap()));
        meta.put("errorMessage", x.getMessage());
        error = error.withMeta(meta);
      }

      body.withError(error);
    }
  }

  private void populateErrorResponse(JsonApiErrors body, ErrorInputParameters param) {
    this.populateErrorResponse(body, param, false);
  }

  private ErrorOutputParameters constructErrorResponse(ErrorInputParameters param) {
    final JsonApiErrors body = JsonApiErrors.create();

    // #region Exception Mappings
    final Exception x = param.getException();
    if (x instanceof MultipleException) {
      populateErrorResponse(body, param);

      var me = (MultipleException) x;
      me.getExceptions().forEach(e -> {
        var inParam = param.copy();
        inParam.setException(e);
        populateErrorResponse(body, inParam, true);
      });

      param.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    } else {
      populateErrorResponse(body, param);
    }
    // #endregion

    if (body.getErrors().isEmpty()) {
      log.debug("Unhandled Error Type: {}", x.getClass());
      if (log.isDebugEnabled()) {
        x.printStackTrace();
      }

      param.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

      body.withError(getJsonApiError(param, "generic"));
    }

    return ErrorOutputParameters.builder()
        .headers(param.getHeaders())
        .status(param.getStatus())
        .body(body)
        .build();
  }

  // ---

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {
    var out = constructErrorResponse(ErrorInputParameters.builder()
        .exception(ex)
        .body(body)
        .headers(headers)
        .status((HttpStatus) status)
        .request(request)
        .build());

    return ResponseEntity
        .status(out.status)
        .headers(out.headers)
        .body(out.body);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleAnyOtherException(Exception ex, WebRequest request) {
    return handleExceptionInternal(ex, null, null, null, request);
  }

}
