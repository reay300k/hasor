/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.rsf.runtime;
import net.hasor.core.Settings;
import net.hasor.rsf.metadata.ServiceMetaData;
import net.hasor.rsf.register.RegisterCenter;
import net.hasor.rsf.serialize.SerializeFactory;
/**
 * RSF 环境。
 * @version : 2014年11月18日
 * @author 赵永春(zyc@hasor.net)
 */
public interface RsfContext {
    /**获取注册中心。*/
    public RegisterCenter getRegisterCenter();
    /**根据服务名获取服务描述。*/
    public ServiceMetaData getService(String serviceName);
    //
    /**获取默认超时时间。*/
    public int getDefaultTimeout();
    /**获取配置*/
    public Settings getSettings();
    /**获取当发起请求的时候所使用的RSF协议版本。*/
    public byte getVersion();
    /**获取序列化管理器。*/
    public SerializeFactory getSerializeFactory();
    //
    /**获取配置的服务器端选项*/
    public RsfOptionSet getServerOption();
    /**获取配置的客户端选项*/
    public RsfOptionSet getClientOption();
}