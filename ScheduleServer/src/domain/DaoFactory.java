package domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

	@Bean
	public ConnectionManager connectionManager() {
		return new ConnectionManager();
	}
	
	@Bean
	public UserDAO userDao() {
		UserDAO userDao = new UserDAO();
		return userDao;
	}
	
	@Bean
	public ScheduleDAO scheduleDao() {
		ScheduleDAO scheduleDao = new ScheduleDAO();
		return scheduleDao;
	}
}
