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
import {Api1User} from "../fixtures/users";
import {Apis} from "../fixtures/apis";
import {createFakeAPI} from "../fixtures/test-data";

const fakeApi = createFakeAPI();

describe("create API", () => {

  it("should create API", () => {
    Apis.create(Api1User, fakeApi)
      .should((response) => {
        const apiId = response.body.id;
        expect(apiId).not.undefined;
        expect(response.status).to.eq(201);
        expect(response.body.state).to.eq("STOPPED");
      });
  });

});