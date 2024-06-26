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
package io.xream.sqli.builder;

import java.util.Map;

/**
 * @author Sim
 */
public enum JoinType {
    NON_JOIN,
    JOIN,
    INNER,
    OUTER,
    LEFT,
    RIGHT,
    COMMA;

//    private String sql;
//    private JoinType(String sql){ //UNWORKABLE IN JAVA 17+
//        this.sql = sql;
//    }

    private static Map<JoinType, String> config = Map.of(
            NON_JOIN,",",
            JOIN,"JOIN",
            INNER,"INNER JOIN",
            OUTER,"OUTER JOIN",
            LEFT,"LEFT JOIN",
            RIGHT,"RIGHT JOIN",
            COMMA,","
            );

    public String sql(){
        return config.get(this);
    }
}
