package annotated.http.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.common.Cookie;
import com.linecorp.armeria.common.Cookies;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Header;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator;

@LoggingDecorator(
        requestLogLevel = LogLevel.INFO,
        successfulResponseLogLevel = LogLevel.INFO
)
public class InjectionService {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Get("/param/{name}/{id}")
    public HttpResponse param(@Param String name,
                              @Param int id,
                              @Param Gender gender) throws JsonProcessingException {
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8,
                               mapper.writeValueAsBytes(Arrays.asList(name, id, gender)));
    }

    @Get("/header")
    public HttpResponse header(@Header String xArmeriaText,
                               @Header List<Integer> xArmeriaSequence, // ?
                               Cookies cookies) throws JsonProcessingException {
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8,
                               mapper.writeValueAsBytes(Arrays.asList(
                                       xArmeriaText,
                                       xArmeriaSequence,
                                       cookies.stream()
                                              .map(Cookie::name)
                                              .collect(Collectors.toList()))));
    }

    enum Gender {
        MALE, FEMALE
    }
}
