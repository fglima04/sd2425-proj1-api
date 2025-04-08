package fctreddit.impl.clients.rest;

import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import fctreddit.impl.discovery.Discovery;

public class RestImageClient {

	private static final String SERVICE_NAME = "ImageService";
	private final Discovery discovery;
	private URI serverURI;
	private final Client client;

	public RestImageClient(Discovery discovery) {
		this.discovery = discovery;
		this.serverURI = resolveServerURI();
		this.client = ClientBuilder.newClient();
	}

	private URI resolveServerURI() {
		URI[] uris = discovery.knownUrisOf(SERVICE_NAME, 1);
		if (uris.length == 0) {
			throw new RuntimeException("No available servers for service: " + SERVICE_NAME);
		}
		return uris[0];
	}

	public String uploadImage(byte[] imageContents) {
		return client.target(serverURI).path("/image")
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM), String.class);
	}
}
