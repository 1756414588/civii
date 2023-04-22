package com.game.uc.servlet.jetty;

import com.game.spring.SpringUtil;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Jetty启动类
 *
 * @author jyb
 */
//public class JettyServer implements ApplicationContextAware {
public class JettyServer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    //private ApplicationContext applicationContext
    private String host = "0.0.0.0";
    private int port = 8080;
    private int securePort = 8443;
    private int minThread = 16;
    private int maxThread = 128;
    private int idleTimeout = 30000;

    private AtomicBoolean start  = new AtomicBoolean(false);
    /**
     * 注解包路径
     */
    private String packagePath;

    // private String descriptor = "web/WEB-INF/web.xml";
    // private String resourceBase = "web";

    public int getMinThread() {
        return minThread;
    }

    public void setMinThread(int minThread) {
        this.minThread = minThread;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext)
//			throws BeansException {
//		this.applicationContext = applicationContext;
//	}

    protected void init() {
        // Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThread);
        threadPool.setMaxThreads(maxThread);
        threadPool.setName("Jetty-QTP");
        // Setup server
        server = new Server(threadPool);
        server.manage(threadPool);

        // Common HTTP configuration
        HttpConfiguration config = new HttpConfiguration();
        config.setSecurePort(securePort);
        config.addCustomizer(new ForwardedRequestCustomizer());
        config.addCustomizer(new SecureRequestCustomizer());
        config.setSendDateHeader(true);
        config.setSendServerVersion(true);

        // Http Connector
        HttpConnectionFactory http = new HttpConnectionFactory(config);
        ServerConnector httpConnector = new ServerConnector(server, http);
        if (host != null) {
            httpConnector.setHost(host);
        }
        httpConnector.setPort(port);
        httpConnector.setIdleTimeout(idleTimeout);
        server.addConnector(httpConnector);

		/*
         * // SSL configurations SslContextFactory sslContextFactory = new
		 * SslContextFactory(); sslContextFactory.setKeyStorePath(jetty_root +
		 * "/jetty-server/src/main/config/etc/keystore");
		 * sslContextFactory.setKeyStorePassword
		 * ("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
		 * sslContextFactory.setKeyManagerPassword
		 * ("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
		 * sslContextFactory.setTrustStorePath(jetty_root +
		 * "/jetty-server/src/main/config/etc/keystore");
		 * sslContextFactory.setTrustStorePassword
		 * ("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
		 * sslContextFactory.setExcludeCipherSuites( "SSL_RSA_WITH_DES_CBC_SHA",
		 * "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
		 * "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
		 * "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
		 * "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
		 * "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
		 * 
		 * 
		 * // Spdy Connector SPDYServerConnectionFactory.checkNPNAvailable();
		 * PushStrategy push = new ReferrerPushStrategy();
		 * HTTPSPDYServerConnectionFactory spdy2 = new
		 * HTTPSPDYServerConnectionFactory(2,config,push);
		 * spdy2.setInputBufferSize(8192); spdy2.setInitialWindowSize(32768);
		 * HTTPSPDYServerConnectionFactory spdy3 = new
		 * HTTPSPDYServerConnectionFactory(3,config,push);
		 * spdy2.setInputBufferSize(8192); NPNServerConnectionFactory npn = new
		 * NPNServerConnectionFactory
		 * (spdy3.getProtocol(),spdy2.getProtocol(),http.getProtocol());
		 * npn.setDefaultProtocol(http.getProtocol());
		 * npn.setInputBufferSize(1024); SslConnectionFactory ssl = new
		 * SslConnectionFactory(sslContextFactory,npn.getProtocol());
		 * ServerConnector spdyConnector = new
		 * ServerConnector(server,ssl,npn,spdy3,spdy2,http);
		 * spdyConnector.setPort(8443); spdyConnector.setIdleTimeout(15000);
		 * server.addConnector(spdyConnector);
		 */

        // Handlers
        // HandlerCollection handlers = new HandlerCollection();
        // ContextHandlerCollection contexts = new ContextHandlerCollection();
        // RequestLogHandler requestLogHandler = new RequestLogHandler();
        // handlers.setHandlers(new Handler[]{ contexts, new DefaultHandler(),
        // requestLogHandler });
        //
        // // Add restart handler to test the ability to save sessions and
        // restart
        // RestartHandler restart = new RestartHandler();
        // restart.setHandler(handlers);
        // server.setHandler(restart);

        WebAppContext webAppContext = new WebAppContext();
        // webAppContext.setContextPath("/");
        // webAppContext.setDescriptor(descriptor);
        // webAppContext.setResourceBase(resourceBase);
        // webAppContext.setConfigurationDiscovered(true);
        // webAppContext.setParentLoaderPriority(true);
        // server.setHandler(webAppContext);

        DispatcherServlet servlet = new DispatcherServlet();
        //servlet.setContextConfigLocation(packagePath);
        servlet.setContextConfigLocation("classpath:servlet-context.xml");
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder("baseServlet", servlet), "/");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
        server.setHandler(handlers);

        // 以下代码是关键
        // webAppContext.setClassLoader(applicationContext.getClassLoader());
        XmlWebApplicationContext xmlWebAppContext = new XmlWebApplicationContext();
        xmlWebAppContext.setParent(SpringUtil.getApplicationContext());
        xmlWebAppContext.setConfigLocation("");
        xmlWebAppContext.setServletContext(webAppContext.getServletContext());
        xmlWebAppContext.refresh();

        webAppContext.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                xmlWebAppContext);

        xmlWebAppContext.refresh();
    }

    public void start() throws Exception {
        if (server == null)
            init();

        Thread thread = new Thread("Jetty-Server") {
            @Override
            public void run() {
                try {
                    server.start();
                    start.set(true);
                    server.join();
                } catch (Exception e) {
                    logger.error("Jetty server start fail.", e);
                }
            }

        };

        thread.start();
    }

    public AtomicBoolean getStart() {
        return start;
    }

    public void setStart(AtomicBoolean start) {
        this.start = start;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }

    static class RestartHandler extends HandlerWrapper {
        private Logger logger = LoggerFactory.getLogger(getClass());

		/* ------------------------------------------------------------ */

        /**
         * @see HandlerWrapper#handle(String,
         * Request,
         * HttpServletRequest,
         * HttpServletResponse)
         */
        @Override
        public void handle(String target, Request baseRequest,
                           HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            super.handle(target, baseRequest, request, response);
            if (Boolean.valueOf(request.getParameter("restart"))) {
                final Server server = getServer();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            server.stop();
                            Thread.sleep(100);
                            server.start();
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }.start();
            }
        }
    }

}
