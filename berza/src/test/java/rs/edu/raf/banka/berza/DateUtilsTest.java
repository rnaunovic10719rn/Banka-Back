package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rs.edu.raf.banka.berza.utils.DateUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {

    @Test
    public void TestIsDateInDecayDays() {
        List<TestCaseIsDateInDecayDays> tcs = new ArrayList<>();
        tcs.add(new TestCaseIsDateInDecayDays(new Date(), 1, false));
        tcs.add(new TestCaseIsDateInDecayDays(Date.from(ZonedDateTime.now().minusDays(2).toInstant()), 1, true));

        for(TestCaseIsDateInDecayDays tc: tcs) {
            boolean result = DateUtils.isDateInDecayDays(tc.date, tc.days);
            assertEquals(result, tc.expected);
        }
    }

    private class TestCaseIsDateInDecayDays {
        public Date date;
        public Integer days;
        public Boolean expected;

        public TestCaseIsDateInDecayDays(Date date, Integer days, Boolean expected) {
            this.date = date;
            this.days = days;
            this.expected = expected;
        }
    }
}
