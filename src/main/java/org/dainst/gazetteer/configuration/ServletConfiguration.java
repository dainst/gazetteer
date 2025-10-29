package org.dainst.gazetteer.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dainst.gazetteer.converter.JsonPlaceMessageConverter;
import org.dainst.gazetteer.converter.KmlPlaceMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
@EnableWebMvc
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ServletComponentScan(basePackages = "org.dainst.gazetteer")
public class ServletConfiguration implements WebMvcConfigurer {

    private final String CONTENT_TYPE_HTML = "text/html";
    private final String CONTENT_TYPE_KML = "application/vnd.google-earth.kml+xml";
    private final String CONTENT_TYPE_JSON = "application/json";
    private final String CONTENT_TYPE_GEOJSON = "application/vnd.geo+json";
    private final String CONTENT_TYPE_JS = "application/javascript";
    private final String CONTENT_TYPE_RDF = "application/rdf+xml";

    @Bean
    public Map<String, String> mediaTypes() {
        return Map.of(
                "html", CONTENT_TYPE_HTML,
                "kml", CONTENT_TYPE_KML,
                "json", CONTENT_TYPE_JSON,
                "geojson", CONTENT_TYPE_GEOJSON,
                "js", CONTENT_TYPE_JS,
                "rdf", CONTENT_TYPE_RDF
        );
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.
                defaultContentType(
                        MediaType.parseMediaType(CONTENT_TYPE_HTML),
                        MediaType.parseMediaType(CONTENT_TYPE_KML),
                        MediaType.parseMediaType(CONTENT_TYPE_JSON),
                        MediaType.parseMediaType(CONTENT_TYPE_GEOJSON),
                        MediaType.parseMediaType(CONTENT_TYPE_JS),
                        MediaType.parseMediaType(CONTENT_TYPE_RDF)
                );
    }

    @Autowired
    JsonPlaceMessageConverter jsonPlaceMessageConverter;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.addAll(List.of(
                new KmlPlaceMessageConverter(),
                jsonPlaceMessageConverter,
                new ByteArrayHttpMessageConverter(),
                new Jaxb2RootElementHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter(),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new AllEncompassingFormHttpMessageConverter()
        ));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver htmlResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/html/",
                ".jsp"
        );
        htmlResolver.setContentType(CONTENT_TYPE_HTML);
        htmlResolver.setViewClass(JstlView.class);

        InternalResourceViewResolver jsonResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/json/",
                ".jsp"
        );
        jsonResolver.setContentType(CONTENT_TYPE_JSON);

        InternalResourceViewResolver geoJsonResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/geojson/",
                ".jsp"
        );
        geoJsonResolver.setContentType(CONTENT_TYPE_GEOJSON);

        InternalResourceViewResolver kmlResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/kml/",
                ".jsp"
        );
        kmlResolver.setContentType(CONTENT_TYPE_KML);

        InternalResourceViewResolver jsResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/javascript/",
                ".jsp"
        );
        jsResolver.setContentType(CONTENT_TYPE_JS);

        InternalResourceViewResolver rdfResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/rdf/",
                ".jsp"
        );
        rdfResolver.setContentType(CONTENT_TYPE_RDF);

        InternalResourceViewResolver jspResolver = new InternalResourceViewResolver(
                "/WEB-INF/views/",
                ".jsp"
        );


        registry.viewResolver(htmlResolver);
        registry.viewResolver(jsonResolver);
        registry.viewResolver(geoJsonResolver);
        registry.viewResolver(kmlResolver);
        registry.viewResolver(jsResolver);
        registry.viewResolver(rdfResolver);
        registry.viewResolver(jspResolver);
    }

    LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.GERMAN);
        return cookieLocaleResolver;
    }

    HandlerInterceptor localeResolverInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (handler instanceof HandlerMethod) {
                    request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver());
                }
                return true;
            }
        };
    }

    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeResolverInterceptor());
        registry.addInterceptor(localeChangeInterceptor());
    }
}
