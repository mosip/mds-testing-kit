package io.mosip.mds.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ErrorResponse {
	
	private HttpStatus status;	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss'Z'")
	private LocalDateTime timestamp;
	private String message;
	private String debugMessage;
	
	private ErrorResponse() {
		timestamp = LocalDateTime.now();
	}

	public ErrorResponse(HttpStatus status) {
		this();
		this.status = status;
	}

	public ErrorResponse(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.message = "Unexpected error";
		this.debugMessage = ex.getLocalizedMessage();
	}

	public ErrorResponse(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.message = message;
		this.debugMessage = ex.getLocalizedMessage();
	}
}
