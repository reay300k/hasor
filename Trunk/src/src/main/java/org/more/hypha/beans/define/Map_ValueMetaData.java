/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.more.hypha.beans.define;
import java.util.HashMap;
import java.util.Map;
/**
 * ��ʾһ��{@link Map}���͵�ֵԪ��Ϣ��������Ӧ��PropertyMetaTypeEnum����Ϊ{@link PropertyMetaTypeEnum#MapCollection}��
 * @version 2010-9-17
 * @author ������ (zyc@byshell.org)
 */
public class Map_ValueMetaData extends Collection_ValueMetaData<MapEntity_ValueMetaData> {
    private HashMap<AbstractValueMetaData, AbstractValueMetaData> valueData = new HashMap<AbstractValueMetaData, AbstractValueMetaData>(); //����
    /**�÷������᷵��{@link PropertyMetaTypeEnum#MapCollection}��*/
    public String getMetaDataType() {
        return PropertyMetaTypeEnum.MapCollection;
    }
    /**��Map��ʽ���ؼ����е����ݡ�*/
    public Map<AbstractValueMetaData, AbstractValueMetaData> getCollectionValue() {
        return this.valueData;
    }
    /**����һ��Ԫ�ء�*/
    public void addObject(AbstractValueMetaData key, AbstractValueMetaData value) {
        this.valueData.put(key, value);
    };
    /**����һ��Ԫ�ء�*/
    public void addObject(MapEntity_ValueMetaData mapEntity) {
        this.valueData.put(mapEntity.getKey(), mapEntity.getValue());
    };
    /**ɾ��һ��Ԫ�ء�*/
    public void removeObject(AbstractValueMetaData key) {
        this.valueData.remove(key);
    };
    /**��ȡ���ϵ�ǰ����������������*/
    public int size() {
        return this.valueData.size();
    };
}