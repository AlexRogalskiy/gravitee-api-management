/*
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

import { API_PUBLISHER_USER } from '../../fakers/users/users';
import { gio } from '../../commands/gravitee.commands';
import { ApiFakers } from '../../fakers/apis';

describe('Bulk plans', () => {
  it('Should create and publish plans', () => {
    gio
      .management(API_PUBLISHER_USER)
      .apis()
      .getAll()
      .ok()
      .should((response) => {
        let apis = response.body;
        apis.forEach((api, index) => {
          gio
            .management(API_PUBLISHER_USER)
            .apisPlans()
            .create(api.id, ApiFakers.plan())
            .created()
            .should((publishResponse) => {
              expect(publishResponse.body.status).to.eq('PUBLISHED');
            });
        });
      });
  });
});
