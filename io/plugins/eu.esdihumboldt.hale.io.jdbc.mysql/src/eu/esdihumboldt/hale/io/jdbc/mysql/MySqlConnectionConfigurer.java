package eu.esdihumboldt.hale.io.jdbc.mysql;

import com.mysql.cj.jdbc.JdbcConnection;

import eu.esdihumboldt.hale.io.jdbc.extension.ConnectionConfigurer;

public class MySqlConnectionConfigurer implements ConnectionConfigurer<JdbcConnection> {

	@Override
	public void configureConnection(JdbcConnection connection) {
		// TODO Auto-generated method stub
		//Eintr�ge in Extension erg�nzen - Vorbild in PostgreSQL
		//checken ob verf�gbar (und breakpoint greift)
	}
	
}
