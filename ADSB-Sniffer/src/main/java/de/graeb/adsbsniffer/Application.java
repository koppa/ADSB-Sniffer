package de.graeb.adsbsniffer;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Overwrites the Application class.
 * <p/>
 * Overwrites the default ExceptionHandler
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // log all uncaught exceptions and write to logfile
        final Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                String format = String.format("ADSBSniffer_Crashlog_%s.txt", new Date().toString());
                File file = new File(Environment.getExternalStorageDirectory(), format);
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(String.format("Thread:\n%s\n\n", thread.toString()));
                    fileWriter.write(String.format("Throwable:\n%s\n\n", throwable.toString()));
                    fileWriter.write(String.format("Stacktrace:\n"));
                    throwable.printStackTrace(new PrintWriter(fileWriter));
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                originalUncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        });

        new Recorder(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        Recorder.getInstance().stopRecording();
        super.onTerminate();
    }
}
