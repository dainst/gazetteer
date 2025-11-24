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
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, MediaType> mediaTypeMap() {
        return mediaTypes().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> MediaType.parseMediaType(entry.getValue()))
        );
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .mediaTypes(mediaTypeMap())
                .favorPathExtension(true)
                .defaultContentType(mediaTypeMap().get("html"));
    }

    /**
     * Create the CNVR. Get Spring to inject the ContentNegotiationManager
     * created by the configurer (see previous method).
     */
    @Bean
    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);
        resolver.setViewResolvers(viewResolvers());
        return resolver;
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

    public List<ViewResolver> viewResolvers() {
        ChainableInternalResourceViewResolver htmlResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/html/",
                ".jsp"
        );
        htmlResolver.setContentType(CONTENT_TYPE_HTML);

        ChainableInternalResourceViewResolver jsonResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/json/",
                ".jsp"
        );
        jsonResolver.setContentType(CONTENT_TYPE_JSON);

        ChainableInternalResourceViewResolver geoJsonResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/geojson/",
                ".jsp"
        );
        geoJsonResolver.setContentType(CONTENT_TYPE_GEOJSON);

        ChainableInternalResourceViewResolver kmlResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/kml/",
                ".jsp"
        );
        kmlResolver.setContentType(CONTENT_TYPE_KML);

        ChainableInternalResourceViewResolver jsResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/javascript/",
                ".jsp"
        );
        jsResolver.setContentType(CONTENT_TYPE_JS);

        ChainableInternalResourceViewResolver rdfResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/rdf/",
                ".jsp"
        );
        rdfResolver.setContentType(CONTENT_TYPE_RDF);

        ChainableInternalResourceViewResolver jspResolver = new ChainableInternalResourceViewResolver(
                "/WEB-INF/views/",
                ".jsp"
        );

        return new ArrayList<>(List.of(
                jsonResolver,
                geoJsonResolver,
                kmlResolver,
                jsonResolver,
                rdfResolver,
                jsonResolver,
                htmlResolver
        ));
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

    @Bean
    CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter("UTF-8", true);
    }

    private static class ChainableInternalResourceViewResolver extends InternalResourceViewResolver {

        public ChainableInternalResourceViewResolver(String prefix, String suffix) {
            super(prefix, suffix);
        }

        @Override
        protected AbstractUrlBasedView buildView(String viewName) throws Exception {
            String url = getPrefix() + viewName + getSuffix();
            InputStream stream = getServletContext().getResourceAsStream(url);
            if (stream == null) {
                return new NonExistentView();
            }
            return super.buildView(viewName);
        }

        private static class NonExistentView extends AbstractUrlBasedView {

            @Override
            protected boolean isUrlRequired() {
                return false;
            }

            @Override
            public boolean checkResource(Locale locale) throws Exception {
                return false;
            }

            @Override
            protected void renderMergedOutputModel(Map<String, Object> model,
                                                   HttpServletRequest request, HttpServletResponse response) throws Exception {
                // Purposely empty, it should never get called
            }
        }
    }
}
