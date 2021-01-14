package annotated.http.service;

import com.linecorp.armeria.common.logging.LogLevel;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Path;
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator;

@LoggingDecorator(
        requestLogLevel = LogLevel.INFO,
        successfulResponseLogLevel = LogLevel.INFO
)
public class PathPatternService {
    /**
     *Accesses the parameter with the name of the path variable.
     */
    @Get("/path/{name}")
    public String pathVar(@Param String name) {
        return "path: " + name;
    }

    /**
     *Accesses the parameter with the name of the capturing group.
     */
    @Get("regex:^/regex/(?<name>.*)$")
    public String regex(@Param String name) {
        return "regex: " + name;
    }

    /**
     * Access the parameter with the index number.`
     */
    @Get("glob:/glob/**")
    public String glob(@Param("0") String name) {
        return "glob: " + name;
    }

    @Get
    @Path("/paths/first/{name}")
    @Path("/paths/second/{name}")
    public String pathsVar(@Param String name) {
        return "paths: " + name;
    }
}
