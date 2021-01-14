package annotated.http.service;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.server.HttpStatusException;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator;

import annotated.http.service.ExceptionHandlerService.GlobalExceptionHandler;

@LoggingDecorator(
        requestLogLevel = LogLevel.INFO,
        successfulResponseLogLevel = LogLevel.INFO
)
@ExceptionHandler(GlobalExceptionHandler.class)
public class ExceptionHandlerService {

    @Get("/locallySpecific")
    @ExceptionHandler(LocalExceptionHandler.class)
    public String exception1() {
        throw new LocallySpecificException();
    }

    @Get("/locallyGeneral")
    @ExceptionHandler(LocalExceptionHandler.class)
    public String exception2() {
        throw new LocallyGeneralException();
    }

    @Get("/globallyGeneral")
    @ExceptionHandler(LocalExceptionHandler.class)
    public String exception3() {
        throw new GloballyGeneralException();
    }

    @Get("/default")
    @ExceptionHandler(LocalExceptionHandler.class)
    public String exception4() {
        throw new IllegalArgumentException();
    }

    @Get("/default/{status}")
    @ExceptionHandler(LocalExceptionHandler.class)
    public String exception5(@Param int status) {
        throw HttpStatusException.of(status);
    }

    static class GloballyGeneralException extends RuntimeException {
        private static final long serialVersionUID = 8210080483318166316L;
    }

    static class LocallyGeneralException extends GloballyGeneralException {
        private static final long serialVersionUID = -9167203216151475846L;
    }

    static class LocallySpecificException extends LocallyGeneralException {
        private static final long serialVersionUID = 5879797158322612975L;
    }

    static final class GlobalExceptionHandler implements ExceptionHandlerFunction {
        @Override
        public HttpResponse handleException(ServiceRequestContext ctx, HttpRequest req, Throwable cause) {
            if (cause instanceof GloballyGeneralException) {
                return HttpResponse.of(HttpStatus.FORBIDDEN);
            }

            // To the next exception handler
            return ExceptionHandlerFunction.fallthrough();
        }
    }

    static final class LocalExceptionHandler implements ExceptionHandlerFunction {

        @Override
        public HttpResponse handleException(ServiceRequestContext ctx, HttpRequest req, Throwable cause) {
            if (cause instanceof LocallySpecificException) {
                return HttpResponse.of(HttpStatus.SERVICE_UNAVAILABLE);
            }

            if (cause instanceof LocallyGeneralException) {
                return HttpResponse.of(HttpStatus.BAD_REQUEST);
            }

            return ExceptionHandlerFunction.fallthrough();
        }
    }
}
