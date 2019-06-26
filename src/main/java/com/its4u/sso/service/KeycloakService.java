package com.its4u.sso.service;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * Service using Keycloak Admin REST client to call Red Hat SSO 7.3
 *
 * @author jerome.cristante
 *
 */
public class KeycloakService {

	/**
	 * Keycloak URL.
	 */
	private static final String KEYCLOAK_URL = "http://localhost:8180/auth";

	/**
	 * Keycloak admin username.
	 */
	private static final String KEYCLOAK_ADMIN_USERNAME = "admin";

	/**
	 * Keycloak admin password.
	 */
	private static final String KEYCLOAK_ADMIN_PASSWORD = "admin";

	/**
	 * Realm name.
	 */
	private static final String REALM_NAME = "Capitalisation";

	/**
	 * Keycloak client.
	 */
	private Keycloak getInstance() {
		return Keycloak.getInstance(KEYCLOAK_URL, "master", KEYCLOAK_ADMIN_USERNAME, KEYCLOAK_ADMIN_PASSWORD, "admin-cli");
	}

	/**
	 * Enumeration of possible reset actions used in Keycloak.
	 */
	public enum Action {
		UPDATE_PASSWORD, UPDATE_PROFILE, VERIFY_EMAIL, CONFIGURE_TOTP
	}

	/**
	 * Retrieve a user based on its keycloak ID.
	 */
	private UserRepresentation retrieveUserRepresentationById(String keycloakId) {
		return this.getInstance().realm(REALM_NAME).users().get(keycloakId).toRepresentation();
	}

	/**
	 * Retrieve a user based on its login.
	 */
	public UserRepresentation retrieveUserRepresentationByLogin(String login) {
		final List<UserRepresentation> search = getInstance().realm(REALM_NAME).users().search(login, 0, 1);
		if (search.isEmpty()) {
			return null;
		}
		return search.get(0);
	}

	/**
	 * Enable a user account.
	 */
	public void enableAccount(String keycloakId) {
		UserRepresentation userRepresentation = retrieveUserRepresentationById(keycloakId);
		userRepresentation.setEnabled(true);
		this.getInstance().realm(REALM_NAME).users().get(keycloakId).update(userRepresentation);
	}

	/**
	 * Disable a user account.
	 */
	public void disableAccount(String keycloakId) {
		UserRepresentation userRepresentation = retrieveUserRepresentationById(keycloakId);
		userRepresentation.setEnabled(false);
		this.getInstance().realm(REALM_NAME).users().get(keycloakId).update(userRepresentation);
	}

	/**
	 * Create a user account.
	 * 
	 * @param login
	 * @param lastname
	 * @param firstname
	 * @param email
	 */
	public void createAccount(final String login, final String lastname, final String firstname, final String email) {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(login);
		userRepresentation.setLastName(lastname);
		userRepresentation.setFirstName(firstname);
		userRepresentation.setEmail(email);
		userRepresentation.setEnabled(true);

		List<String> actions = Arrays.asList(Action.UPDATE_PASSWORD.name(), Action.VERIFY_EMAIL.name());
		userRepresentation.setRequiredActions(actions);

		Response response = getInstance().realm(REALM_NAME).users().create(userRepresentation);

		if (response.getStatus() != 201) {
			throw new RuntimeException("Unable to create user on keycloak (status " + response.getStatus() + ")");
		}

		// Retrieve the id of the created keycloak account
		UserRepresentation createdUserRepresentation = retrieveUserRepresentationByLogin(userRepresentation.getUsername());

		if (createdUserRepresentation == null) {
			throw new RuntimeException("Unable to find newly created user [" + userRepresentation.getUsername() + "]");
		}
	}

}
