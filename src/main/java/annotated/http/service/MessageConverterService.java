package annotated.http.service;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.ProducesText;
import com.linecorp.armeria.server.annotation.RequestConverter;
import com.linecorp.armeria.server.annotation.RequestConverterFunction;
import com.linecorp.armeria.server.annotation.RequestObject;
import com.linecorp.armeria.server.annotation.ResponseConverter;
import com.linecorp.armeria.server.annotation.ResponseConverterFunction;
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@LoggingDecorator(
        requestLogLevel = LogLevel.INFO,
        successfulResponseLogLevel = LogLevel.INFO
)
public class MessageConverterService {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Post("/node/node")
    public JsonNode json1(@RequestObject JsonNode input) {
        final JsonNode name = input.get("name");
        return mapper.valueToTree(new Response(Response.SUCCESS, name.textValue()));
    }

    @Post("/node/obj")
    @ProducesJson
    public Response json2(@RequestObject JsonNode input) {
        final JsonNode name = input.get("name");
        return new Response(Response.SUCCESS, name.textValue());
    }

    @Post("/obj/obj")
    @ProducesJson
    public Response json3(@RequestObject Request request) {
        return new Response(Response.SUCCESS, request.name());
    }

    @Post("/obj/future")
    @ProducesJson
    public CompletionStage<Response> json4(@RequestObject Request request,
                                           ServiceRequestContext ctx) {
        final CompletableFuture<Response> future = new CompletableFuture<>();
        ctx.blockingTaskExecutor()
           .submit(() -> future.complete(new Response(Response.SUCCESS, request.name)));
        return future;
    }

    @Post("/custom")
    @ProducesText
    @RequestConverter(CustomerRequestConverter.class)
    @ResponseConverter(CustomResponseConverter.class)
    public Response custom(@RequestObject Request request) {
        return new Response(Response.SUCCESS, request.name);
    }

    public static final class Request {
        private final String name;

        @JsonCreator
        public Request(@JsonProperty("name") String name) {
            this.name = name;
        }

        @JsonProperty
        public String name() {
            return name;
        }
    }

    public static final class Response {
        static final String SUCCESS = "success";

        private final String result;
        private final String from;

        private Response(String result, String from) {
            this.result = result;
            this.from = from;
        }

        @JsonProperty
        public String result() {
            return result;
        }

        @JsonProperty
        public String from() {
            return from;
        }
    }

    public static final class CustomerRequestConverter implements RequestConverterFunction {

        @Override
        public Object convertRequest(ServiceRequestContext ctx, AggregatedHttpRequest request,
                                     Class<?> expectedResultType,
                                     @Nullable ParameterizedType expectedParameterizedResultType)
                throws Exception {
            final MediaType mediaType = request.contentType();
            if (mediaType != null && mediaType.is(MediaType.PLAIN_TEXT_UTF_8)) {
                return new Request(request.contentUtf8());
            }
            return RequestConverterFunction.fallthrough();
        }
    }

    public static final class CustomResponseConverter implements ResponseConverterFunction {
        @Override
        public HttpResponse convertResponse(ServiceRequestContext ctx, ResponseHeaders headers,
                                            @Nullable Object result,
                                            HttpHeaders trailers) throws Exception {
            if (result instanceof Response) {
                final Response response = (Response) result;
                final HttpData body = HttpData.ofUtf8(response.result + ':' + response.from());
                return HttpResponse.of(headers, body, trailers);
            }
            return ResponseConverterFunction.fallthrough();
        }
    }
}
