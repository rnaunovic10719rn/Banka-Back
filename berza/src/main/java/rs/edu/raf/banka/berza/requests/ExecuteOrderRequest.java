package rs.edu.raf.banka.berza.requests;

import lombok.Data;

@Data
public class ExecuteOrderRequest {

    Long berzaId;
    Long orderId;

}
