/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
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
package io.gravitee.admin.api.resources;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * @author Azize Elamrani (azize dot elamrani at gmail dot com)
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) throws Exception {
        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", "io.gravitee.admin.api.resources");
        servletHolder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        final Server server = new Server(8059);
        final ServletContextHandler context = new ServletContextHandler(server, "/rest", ServletContextHandler.NO_SESSIONS);
        context.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }
}