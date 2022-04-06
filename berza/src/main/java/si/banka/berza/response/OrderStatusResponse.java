package si.banka.berza.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusResponse {

    //da li je prilikom order-a berza zatvorena ili otvorena
    private boolean berzaOtvorena;
    private String poruka;
}
