/*
 * Copyright 2020 io.xream.sqli
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.spi;

import io.xream.sqli.core.BaseFinder;
import io.xream.sqli.core.ResultMapFinder;
import io.xream.sqli.dialect.Dialect;

import java.util.Collection;
import java.util.List;

/**
 * @author Sim
 */
public interface JdbcHelper extends BaseFinder, ResultMapFinder {

    <T> boolean createBatch(Class<T> clzz, String sql, BatchObjectValues batchObjectValues, int batchSize, Dialect dialect);

    boolean create(boolean isAutoIncreaseId, String sql, List<Object> valueList);

    boolean createOrReplace(String sql, List<Object> valueList);

    boolean refresh(String sql, Object[] valueList);

    boolean remove(String sql, Object id);

    boolean execute(String sql,Object...objs);

    <K> List<K> queryForPlainValueList(Class<K> clzz, String sql, Collection<Object> valueList, Dialect dialect);

    interface BatchObjectValues {
        List<Collection<Object>> valuesList();
    }
}
