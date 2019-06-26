package com.its4u.sso.service;

import org.keycloak.representations.idm.UserRepresentation;

/**
 * Simple class with main method to test Keycloak Admin REST client.
 *
 * @author jerome.cristante
 *
 */
public class KeycloakServiceRun {

	public static void main(String[] args) {

		final KeycloakService keycloakService = new KeycloakService();

		UserRepresentation user = keycloakService.retrieveUserRepresentationByLogin("test");
		keycloakService.enableAccount(user.getId());

		keycloakService.createAccount("john.doe", "Doe", "John", "john.doe@its4u.com");

	}

}
