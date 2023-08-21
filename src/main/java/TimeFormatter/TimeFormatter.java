package TimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {
    public static String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return now.format(formatter);
    }
}
