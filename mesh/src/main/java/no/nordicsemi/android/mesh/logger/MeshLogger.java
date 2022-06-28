package no.nordicsemi.android.mesh.logger;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

public final class MeshLogger {

    public interface LogHandler {
        /**
         * The log interface is designed to be similar to android.util.Log
         * @param priority The priority/type of this log message.
         *                 Possible values are {@link Log#VERBOSE}, {@link Log#DEBUG}, {@link Log#INFO}, {@link Log#WARN}, {@link Log#ERROR}
         * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs
         * @param message The message that was logged.
         */
        void log(int priority, String tag, String message);
    }
    
    @Nullable
    private static LogHandler logHandler = null;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void verbose(String tag, String message) {
        log(Log.VERBOSE, tag, message, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void debug(String tag, String message) {
        log(Log.DEBUG, tag, message, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void info(String tag, String message) {
        log(Log.INFO, tag, message, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void warn(String tag, String message) {
        log(Log.WARN, tag, message, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void error(String tag, String message) {
        log(Log.ERROR, tag, message, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void error(String tag, String message, Throwable throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    public static void setLogHandler(@Nullable LogHandler logHandler) {
        MeshLogger.logHandler = logHandler;
    }

    private static void log(int priority, String tag, String message, @Nullable Throwable throwable) {
        String fullMessage = message;
        if (throwable != null) {
            fullMessage = fullMessage + "\n" + Log.getStackTraceString(throwable);
        }
        if (logHandler != null) {
            logHandler.log(priority, tag, fullMessage);
        } else {
            Log.println(priority, tag, fullMessage);
        }
    }

}
