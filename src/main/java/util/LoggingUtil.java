package util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LoggingUtil {
    public static void setupLogger(String logFileName) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        try {
            FileHandler fileHandler = new FileHandler(logFileName, true); // true for append
            fileHandler.setFormatter(new Formatter() {
                private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s: %s%n",
                            sdf.format(new Date(record.getMillis())),
                            record.getLevel(),
                            record.getMessage());
                }
            });

            rootLogger.addHandler(fileHandler);

            // Optional: apply same format to console output
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    handler.setFormatter(fileHandler.getFormatter());
                }
            }

        } catch (IOException e) {
            rootLogger.severe("Failed to setup logger: " + e.getMessage());
        }
    }
}
