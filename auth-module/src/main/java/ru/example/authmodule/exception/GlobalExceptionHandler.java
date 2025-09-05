package ru.example.authmodule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public Map<String, String> entityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return response;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IncorrectDataException.class)
    public Map<String, String> incorrectDataException(IncorrectDataException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return response;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ErrorMessageGlobal.class)
    public Map<String, String> errorMessageGlobal(ErrorMessageGlobal ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return response;
    }

//    @ResponseBody
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(CustomNotFoundException.class)
//    public Map<String, String> entityNotFoundException(CustomNotFoundException ex) {
//        Map<String, String> response = new HashMap<>();
//        response.put("error", ex.getMessage());
//
//        return response;
//    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public Map<String, String> entityAlreadyExistsException(EntityAlreadyExistsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return response;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getFieldErrors().forEach(error ->
                response.put(error.getField(), error.getDefaultMessage()));

        return response;
    }


}
