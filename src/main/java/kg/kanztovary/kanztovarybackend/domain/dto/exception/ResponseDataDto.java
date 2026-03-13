package kg.kanztovary.kanztovarybackend.domain.dto.exception;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDataDto {
    private int code;
    private String message;
}