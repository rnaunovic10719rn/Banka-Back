package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private long id;
    private String username;

    private Double limit;
    private Double limitUsed;
    private boolean needsSupervisorPermission;

    private String roleName;
}
