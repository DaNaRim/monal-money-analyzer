package com.danarim.monal.config;

/**
 * Tomcat configuration.
 */
//@Configuration
public class ServerConfig {
//
//    @Value("${http-redirect-server.port}")
//    private int redirectServerPort;
//
//    @Value("${server.port}")
//    private int serverPort;
//
//    /**
//     * Redirects all HTTP requests to HTTPS.
//     *
//     * @return ServletWebServerFactory with redirect configuration.
//     */
//    @Bean
//    public ServletWebServerFactory servletContainer() {
//        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
//            @Override
//            protected void postProcessContext(Context context) {
//                SecurityConstraint securityConstraint = new SecurityConstraint();
//                securityConstraint.setUserConstraint("CONFIDENTIAL");
//
//                SecurityCollection collection = new SecurityCollection();
//                collection.addPattern("/*");
//
//                securityConstraint.addCollection(collection);
//                context.addConstraint(securityConstraint);
//            }
//        };
//        tomcat.addAdditionalTomcatConnectors(getHttpConnector());
//        return tomcat;
//    }
//
//    /**
//     * Bean for customizing the Tomcat server. This is needed to allow cookies with json.
//     *
//     * @return WebServerFactoryCustomizer
//     */
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
//        return container -> container.addContextCustomizers(context -> {
//            context.setCookieProcessor(new LegacyCookieProcessor());
//        });
//    }
//
//    private Connector getHttpConnector() {
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//        connector.setScheme("http");
//        connector.setPort(redirectServerPort);
//        connector.setSecure(false);
//        connector.setRedirectPort(serverPort);
//        return connector;
//    }

}
