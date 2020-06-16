package ai.deepcode.javaclient.core;

import java.text.SimpleDateFormat;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DCLoggerBase {

  private final Supplier<Consumer<String>> infoFunctionSupplier;
  private final Supplier<Consumer<String>> warnFunctionSupplier;
  private final Supplier<Boolean> isInfoEnabledSupplier;
  private final Supplier<Boolean> isWarnEnabledSupplier;

  protected DCLoggerBase(
      Supplier<Consumer<String>> infoFunctionSupplier,
      Supplier<Consumer<String>> warnFunctionSupplier,
      Supplier<Boolean> isInfoEnabledSupplier,
      Supplier<Boolean> isWarnEnabledSupplier) {
    this.infoFunctionSupplier = infoFunctionSupplier;
    this.warnFunctionSupplier = warnFunctionSupplier;
    this.isInfoEnabledSupplier = isInfoEnabledSupplier;
    this.isWarnEnabledSupplier = isWarnEnabledSupplier;
  }

  protected static final SimpleDateFormat HMSS = new SimpleDateFormat("H:mm:ss,S");
  protected static final SimpleDateFormat mmssSSS = new SimpleDateFormat("mm:ss,SSS");

  public void logInfo(String message) {
    if (!isInfoEnabledSupplier.get()) return;
    doLogging(message, infoFunctionSupplier.get());
  }

  public void logWarn(String message) {
    if (!isWarnEnabledSupplier.get()) return;
    doLogging(message, warnFunctionSupplier.get());
  }

  private synchronized void doLogging(String message, Consumer<String> logFunction) {
    //    String currentTime = "[" + HMSS.format(System.currentTimeMillis()) + "] ";
    String currentTime = "[" + mmssSSS.format(System.currentTimeMillis()) + "] ";
    if (message.length() > 500) {
      message =
          message.substring(0, 500)
              + " ... ["
              + (message.length() - 500)
              + " more symbols were cut]";
    }

    String currentThread = " [" + Thread.currentThread().getName() + "] ";

    final String[] lines = message.split("[\n\r]");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      line = (i == 0 ? currentTime : "            ") + line;
      if (i == lines.length - 1) {
        line += "\n" + getMyClassesStacktrace();
        line += "\n" + getExtraInfo();
      }

      logFunction.accept(line);
    }
  }

  private static String getMyClassesStacktrace() {
    StringJoiner joiner = new StringJoiner(" -> ");
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (int i = stackTrace.length - 1; i > 2; i--) {
      StackTraceElement ste = stackTrace[i];
      if (ste.getClassName().contains("ai.deepcode")) {
        String s =
            ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1)
                + "."
                + ste.getMethodName()
                + ":"
                + ste.getLineNumber();
        joiner.add(s);
      }
    }
    return joiner.toString();
  }

  protected abstract String getExtraInfo();

}
