package com.alibaba.csp.sentinel.transport.dubbo;

import org.junit.Test;

import com.alibaba.csp.sentinel.transport.dubbo.server.UserService;
import com.alibaba.csp.sentinel.transport.dubbo.server.UserServiceImpl;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

public class DubboTest {

	@Test
	public void dubboTest() {
		
		producer();
		UserService userServiceConsumer = consumer();
		
		
		try {
			userServiceConsumer.getUserName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void producer() {
		UserService userService = new UserServiceImpl();
		
		ApplicationConfig application = new ApplicationConfig();
		application.setName("producer");
		
		ProtocolConfig protocol = new ProtocolConfig();
		protocol.setName("dubbo");
		protocol.setPort(20880);
		
		RegistryConfig registryConfig = new RegistryConfig();
		registryConfig.setRegister(false);
		
		ProviderConfig provider = new ProviderConfig();
		provider.setFilter("sentinel.dubbo.provider.filter");
		
		ServiceConfig<UserService> service = new ServiceConfig<UserService>();

		service.setProvider(provider);
		service.setApplication(application);
		service.setProtocol(protocol); // 多个协议可以用setProtocols()
		service.setRegistry(registryConfig);
		service.setInterface(UserService.class);
		service.setRef(userService);
		service.setVersion("1.0.0");
		service.setRegister(false);
		
		
		// 暴露及注册服务
		service.export();
	}
	public UserService consumer() {
		ApplicationConfig application = new ApplicationConfig();
		application.setName("consumer");
		 
		// 连接注册中心配置
		RegistryConfig registry = new RegistryConfig();
		registry.setRegister(false);

		 
		// 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接
		 
		// 引用远程服务
		ReferenceConfig<UserService> reference = new ReferenceConfig<UserService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
		reference.setApplication(application);
		reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
		reference.setInterface(UserService.class);
		reference.setVersion("1.0.0");
		reference.setUrl("dubbo://localhost:20880");
		//reference.setInjvm(true);
		//reference.setScope("local");
		//reference.setGeneric(false);
		//reference.setCheck(false);
		
		 
		// 和本地bean一样使用xxxService
		return reference.get(); // 注意：此代
	}
}
