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
package io.gravitee.rest.api.service.impl.swagger.policy.impl;

import io.gravitee.policy.api.swagger.Policy;
import io.gravitee.rest.api.service.impl.swagger.visitor.v2.SwaggerOperationVisitor;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import java.util.Optional;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SwaggerPolicyOperationVisitor
    extends AbstractPolicyOperationVisitor<Swagger, Operation>
    implements SwaggerOperationVisitor<Optional<Policy>> {

    public SwaggerPolicyOperationVisitor(io.gravitee.policy.api.swagger.v2.SwaggerOperationVisitor policyVisitor) {
        super(policyVisitor);
    }
}
