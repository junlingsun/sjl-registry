package com.junling.registry.client;

import com.junling.registry.common.entity.Service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RegistryClientApplicationTests {

	@Test
	void registryClientTest() throws InterruptedException {
		RegistryClient registryClient = new RegistryClient("http://localhost", 8080);
		Service service1 = new Service("name1", "address1");
		Service service2 = new Service("name2", "address2");
		List<Service> serviceList = new ArrayList<>();
		serviceList.add(service1);
		serviceList.add(service2);
		registryClient.register(serviceList);

		TimeUnit.SECONDS.sleep(2);

		Set<String> set = new TreeSet<>();
		set.add("name1");
		set.add("name2");
		Map<String, TreeSet<String>> discover = registryClient.discover(set);
		System.out.println("discover>>>>>>>>>>>>>>" + discover);

//		 //remove test
//		System.out.println("remove:" + registryClient.remove(new HashSet<>(serviceList)));
//		TimeUnit.SECONDS.sleep(2);

		// monitor test
		TimeUnit.SECONDS.sleep(10);
		long start = System.currentTimeMillis();
		System.out.println("monitor start...." + start);
		registryClient.monitor(set);
		long end = System.currentTimeMillis();
		System.out.println("monitor complete... " + (end-start));


		while (true) {
			try {
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


	}

}
