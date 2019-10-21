package com.taobao.diamond.maintenance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.impl.DiamondEnv;

/**
 * ����״̬��Ϣ
 * 
 * @author Diamond
 *
 */
public class DiamondHealth {

	private static Map<String, Boolean> env2Health = new HashMap<String, Boolean>();

	public static String getHealth() {

		boolean isHealth = true;
		List<DiamondEnv> diamondEnvs = Diamond.allDiamondEnvs();
		for (DiamondEnv diamondEnv : diamondEnvs) {
			boolean isHealthServer = diamondEnv.getWorker().isHealthServer();
			String envName = diamondEnv.getName();
			env2Health.put(envName, isHealthServer);
			if (!isHealthServer) {
				isHealth = false;
			}
		}

		if (isHealth) {
			return "UP";
		} else {
			String envHealth = env2Health.toString();
			return "DOWN(" + envHealth + ")";
		}

	}
}
