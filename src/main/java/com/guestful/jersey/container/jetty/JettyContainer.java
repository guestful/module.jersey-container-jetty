/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jersey.container.jetty;

import com.guestful.jersey.container.Container;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.ws.rs.core.Application;

public class JettyContainer extends Container {

    private Server server;

    @Override
    protected void doStart() throws Exception {
        server = buildServer();
        server.start();
    }

    @Override
    protected void doStop() throws Exception {
        server.stop();
        server = null;
    }

    @Override
    public boolean isRunning() {
        return server != null;
    }

    @Override
    public boolean isStopped() {
        return server == null;
    }

    private Server buildServer() {
        QueuedThreadPool pool = getMaxWorkers() > 0 ? new QueuedThreadPool(getMaxWorkers()) : new QueuedThreadPool();
        Server server = new Server(pool);
        HttpConfiguration config = new HttpConfiguration();
        config.setSendXPoweredBy(false);
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(config));
        http.setPort(getPort());
        server.setConnectors(new Connector[]{http});
        Handler context = buildHandler();
        server.setHandler(context);
        return server;
    }

    private Handler buildHandler() {
        ServletContextHandler context = new ServletContextHandler();
        context.setResourceBase("www");
        context.setDisplayName(JettyContainer.class.getSimpleName());
        context.setContextPath(getContextPath());
        Class<? extends Application> app = getApplicationClass();
        if (app != null) {
            ServletHolder holder = context.addServlet(ServletContainer.class, "/*");
            holder.setDisplayName(app.getName());
            holder.setAsyncSupported(true);
            holder.setInitOrder(0);
            holder.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, app.getName());
        }
        return context;
    }

}
