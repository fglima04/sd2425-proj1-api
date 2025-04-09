package fctreddit.impl.servers.java;

import java.util.List;
import java.util.logging.Logger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.servers.persistence.Hibernate;
import fctreddit.impl.clients.rest.RestImageClient;
	
public class JavaContent {

    private static Logger Log = Logger.getLogger(JavaContent.class.getName());

	private Hibernate hibernate;
	private RestImageClient imageClient;
	
	public JavaContent() {
		hibernate = Hibernate.getInstance();
	}
    
}
