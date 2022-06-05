package rs.edu.raf.banka.user_service.controller.response_forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangeLimitForm {
    Double limitDelta;
}