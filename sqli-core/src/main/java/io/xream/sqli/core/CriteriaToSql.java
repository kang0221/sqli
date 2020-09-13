/*
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
package io.xream.sqli.core;

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.CriteriaCondition;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.builder.SqlBuilt;
import io.xream.sqli.parser.Parsed;

import java.util.List;


/**
 * @Author Sim
 */
public interface CriteriaToSql {

//    void setDialect(Dialect dialect);

    String toSql(CriteriaCondition criteriaCondition, List<Object> valueList, Alias alias) ;

    void toSql(boolean isSub, Criteria criteria, SqlBuilt sqlBuilt, SqlBuildingAttached sqlBuildingAttached) ;

    String toSql(Parsed parsed, RefreshCondition refreshCondition);

}