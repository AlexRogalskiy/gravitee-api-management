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
const conf = require('./gulp.conf');
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (env) {
  return {
    server: {
      baseDir: [conf.paths.tmp, conf.paths.src],
    },
    open: false,
    middleware: createProxyMiddleware(env ? `https://${env}.gravitee.io/management/**` : 'http://localhost:8083/management/**', {
      changeOrigin: Boolean(env),
      secure: false,
    }),
  };
};
