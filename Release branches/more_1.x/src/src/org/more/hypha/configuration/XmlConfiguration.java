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
package org.more.hypha.configuration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.more.NoDefinitionException;
import org.more.RepeateException;
import org.more.core.xml.XmlParserKit;
import org.more.core.xml.XmlParserKitManager;
import org.more.core.xml.stream.XmlReader;
import org.more.hypha.AbstractEventManager;
import org.more.hypha.DefineResource;
import org.more.hypha.DefineResourcePlugin;
import org.more.hypha.EventManager;
import org.more.hypha.beans.AbstractBeanDefine;
import org.more.hypha.beans.support.QuickPropertyParser;
import org.more.hypha.beans.support.TagBeans_Beans;
import org.more.hypha.event.AddBeanDefineEvent;
import org.more.hypha.event.AddPluginEvent;
import org.more.hypha.event.BeginInitEvent;
import org.more.hypha.event.DestroyEvent;
import org.more.hypha.event.EndInitEvent;
import org.more.hypha.event.ReloadEvent;
import org.more.util.ClassPathUtil;
import org.more.util.attribute.AttBase;
import org.more.util.attribute.IAttribute;
/**
 * xml�������������Ѿ������xml��������Ҫ�����й���������Ҫ�����ض�Ҫ��ע����Ӧ�������ռ��������
 * �Լ�����ֵԪ��Ϣ���������ɡ�
 * @version 2010-9-15
 * @author ������ (zyc@byshell.org)
 */
public class XmlConfiguration implements DefineResource {
    /**  */
    private static final long                 serialVersionUID = -2907262416329013610L;
    //
    private static final String               ResourcePath     = "/META-INF/resource/hypha/register.xml";  //
    //
    private String                            sourceName       = null;                                     //��Դ��
    private URI                               sourceURI        = null;                                     //xml URI����
    private InputStream                       sourceStream     = null;                                     //xml ������
    private Map<String, DefineResourcePlugin> pluginList       = null;                                     //�������
    private Map<String, AbstractBeanDefine>   defineMap        = new HashMap<String, AbstractBeanDefine>(); //bean����Map
    private EventManager                      eventManager     = new AbstractEventManager() {};            //�¼�������
    private IAttribute                        attributeManager = null;                                     //���Թ�����
    //
    private ArrayList<QuickPropertyParser>    quickParser      = new ArrayList<QuickPropertyParser>();     //���Կ��ٽ���������
    private XmlParserKitManager               manager          = new XmlParserKitManager();                //xml������
    //========================================================================================���췽��
    /**����{@link XmlConfiguration}����init������Ҫ�ֶ����С�*/
    public XmlConfiguration() throws IOException, XMLStreamException {
        this.initRegedit(this);
    }
    /**����{@link XmlConfiguration}����sourceFile��Ҫװ�ص������ļ���*/
    public XmlConfiguration(String sourceFile) throws IOException, XMLStreamException {
        this(new File(sourceFile));
    }
    /**����{@link XmlConfiguration}����sourceURI��Ҫװ�ص������ļ���*/
    public XmlConfiguration(URI sourceURI) throws IOException, XMLStreamException {
        this.initRegedit(this);
        this.sourceURI = sourceURI;
        this.init();
    }
    /**����{@link XmlConfiguration}����sourceFile��Ҫװ�ص������ļ���*/
    public XmlConfiguration(File sourceFile) throws IOException, XMLStreamException {
        this.initRegedit(this);
        this.sourceURI = sourceFile.toURI();
        this.init();
    }
    /**����{@link XmlConfiguration}����sourceStream��Ҫװ�ص������ļ�����*/
    public XmlConfiguration(InputStream sourceStream) throws IOException, XMLStreamException {
        this.initRegedit(this);
        this.sourceStream = sourceStream;
        this.init();
    }
    /**ִ�г�ʼ��ע�ᡣ */
    private void initRegedit(XmlConfiguration config) throws IOException, XMLStreamException {
        List<InputStream> ins = ClassPathUtil.getResource(ResourcePath);
        NameSpaceConfiguration ns = new NameSpaceConfiguration(config);
        for (InputStream is : ins)
            new XmlReader(is).reader(ns, null);
    }
    //========================================================================================DefineResourcePluginSet�ӿ�
    /**������չDefine����������*/
    public DefineResourcePlugin getPlugin(String name) {
        if (this.pluginList == null)
            return null;
        return this.pluginList.get(name);
    };
    /**����һ��������������������滻�����Ĳ��ע�ᡣ*/
    public void setPlugin(String name, DefineResourcePlugin plugin) {
        if (this.pluginList == null)
            this.pluginList = new HashMap<String, DefineResourcePlugin>();
        this.getEventManager().pushEvent(new AddPluginEvent(this, plugin));//TODO �²��
        this.pluginList.put(name, plugin);
    };
    /**ɾ��һ�����еĲ��ע�ᡣ*/
    public void removePlugin(String name) {
        this.pluginList.remove(name);
    };
    //========================================================================================
    /**��ȡһ��{@link AbstractBeanDefine}���塣*/
    public AbstractBeanDefine getBeanDefine(String name) throws NoDefinitionException {
        if (this.defineMap.containsKey(name) == false)
            throw new NoDefinitionException("����������Ϊ[" + name + "]��Bean���塣");
        return this.defineMap.get(name);
    };
    /**����ĳ�����Ƶ�bean�����Ƿ���ڡ�*/
    public boolean containsBeanDefine(String name) {
        return this.defineMap.containsKey(name);
    }
    /**����һ��Bean���壬�����ӵ�Bean����ᱻִ�м�⡣*/
    public void addBeanDefine(AbstractBeanDefine define) {
        this.getEventManager().pushEvent(new AddBeanDefineEvent(this, define));//TODO ��Bean����
        if (this.defineMap.containsKey(define.getName()) == true)
            throw new RepeateException("[" + define.getName() + "]Bean�����ظ���");
        this.defineMap.put(define.getID(), define);
    };
    /**ʹ��ָ��������������*/
    private XmlConfiguration passerXml(InputStream in) throws XMLStreamException {
        XmlReader reader = new XmlReader(in);
        this.manager.getContext().setAttribute(TagBeans_Beans.BeanDefineManager, this);
        reader.reader(this.manager, null);
        return this;
    };
    /**��ȡ{@link XmlParserKitManager}*/
    protected XmlParserKitManager getManager() {
        return this.manager;
    }
    //========================================================================================
    /**ע��һ����������ֵ��������*/
    public void regeditQuickParser(QuickPropertyParser parser) {
        if (parser == null)
            throw new NullPointerException("QuickPropertyParser���Ͳ�������Ϊ�ա�");
        if (this.quickParser.contains(parser) == false)
            this.quickParser.add(parser);
    }
    /**ȡ��һ����������ֵ��������ע�ᡣ*/
    public void unRegeditQuickParser(QuickPropertyParser parser) {
        if (parser == null)
            throw new NullPointerException("QuickPropertyParser���Ͳ�������Ϊ�ա�");
        if (this.quickParser.contains(parser) == true)
            this.quickParser.remove(parser);
    }
    /**��ȡע���{@link QuickPropertyParser}����*/
    public List<QuickPropertyParser> getQuickList() {
        return this.quickParser;
    }
    /**ע��һ����ǩ�������߼���*/
    public void regeditXmlParserKit(String namespace, XmlParserKit kit) {
        this.manager.regeditKit(namespace, kit);
    }
    /**ȡ��һ����ǩ�������߼���ע�ᡣ*/
    public void unRegeditXmlParserKit(String namespace, XmlParserKit kit) {
        this.manager.unRegeditKit(namespace, kit);
    }
    /**����{@link XmlConfiguration}�������{@link XmlConfiguration}ʱʹ�õ�������ʽ��ô����Ҫ֧��reset����������쳣��*/
    public void reload() throws XMLStreamException, MalformedURLException, IOException {
        this.getEventManager().pushEvent(new ReloadEvent(this));//TODO ����
        this.destroy();
        this.init();
    }
    /**�������������������³�ʼ��{@link XmlConfiguration}����*/
    public void init() throws XMLStreamException, MalformedURLException, IOException {
        if (this.sourceStream == null)
            this.sourceStream = this.sourceURI.toURL().openStream();
        try {
            this.sourceStream.reset();
        } catch (Exception e) {}
        this.getEventManager().pushEvent(new BeginInitEvent(this));//TODO ��ʼ��ʼ��
        this.getEventManager().popEvent(BeginInitEvent.class);
        this.passerXml(this.sourceStream);
        this.getEventManager().pushEvent(new EndInitEvent(this));//TODO ������ʼ��
        this.getEventManager().popEvent(EndInitEvent.class);
        //
        this.getEventManager().popEvent();
    };
    /**�������ע���Bean����*/
    public void destroy() {
        this.getEventManager().pushEvent(new DestroyEvent(this));//TODO ����
        this.defineMap.clear();
    }
    /**������Դ��*/
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    public String getSourceName() {
        return this.sourceName;
    }
    public IAttribute getAttribute() {
        if (this.attributeManager == null)
            this.attributeManager = new AttBase();
        return this.attributeManager;
    }
    public EventManager getEventManager() {
        return this.eventManager;
    }
    public URI getSourceURI() {
        return this.sourceURI;
    }
    public List<String> getBeanDefineNames() {
        return new ArrayList<String>(this.defineMap.keySet());
    }
    public boolean isPrototype(String name) throws NoDefinitionException {
        AbstractBeanDefine define = this.getBeanDefine(name);
        return (define.factoryName() == null) ? false : true;
    }
    public boolean isSingleton(String name) throws NoDefinitionException {
        AbstractBeanDefine define = this.getBeanDefine(name);
        return define.isSingleton();
    }
    public boolean isFactory(String name) throws NoDefinitionException {
        AbstractBeanDefine define = this.getBeanDefine(name);
        return (define.factoryName() == null) ? false : true;
    }
}