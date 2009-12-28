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
package org.more.beans.resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.more.FormatException;
import org.more.InvokeException;
import org.more.NoDefinitionException;
import org.more.ResourceException;
import org.more.beans.BeanResource;
import org.more.beans.info.BeanDefinition;
import org.more.beans.info.CreateTypeEnum;
import org.more.beans.resource.xml.XMLEngine;
import org.more.util.attribute.AttBase;
/**
 * 提供了以XML作为数据源提供bean数据的支持。
 * <br/>Date : 2009-11-25
 * @author 赵永春
 */
public class XmlFileResource extends AttBase implements BeanResource {
    //========================================================================================Field
    /**  */
    private static final long               serialVersionUID    = 5085542182667236561L;
    private File                            xmlFile             = null;                                 //配置文件
    private URI                             xmlURI              = null;                                 //配置文件
    private URL                             xmlURL              = null;                                 //配置文件
    private String                          resourceDescription = null;                                 //说明
    private ArrayList<String>               initNames           = null;                                 //要求启动时装载的bean
    private ArrayList<String>               allNames            = null;                                 //要求启动时装载的bean
    /*---------------------*/
    /** XML解析引擎 */
    protected XMLEngine                     xmlEngine           = null;
    /**静态缓存对象数目。*/
    protected int                           staticCacheSize     = 10;
    /**静态缓存对象。*/
    private HashMap<String, BeanDefinition> staticCache         = new HashMap<String, BeanDefinition>();
    /**动态缓存对象数目。*/
    protected int                           dynamicCacheSize    = 50;
    /**动态缓存对象。*/
    private HashMap<String, BeanDefinition> dynamicCache        = new HashMap<String, BeanDefinition>();
    /***/
    private LinkedList<String>              dynamicCacheNames   = new LinkedList<String>();
    /***/
    private boolean                         validatorXML        = true;                                 //是否开启XSD验证。
    //==================================================================================Constructor
    /**创建XmlFileResource对象。*/
    public XmlFileResource() {
        this.xmlEngine = new XMLEngine();
    }
    /**创建XmlFileResource对象，参数filePath是配置文件位置。*/
    public XmlFileResource(String filePath) {
        this();
        this.xmlFile = new File(filePath);
        this.reload();
    }
    /**创建XmlFileResource对象，参数file是配置文件位置。*/
    public XmlFileResource(File file) {
        this();
        this.xmlFile = file;
        this.reload();
    }
    /**创建XmlFileResource对象，参数xmlURI是配置文件位置。*/
    public XmlFileResource(URI xmlURI) {
        this();
        this.xmlURI = xmlURI;
        this.reload();
    }
    /**创建XmlFileResource对象，参数xmlURL是配置文件位置。*/
    public XmlFileResource(URL xmlURL) {
        this();
        this.xmlURL = xmlURL;
        this.reload();
    }
    //=========================================================================================Impl
    /**是否开启Schema验证XML配置正确性，默认值是true开启验证。*/
    public boolean isValidatorXML() {
        return validatorXML;
    }
    /**设置是否开启Schema验证XML配置正确性。*/
    public void setValidatorXML(boolean validatorXML) {
        this.validatorXML = validatorXML;
    }
    /** 通过Schema验证XML配置是否正确，返回null表示验证通过，否则返回错误信息。 */
    public String validatorConfigXML() {
        if (this.validatorXML == false)
            return null;
        //----
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");//建立schema工厂
            Source xsdSource = new StreamSource(XmlFileResource.class.getResourceAsStream("/META-INF/beans-schema.xsd"));
            Schema schema = schemaFactory.newSchema(xsdSource); //利用schema工厂，接收验证文档文件对象生成Schema对象
            Validator validator = schema.newValidator();//通过Schema产生针对于此Schema的验证器，利用students.xsd进行验证
            Source source = new StreamSource(this.getXmlInputStream());//得到验证的数据源
            //开始验证，成功输出success!!!，失败输出fail
            validator.validate(source);
            System.out.println("XmlFileResource validatorConfigXML OK!");
            return null;
        } catch (Exception ex) {
            System.out.println("XmlFileResource validatorConfigXML Error!");
            return "validator error[" + ex.getLocalizedMessage() + "]";
        }
    }
    @SuppressWarnings("unchecked")
    private void reload() {
        String str = this.validatorConfigXML();
        if (str != null)
            throw new FormatException("Schema验证失败：" + str);
        this.clearCache();
        AttBase att = (AttBase) this.xmlEngine.runTask(this.getXmlInputStream(), "init", ".*");
        this.dynamicCacheSize = (Integer) att.get("dynamicCache");
        this.staticCacheSize = (Integer) att.get("staticCache");
        this.staticCache = (HashMap<String, BeanDefinition>) att.get("beanList");
        this.initNames = (ArrayList<String>) att.get("initBean");
        this.allNames = (ArrayList<String>) att.get("allNames");
    }
    /**获取XML输入流。*/
    private InputStream getXmlInputStream() throws ResourceException {
        try {
            if (this.xmlFile != null)
                return new FileInputStream(this.xmlFile);
            if (this.xmlURL != null)
                this.xmlURL.openConnection().getInputStream();
            if (this.xmlURI != null)
                this.xmlURI.toURL().openConnection().getInputStream();
            throw new NoDefinitionException("没有定义任何XML数据源信息。");
        } catch (IOException e) {
            throw new ResourceException("无法获取XML数据输入流，msg=" + e.getMessage());
        }
    }
    @Override
    public void clearCache() {
        this.staticCache.clear();
        this.dynamicCache.clear();
    }
    @Override
    public boolean containsBeanDefinition(String name) {
        return this.allNames.contains(name);
    }
    @Override
    public BeanDefinition getBeanDefinition(String name) throws InvokeException {
        try {
            if (this.staticCache.containsKey(name) == true)
                return this.staticCache.get(name);
            if (this.dynamicCache.containsKey(name) == true)
                return this.dynamicCache.get(name);
            BeanDefinition bean = (BeanDefinition) this.xmlEngine.runTask(this.getXmlInputStream(), "findBean", ".*", name);
            if (bean != null) {
                if (dynamicCacheNames.size() >= this.dynamicCacheSize)
                    this.dynamicCache.remove(dynamicCacheNames.removeFirst());
                else
                    this.dynamicCache.put(bean.getName(), bean);//缓存
            }
            return bean;
        } catch (Exception e) {
            throw new InvokeException("执行findBean任务期间发生异常。", e);
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getBeanDefinitionNames() {
        return (List<String>) this.allNames.clone();
    }
    @Override
    public String getResourceDescription() {
        return resourceDescription;
    }
    @Override
    public File getSourceFile() {
        return this.xmlFile;
    }
    @Override
    public String getSourceName() {
        if (this.xmlFile != null)
            return this.xmlFile.getName();
        if (this.xmlURL != null)
            this.xmlURL.getFile();
        if (this.xmlURI != null)
            this.xmlURI.getPath();
        return null;
    }
    @Override
    public URI getSourceURI() {
        return this.xmlURI;
    }
    @Override
    public URL getSourceURL() {
        return this.xmlURL;
    }
    @Override
    public List<String> getStrartInitBeanDefinitionNames() {
        return initNames;
    }
    @Override
    public boolean isCacheBeanMetadata() {
        return true;
    }
    /**
     * 测试某名称Bean是否为工厂模式创建，如果目标bean不存在则返回false。
     * @param name 要测试的Bean名称。
     * @return 返回测试结果，如果是以原型模式创建则返回true,否则返回false。
     */
    @Override
    public boolean isFactory(String name) throws InvokeException {
        try {
            if (this.staticCache.containsKey(name) == true)
                return (this.staticCache.get(name).getCreateType() == CreateTypeEnum.Factory) ? true : false;
            if (this.dynamicCache.containsKey(name) == true)
                return (this.dynamicCache.get(name).getCreateType() == CreateTypeEnum.Factory) ? true : false;
            BeanDefinition bean = (BeanDefinition) this.xmlEngine.runTask(this.getXmlInputStream(), "findBean", ".*", name);
            if (bean == null)
                return false;
            return (bean.getCreateType() == CreateTypeEnum.Factory) ? true : false;
        } catch (Exception e) {
            throw new InvokeException("执行findBean任务期间发生异常。", e);
        }
    }
    @Override
    public boolean isPrototype(String name) {
        return !isSingleton(name);
    }
    @Override
    public boolean isSingleton(String name) {
        try {
            String str = (String) this.xmlEngine.runTask(this.getXmlInputStream(), "getAttribute", ".*", name, "singleton");
            return str.equals("true") ? true : false;
        } catch (Exception e) {
            throw new InvokeException("执行getAttribute任务期间发生异常。", e);
        }
    }
}