package com.cloud.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.cloud.utils.exep.IllegalDBStructureException;
import com.cloud.utils.exep.UserSQLException;


/**
 * Производит всю работу с БД
 * @author prozorova 10.10.2018
 */
public class DatabaseQueriesProcessor {
	
	private Connection connection;
    private Statement stmt;
    
    // корневая папка сервера
    private static final String ROOT_DIRECTORY = "/Server/";
    
    // проверка логина нового пользователя
    private static final String QUERY_CHECK_NEW_USERNAME = "SELECT user_id " +
    		                                               "FROM users " + 
    		                                               "WHERE user_login = '%s';";
    // регистрация нового пользователя
    private static final String QUERY_NEW_USER_REGISTRATION = "INSERT INTO users (user_login, user_password) " +
                                                              "VALUES ('%s','%s');";
    // проверка данных аутентификации
    private static final String QUERY_CHECK_USER_AUTH = "SELECT user_id " +
                                                        "FROM users " + 
                                                        "WHERE user_login = '%s' " +
                                                        "AND user_password = '%s';";
    // получение пути к личному ящику пользователя
    private static final String QUERY_USER_PRIVATE_BOX_PATH = "SELECT b.box_path " +
                                                              "FROM users_boxes b " + 
                                                              "JOIN users u " + 
                                                              "ON b.user_id = u.user_id " +
                                                              "WHERE u.user_login = '%s';";
    // добавить личный ящик новому пользователю
    private static final String QUERY_NEW_BOX_REGISTRATION = "INSERT INTO users_boxes (user_id, box_path) " +
                                                             "VALUES (%d,'%s');";
	
    private static final Logger logger = Logger.getLogger(DatabaseQueriesProcessor.class);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private static final DatabaseQueriesProcessor INSTANCE = new DatabaseQueriesProcessor();
	
	private DatabaseQueriesProcessor() {
		initialize();
	}
	
	public static DatabaseQueriesProcessor getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Регистрация нового пользователя
	 * @param login логин
	 * @param pass пароль
	 * @return результат операции
	 * @throws SQLException
	 * @throws IOException 
	 */
	public boolean addNewUser(String login, String  pass) throws SQLException, IOException {
		
		lock.readLock().lock();
		logger.debug("New user registration : " + login);
		
		try (ResultSet rs = stmt.executeQuery(String.format(QUERY_CHECK_NEW_USERNAME, login))) {
			// проверка: есть ли в БД пользователь с таким логином
			if (rs.next()) {
				logger.debug("   This username already exists");
				return false;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			lock.readLock().unlock();
		}
		
        // регистрация нового пользователя
		lock.writeLock().lock();
		try {
			// добавляем пользователя
			stmt.execute(String.format(QUERY_NEW_USER_REGISTRATION, login, pass));
			try (ResultSet rs = stmt.executeQuery(String.format(QUERY_CHECK_NEW_USERNAME, login))) {
				rs.next();
				int id = rs.getInt("user_id");
				
				// привязываем к учетке пользователя личный ящик
				Path path = Paths.get(ROOT_DIRECTORY + login);
				if (Files.exists(path)) {
					String rename = ROOT_DIRECTORY + login + System.currentTimeMillis();
					Files.move(path, Paths.get(rename));
					logger.error(path + " directory already exists, renamed to " + rename);
				}
				Files.createDirectory(path);
				stmt.execute(String.format(QUERY_NEW_BOX_REGISTRATION, id, ROOT_DIRECTORY + login));
			}
			
			logger.debug("   New user registration is successful");
			return true;
		} finally {
			lock.writeLock().unlock();
		}
    }
	
	/**
	 * Проверка данных аутентификации
	 * @param login логин
	 * @param pass пароль
	 * @return путь к личной папке пользователя
	 * @throws SQLException
	 */
	public String checkUserAuth (String login, String  pass) throws SQLException {
		
		lock.readLock().lock();
		logger.debug("User authentification : " + login);
		
		try (ResultSet rs = stmt.executeQuery(String.format(QUERY_CHECK_USER_AUTH, login, pass))) {
			if (rs.next()) {
				logger.debug("   Authentification is successful.");
				return this.getPath(login);
			} else {
				logger.debug("   Authentification is failed");
				throw new UserSQLException("No user with such login/password");
			}
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Получить путь к папке пользователя на сервере
	 * @param login идентификатор пользователя
	 * @return путь к личной папке
	 * @throws SQLException 
	 */
	public String getPath(String login) throws SQLException {
		
		lock.readLock().lock();
		logger.debug("Getting private storage: " + login);
		
		try (ResultSet rs = stmt.executeQuery(String.format(QUERY_USER_PRIVATE_BOX_PATH, login))) {
			if (rs.next()) {
				String path = rs.getString("box_path");
				logger.debug("   path found: " + path);
				return path;
			} else
				throw new IllegalDBStructureException("   no private storage found for user " + login);

		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void initialize() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:MainDataBase.db");
	        stmt = connection.createStatement();
	        logger.debug("Database initialization comlete: " + connection.isClosed());
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("Connection to database is failed: " + e.getMessage(), e);
		}
	}
	
	private void disconnect() throws SQLException {
		if (stmt != null && !stmt.isClosed())
			stmt.close();
		if (connection != null && !connection.isClosed())
			connection.close();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			this.disconnect();
		} finally {
			super.finalize();
		}
	}

}
