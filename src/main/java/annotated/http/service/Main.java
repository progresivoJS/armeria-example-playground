package annotated.http.service;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        final Server server = newServer(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop().join();
            log.info("Server has been stopped.");
        }));

        server.start().join();

        log.info("Server has been started. Serving DocService at http://127.0.0.1:{}/docs",
                 server.activeLocalPort());
    }

    static Server newServer(int port) {
        final ServerBuilder sb = Server.builder();
        return sb.http(port)
                 .annotatedService("/pathPattern", new PathPatternService())
                 .annotatedService("/injection", new InjectionService())
                 .annotatedService("/messageConverter", new MessageConverterService())
                 .annotatedService("/exception", new ExceptionHandlerService())
                 .serviceUnder("/docs",
                               DocService.builder()
                                         .examplePaths(PathPatternService.class,
                                                       "pathsVar",
                                                       "/pathPattern/paths/first/foo",
                                                       "/pathPattern/paths/second/bar")
                                         .examplePaths(PathPatternService.class,
                                                       "pathVar",
                                                       "/pathPattern/path/foo",
                                                       "/pathPattern/path/bar")
                                         .exampleRequests(MessageConverterService.class,
                                                          "json1",
                                                          "{\"name\":\"bar\"}")
                                         .build())
                 .build();
    }
}
