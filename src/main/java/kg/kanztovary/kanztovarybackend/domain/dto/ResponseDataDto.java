package kg.kanztovary.kanztovarybackend.domain.dto;

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