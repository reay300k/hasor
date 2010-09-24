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
package org.more.beans.resource.namespace;
import java.io.File;
import org.more.beans.ValueMetaData;
import org.more.beans.define.File_ValueMetaData;
import org.more.beans.define.QuickProperty_ValueMetaData;
import org.more.beans.resource.QuickParserEvent;
import org.more.beans.resource.QuickPropertyParser;
/**
 * �ļ���������ֵ��������
 * @version 2010-9-23
 * @author ������ (zyc@byshell.org)
 */
public class QPP_File implements QuickPropertyParser {
    public ValueMetaData parser(QuickParserEvent event) {
        QuickProperty_ValueMetaData meta = event.getOldMetaData();
        if (meta.getFile() == null)
            return null;
        File_ValueMetaData newMETA = new File_ValueMetaData();
        newMETA.setFileObject(new File(meta.getFile()));
        newMETA.setDir(false);
        return newMETA;
    }
}