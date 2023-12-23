package hu.ellipsysintegrations.adapter.allure.server.exception;

public class AllureServerAdapterException extends RuntimeException {

    public AllureServerAdapterException(String message) {
        super(message);
    }

    public AllureServerAdapterException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
