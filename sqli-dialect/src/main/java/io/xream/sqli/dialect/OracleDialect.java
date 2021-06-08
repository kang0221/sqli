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
package io.xream.sqli.dialect;

import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.exception.NotSupportedException;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliJsonUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class OracleDialect implements Dialect {

    private final static String ORACLE_PAGINATION = "SELECT * FROM (SELECT A.*, ROWNUM RN FROM ( ${SQL} ) A   WHERE ROWNUM <= ${END}  )  WHERE RN > ${BEGIN} ";
    private final static String ORACLE_PAGINATION_REGX_SQL = "${SQL}";
    private final static String ORACLE_PAGINATION_REGX_BEGIN = "${BEGIN}";
    private final static String ORACLE_PAGINATION_REGX_END = "${END}";
    private Method NCLOBReader = null;
    private Method NCLOBLength = null;
    private  void init() {
        try{
            Class clzz = Class.forName("oracle.sql.NCLOB");
            NCLOBReader = clzz.getDeclaredMethod("getCharacterStream");
            NCLOBLength = clzz.getDeclaredMethod("length");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public OracleDialect(){
        init();
    }

    @Override
    public String getKey(){
        return "oracle";
    }
    @Override
    public String buildPageSql(String origin, long start, long rows,long last) {

        if (rows > 0)
            return ORACLE_PAGINATION.replace(ORACLE_PAGINATION_REGX_END, String.valueOf(start + rows))
                    .replace(ORACLE_PAGINATION_REGX_BEGIN, String.valueOf(start)).replace(ORACLE_PAGINATION_REGX_SQL, origin);
        return origin;

    }


    private Object toNCLOBString(Object obj) {
        if (obj.getClass().getSimpleName().endsWith("NCLOB")) {
            Reader reader = null;
            try {
                reader = (Reader) NCLOBReader.invoke(obj);
                int length = (int) NCLOBLength.invoke(obj);
                char[] charArr = new char[length];
                reader.read(charArr);
                return new String(charArr);//FIXME UIF-8 ?
            } catch (Exception e) {
                SqliExceptionUtil.throwRuntimeExceptionFirst(e);
                throw new PersistenceException(SqliExceptionUtil.getMessage(e));
            }finally{
                if (reader !=null) {
                    try {
                        reader.close();
                    }catch (Exception e){

                    }
                }
            }
        }
        return obj;
    }

    private Object toBigDecimal(Class clzz, Object obj) {
        BigDecimal bg = (BigDecimal) obj;
        if (clzz == int.class || clzz == Integer.class) {
            return bg.intValue();
        } else if (clzz == long.class || clzz == Long.class) {
            return bg.longValue();
        } else if (clzz == double.class || clzz == Double.class) {
            return bg.doubleValue();
        } else if (clzz == float.class || clzz == Float.class) {
            return bg.floatValue();
        } else if (clzz == boolean.class || clzz == Boolean.class) {
            int i = bg.intValue();
            return i == 0 ? false : true;
        } else if (clzz == Date.class) {
            long l = bg.longValue();
            return new Date(l);
        } else if (clzz == java.sql.Date.class) {
            long l = bg.longValue();
            return new java.sql.Date(l);
        } else if (clzz == Timestamp.class) {
            long l = bg.longValue();
            return new Timestamp(l);
        } else if (clzz == byte.class || clzz == Byte.class) {
            return bg.byteValue();
        }
        return bg;
    }

    @Override
    public Object mappingToObject(Object obj, BeanElement element) {
        if (obj == null)
            return null;

        Class ec = element.getClz();

        obj = toNCLOBString(obj);

        if (element.isJson()) {

            if (SqliStringUtil.isNullOrEmpty(obj))
                return null;

            String str = obj.toString().trim();

            if (ec == List.class) {
                Class geneType = element.getGeneType();
                return SqliJsonUtil.toList(str, geneType);
            } else if (ec == Map.class) {
                return SqliJsonUtil.toMap(str);
            } else {
                return SqliJsonUtil.toObject(str, ec);
            }
        }else if (obj instanceof BigDecimal) {
            return toBigDecimal(ec, obj);

        } else if (obj instanceof Timestamp && ec == Date.class) {
            Timestamp ts = (Timestamp) obj;
            return new Date(ts.getTime());
        }else if (obj instanceof LocalDateTime) {
            if (ec == Date.class) {
                Instant instant = ((LocalDateTime)obj).atZone(ZoneId.systemDefault()).toInstant();
                obj = Date.from(instant);
            }else if (ec == Timestamp.class) {
                Instant instant = ((LocalDateTime)obj).atZone(ZoneId.systemDefault()).toInstant();
                obj = Timestamp.from(instant);
            }
        }else if (EnumUtil.isEnum(ec)) {
            return EnumUtil.deserialize(ec, obj.toString());
        }
        return obj;
    }

    @Override
    public String createOrReplaceSql(String sql) {
        throw new NotSupportedException("sqli not support createOrReplace() for Oracle");
    }

    @Override
    public String createSql(Parsed parsed, List<BeanElement> tempList) {
        return getDefaultCreateSql(parsed,tempList);
    }


    @Override
    public Object convertJsonToPersist(Object json) {
        if (json == null)
            return null;
        return new StringReader(json.toString());
    }

    @Override
    public String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap) {

        if (resultKeyAliaMap.containsKey(mapper)) {
            mapper = resultKeyAliaMap.get(mapper);
        }
        if (aliaMap.isEmpty())
            return mapper;

        if (mapper.contains(".")) {
            String[] arr = mapper.split("\\.");
            String alia = arr[0];
            String p = arr[1];
            String clzName = aliaMap.get(alia);
            if (SqliStringUtil.isNullOrEmpty(clzName)) {
                clzName = alia;
            }
            return clzName + "." + p;
        }
        return mapper;
    }

    @Override
    public Object filterValue(Object object) {
        return filter(object, (obj) -> {
            if (obj instanceof Date) {
                Date date = (Date) obj;
                Timestamp timestamp = new Timestamp(date.getTime());
                return timestamp;
            } else if (obj instanceof Boolean) {
                Boolean b = (Boolean) obj;
                return b.booleanValue() ? 1 : 0;
            }
            return obj;
        });
    }

    @Override
    public Object[] toArr(Collection<Object> list) {

        if (list == null || list.isEmpty())
            return null;
        int size = list.size();
        Object[] arr = new Object[size];
        int i = 0;
        for (Object obj : list) {
            obj = filterValue(obj);
            arr[i++] = obj;
        }
        return arr;
    }

    @Override
    public String getAlterTableUpdate() {
        return SqlScript.UPDATE;
    }

    @Override
    public String getAlterTableDelete() {
        return SqlScript.DELETE_FROM ;
    }

    @Override
    public String getCommandUpdate() {
        return SqlScript.SET;
    }

    @Override
    public String getCommandDelete() {
        return SqlScript.SPACE;
    }

    @Override
    public String getLimitOne() {
        return null;
    }

    @Override
    public String getInsertTagged() {
        return null;
    }

    @Override
    public void filterTags(List<BeanElement> list,List<Field> tagList) {
        return;
    }

    @Override
    public List<Object> objectToListForCreate(Object obj, Parsed parsed) {
        List<BeanElement> tempList = parsed.getBeanElementList();

        List<Object> list = new ArrayList<>();

        objectToListForCreate(list, obj, tempList);

        return list;

    }
}
