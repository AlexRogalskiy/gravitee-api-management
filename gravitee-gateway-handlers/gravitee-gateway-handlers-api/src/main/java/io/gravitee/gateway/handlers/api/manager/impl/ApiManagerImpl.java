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
package io.gravitee.gateway.handlers.api.manager.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.MapListenerAdapter;
import io.gravitee.common.event.EventManager;
import io.gravitee.definition.model.Plan;
import io.gravitee.gateway.env.GatewayConfiguration;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.gateway.handlers.api.manager.ApiManager;
import io.gravitee.gateway.reactor.ReactorEvent;
import io.gravitee.node.api.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.Collator;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.gravitee.gateway.handlers.api.definition.DefinitionContext.planRequired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApiManagerImpl extends MapListenerAdapter<String, Api> implements ApiManager, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ApiManagerImpl.class);

    @Autowired
    private EventManager eventManager;

    @Autowired
    private GatewayConfiguration gatewayConfiguration;

    @Autowired
    private HazelcastInstance hzInstance;

    @Autowired
    private ClusterManager clusterManager;

    private Map<String, Api> apis;

    @Override
    public void afterPropertiesSet() throws Exception {
        apis = hzInstance.getMap("apis");
        ((IMap) apis).addEntryListener(this, true);
    }

    @Override
    public void onEntryEvent(EntryEvent<String, Api> event) {
        // Replication is only done for secondary nodes
        if (! clusterManager.isMasterNode()) {
            if (event.getEventType() == EntryEventType.ADDED) {
                register(event.getValue());
            } else if (event.getEventType() == EntryEventType.UPDATED) {
                register(event.getValue());
            } else if (event.getEventType() == EntryEventType.REMOVED ||
                    event.getEventType() == EntryEventType.EVICTED ||
                    event.getEventType() == EntryEventType.EXPIRED) {
                unregister(event.getKey());
            }
        }
    }

    private boolean register(Api api, boolean force) {
        // Get deployed API
        Api deployedApi = get(api.getId());

        // Does the API have a matching sharding tags ?
        if (hasMatchingTags(api.getTags())) {
            // API to deploy
            api.setPlans(
                    api.getPlans()
                            .stream()
                            .filter(new Predicate<Plan>() {
                                @Override
                                public boolean test(Plan plan) {
                                    if (plan.getTags() != null && ! plan.getTags().isEmpty()) {
                                        boolean hasMatchingTags = hasMatchingTags(plan.getTags());
                                        logger.debug("Plan name[{}] api[{}] has been ignored because not in configured sharding tags", plan.getName(), api.getName());
                                        return hasMatchingTags;
                                    }

                                    return true;
                                }
                            }).collect(Collectors.toList()));

            // API is not yet deployed, so let's do it !
            if (deployedApi == null || force) {
                deploy(api);
                return true;
            } else if (deployedApi.getDeployedAt().before(api.getDeployedAt())) {
                update(api);
                return true;
            }
        } else {
            logger.debug("The API {} has been ignored because not in configured tags {}", api.getName(), api.getTags());

            // Check that the API was not previously deployed with other tags
            // In that case, we must undeploy it
            if (deployedApi != null) {
                undeploy(api.getId());
            }
        }

        return false;
    }

    @Override
    public boolean register(Api api) {
        return register(api, false);
    }

    @Override
    public void unregister(String apiId) {
        undeploy(apiId);
    }

    @Override
    public void refresh() {
        apis.forEach((s, api) -> register(api, true));
    }

    private void deploy(Api api) {
        MDC.put("api", api.getId());

        if (api.isEnabled()) {
            logger.info("Deployment of {}", api);

            // Deploy the API only if there is at least one plan
            if (!api.getPlans().isEmpty() || !planRequired(api)) {
                logger.info("Deploying {} plan(s) for {}:", api.getPlans().size(), api);
                for (Plan plan : api.getPlans()) {
                    logger.info("\t- {}", plan.getName());
                }

                apis.put(api.getId(), api);
                eventManager.publishEvent(ReactorEvent.DEPLOY, api);
            } else {
                logger.warn("There is no published plan associated to this API, skipping deployment...");
            }
        } else {
            logger.debug("{} is not enabled. Skip deployment.", api);
        }

        MDC.remove("api");
    }

    private void update(Api api) {
        MDC.put("api", api.getId());
        logger.info("Updating {}", api);

        if (!api.getPlans().isEmpty() || !planRequired(api)) {
            logger.info("Deploying {} plan(s) for {}:", api.getPlans().size(), api);
            for(Plan plan: api.getPlans()) {
                logger.info("\t- {}", plan.getName());
            }

            apis.put(api.getId(), api);
            eventManager.publishEvent(ReactorEvent.UPDATE, api);
        } else {
            logger.warn("There is no published plan associated to this API, undeploy it...");
            undeploy(api.getId());
        }

        MDC.remove("api");
    }

    private void undeploy(String apiId) {
        Api currentApi = apis.remove(apiId);
        if (currentApi != null) {
            MDC.put("api", apiId);
            logger.info("Undeployment of {}", currentApi);

            eventManager.publishEvent(ReactorEvent.UNDEPLOY, currentApi);
            logger.info("{} has been undeployed", currentApi);
            MDC.remove("api");
        }
    }

    private boolean hasMatchingTags(Set<String> tags) {
        final Optional<List<String>> optTagList = gatewayConfiguration.shardingTags();

        if (optTagList.isPresent()) {
            List<String> tagList = optTagList.get();
            if (tags != null) {
                final List<String> inclusionTags = tagList.stream()
                        .map(String::trim)
                        .filter(tag -> !tag.startsWith("!"))
                        .collect(Collectors.toList());

                final List<String> exclusionTags = tagList.stream()
                        .map(String::trim)
                        .filter(tag -> tag.startsWith("!"))
                        .map(tag -> tag.substring(1))
                        .collect(Collectors.toList());

                if (inclusionTags.stream().anyMatch(exclusionTags::contains)) {
                    throw new IllegalArgumentException("You must not configure a tag to be included and excluded");
                }

                return inclusionTags.stream()
                        .anyMatch(tag -> tags.stream()
                                .anyMatch(crtTag -> {
                                    final Collator collator = Collator.getInstance();
                                    collator.setStrength(Collator.NO_DECOMPOSITION);
                                    return collator.compare(tag, crtTag) == 0;
                                })
                        ) || (!exclusionTags.isEmpty() &&
                        exclusionTags.stream()
                                .noneMatch(tag -> tags.stream()
                                        .anyMatch(crtTag -> {
                                            final Collator collator = Collator.getInstance();
                                            collator.setStrength(Collator.NO_DECOMPOSITION);
                                            return collator.compare(tag, crtTag) == 0;
                                        })
                                ));
            }
        }

        // no tags configured on this gateway instance
        return true;
    }

    @Override
    public Collection<Api> apis() {
        return apis.values();
    }

    @Override
    public Api get(String name) {
        return apis.get(name);
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setApis(Map<String, Api> apis) {
        this.apis = apis;
    }
}
