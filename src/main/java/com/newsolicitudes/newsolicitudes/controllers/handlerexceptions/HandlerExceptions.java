package com.newsolicitudes.newsolicitudes.controllers.handlerexceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.newsolicitudes.newsolicitudes.dto.ErrorResponse;
import com.newsolicitudes.newsolicitudes.exceptions.AprobacionException;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.exceptions.NotFounException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class HandlerExceptions {

  @ExceptionHandler(NotFounException.class)
  public ResponseEntity<Object> handlerNotFoundException(NotFounException e, HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.NOT_FOUND);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

  }

  @ExceptionHandler(FuncionarioException.class)
  public ResponseEntity<Object> handlerFuncionarioException(FuncionarioException e, HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.BAD_REQUEST);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

  }

  @ExceptionHandler(AprobacionException.class)
  public ResponseEntity<Object> handlerAprobacionException(AprobacionException e, HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.NOT_FOUND);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

  }

  @ExceptionHandler(DepartamentoException.class)
  public ResponseEntity<Object> handlerDepartamentoExceptionException(DepartamentoException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.NOT_FOUND);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

  }

  private <T extends Exception> ErrorResponse maptoErrorResponse(T e, HttpServletRequest request, HttpStatus status) {

    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .mensaje(e.getMessage())
        .ruta(request.getRequestURI())
        .build();

  }

}
