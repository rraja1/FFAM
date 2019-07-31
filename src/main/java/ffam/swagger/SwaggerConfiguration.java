package ffam.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;

import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        TypeResolver typeResolver = new TypeResolver();

        return new Docket(DocumentationType.SWAGGER_2)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(
                        newRule(
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class),
                                typeResolver.resolve(WildcardType.class)
                        )
                )
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(not(regex("/env.*")))
                .paths(not(regex("/error")))
                .paths(not(regex("/health.*")))
                .paths(not(regex("/info")))
                .paths(not(regex("/pause")))
                .paths(not(regex("/refresh")))
                .paths(not(regex("/restart")))
                .paths(not(regex("/resume")))
                .paths(not(regex("/actuator.*")))
                .build()
                .produces(Collections.singleton("application/json"));
    }
}
