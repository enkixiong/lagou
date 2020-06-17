package engine.ioc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertySource {

    /**
     * Indicate the resource location(s) of the properties file to be loaded.
     * <p>Both traditional and XML-based properties file formats are supported
     * &mdash; for example, {@code "classpath:/com/myco/app.properties"}
     * or {@code "file:/path/to/file.xml"}.
     * <p>Resource location wildcards (e.g. *&#42;/*.properties) are not permitted;
     * each location must evaluate to exactly one {@code .properties} resource.
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}. See {@linkplain org.springframework.context.annotation.PropertySource above}
     * for examples.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    String[] value();


}
