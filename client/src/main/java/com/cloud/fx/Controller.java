package com.cloud.fx;

public abstract class Controller {
	
	// менеджер переключения экранов
	private static SceneManager manager;
	
	
	public static SceneManager getSceneManager() {
		return manager;
	}
	
	static void setSceneManager(SceneManager sceneManager) {
		manager = sceneManager;
	}

}
