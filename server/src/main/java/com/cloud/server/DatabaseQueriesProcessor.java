package com.cloud.server;

/**
 * Производит всю работу с БД
 * @author prozorova 10.10.2018
 */
public class DatabaseQueriesProcessor {
	
	// TODO заглушка - убрать
	private static final String DIR_FOR_TESTING_SERVER = "/Users/prozorova/Documents/icons/222";
	
	/**
	 * Получить путь к папке пользователя на сервере
	 * @param login идентификатор пользователя
	 * @return путь к личной папке
	 */
	public static synchronized String getPath(String login) {
		// TODO заппрос к БД
		
		return DIR_FOR_TESTING_SERVER;
	}

}
