package io.mosip.mds.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.mosip.mds.dto.ErrorResponse;


@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

		@Override
		protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, 
				   HttpHeaders headers, HttpStatus status, WebRequest request) {
			String error = "Malformed/Invalid JSON request";
			return buildResponseEntity(new ErrorResponse(HttpStatus.BAD_REQUEST, error, ex));
		}

		private ResponseEntity<Object> buildResponseEntity(ErrorResponse errorResponse) {
			return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
		}
		
		@ExceptionHandler(Throwable.class)
		public ResponseEntity<Object> handleError(Throwable t) {
			String error = "Requested Operation Failed";
			return buildResponseEntity(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, error, t));
		}

}
