package com.newsolicitudes.newsolicitudes.controllers.handlerexceptions;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import com.newsolicitudes.newsolicitudes.dto.ErrorResponse;
import com.newsolicitudes.newsolicitudes.exceptions.AprobacionException;
import com.newsolicitudes.newsolicitudes.exceptions.AusenciaException;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.exceptions.DocumentException;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.exceptions.MailServiceException;
import com.newsolicitudes.newsolicitudes.exceptions.NotFoundException;
import com.newsolicitudes.newsolicitudes.exceptions.SolicitudException;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class HandlerExceptions {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handlerNotFoundException(NotFoundException e, HttpServletRequest request) {

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

  @ExceptionHandler(AusenciaException.class)
  public ResponseEntity<Object> handlerAusenciaException(AusenciaException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.CONFLICT);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Object> handlerIOSException(IOException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.CONFLICT);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

  }

  @ExceptionHandler(DocumentException.class)
  public ResponseEntity<Object> handlerDocumentException(DocumentException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.BAD_REQUEST);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

  }

  @ExceptionHandler(MailServiceException.class)
  public ResponseEntity<Object> handlerMailServiceException(MailServiceException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.BAD_REQUEST);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

  }

  @ExceptionHandler(SolicitudException.class)
  public ResponseEntity<Object> handlerSolicitudException(SolicitudException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.BAD_REQUEST);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Object> handeleResourceException(NoResourceFoundException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.NOT_FOUND);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

  }


  @ExceptionHandler(SubroganciaException.class)
  public ResponseEntity<Object> handeleSubroganciaException(SubroganciaException e,
      HttpServletRequest request) {

    ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.CONFLICT);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

  }

  private <T extends Exception> ErrorResponse maptoErrorResponse(T e, HttpServletRequest request, HttpStatus status) {

    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(e.getMessage())
        .ruta(request.getRequestURI())
        .build();

  }

}
