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
package io.gravitee.gateway.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.model.Api;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * File API repository.
 * This repository is based on JSON processed files to provide Gateway configuration.
 *
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class FileRepository extends AbstractRepository {

    private final static String JSON_EXTENSION = ".json";
//    private final static String DEFAULT_FILE_REPOSITORY = "/etc/gravitee.io/conf";

    @Value("${repository.file.path:/etc/gravitee.io/conf}")
    private String repositoryPath;

    /*
    @Autowired
    private Properties configuration;
    */

    public FileRepository() {

    }

    public FileRepository(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    @PostConstruct
    public void init() {
        if (repositoryPath == null || repositoryPath.isEmpty()) {
            LOGGER.error("Repository path is not specified.");
            throw new RuntimeException("Repository path is not specified.");
        }

        File repositoryDir = new File(repositoryPath);

        // quick sanity check on the install root
        if (! repositoryDir.isDirectory()) {
            LOGGER.error("Invalid repository directory, {} is not a directory.", repositoryDir.getAbsolutePath());
            throw new RuntimeException("Invalid repository directory. Not a directory: "
                    + repositoryDir.getAbsolutePath());
        }

        /*
        if (repositoryPath == null) {
            // fallback to system properties
            String repositoryPath = configuration.getProperty("repository.file.path", DEFAULT_FILE_REPOSITORY);

            LOGGER.info("No directory set for FileRepository, fallback using property 'repository.file.path': {}", repositoryPath);
            workspaceDir = new File(repositoryPath);
        }
        */

        if (repositoryDir.exists()) {
            LOGGER.info("Initializing file repository with directory set to {}", repositoryDir);
            readConfiguration(repositoryDir);
        } else {
            LOGGER.warn("No configuration can be read from {}",
                    repositoryDir.getAbsolutePath());
        }
    }

    @Override
    public boolean create(Api api) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean update(Api api) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<Api> fetchAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Api fetch(final String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void readConfiguration(File configuration) {
        LOGGER.info("Loading repository data from {}", configuration.getAbsolutePath());

        Set<File> configurations = lookingForConfigurationFiles(configuration);

        // Initialize Jackson mapper to read json files
        ObjectMapper mapper = new ObjectMapper();

        // Read all configuration files
        for (File conf : configurations) {
            try {
                LOGGER.info("Read repository configuration from {}", conf.getAbsolutePath());
                register(mapper.readValue(conf, Api.class));
            } catch (IOException ioe) {
                LOGGER.error("Unable to read file : {}", conf, ioe);
            }
        }

        LOGGER.info("{} API(s) registered", listAll().size());
    }

    private Set<File> lookingForConfigurationFiles(File configuration) {
        if (configuration.isFile()) {
            LOGGER.debug("Provided configuration path is a file...");

            // Check if provided file is suffixed with .json
            if (!configuration.getName().endsWith(JSON_EXTENSION)) {
                LOGGER.error("Configuration file is not a JSON file (does not end with .json)");
                throw new IllegalStateException("Configuration file is not a JSON file (does not end with .json)");
            }

            return Collections.singleton(configuration);
        } else {
            LOGGER.debug("Provided configuration is a directory, looking for json files.");
            final File[] confs = configuration.listFiles(pathname -> {
                return pathname.getName().endsWith(JSON_EXTENSION);
            });

            return new HashSet<>(Arrays.asList(confs));
        }
    }

    /*
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }
    */
}
