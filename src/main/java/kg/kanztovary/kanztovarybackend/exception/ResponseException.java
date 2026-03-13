package kg.kanztovary.kanztovarybackend.exception;

import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseException extends RuntimeException{
    private final ResponseDataDto responseDataDto;

    public ResponseException(int code, String message) {
        super(message);
        this.responseDataDto = new ResponseDataDto(code, message);
    }
}