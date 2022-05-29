package rs.edu.raf.banka.user_service.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import rs.edu.raf.banka.user_service.service.UserService;

@Configuration
@EnableScheduling
public class ResetLimitScheduler {

    @Autowired
    UserService userService;

    @Scheduled(cron = "0 0 0 * * *",zone = "Europe/Paris")
    public void resetAllLimitUsed(){
        userService.resetLimitUsedAllAgents();
        System.out.println("Reset all limits used");
    }
}
