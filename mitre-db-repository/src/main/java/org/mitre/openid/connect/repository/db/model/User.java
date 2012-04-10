/****************************************************************************************
 *  User.java
 *
 *  Created: Jul 8, 2010
 *
 *  @author DRAND
 *
 *  (C) Copyright MITRE Corporation 2010
 *
 *  The program is provided "as is" without any warranty express or implied, including
 *  the warranty of non-infringement and the implied warranties of merchantibility and
 *  fitness for a particular purpose.  The Copyright owner will not be liable for any
 *  damages suffered by you as a result of using the Program.  In no event will the
 *  Copyright owner be liable for any special, indirect or consequential damages or
 *  lost profits even if the Copyright owner has been advised of the possibility of
 *  their occurrence.
 *
 ***************************************************************************************/
package org.mitre.openid.connect.repository.db.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a single user to the system. Each user is identified by their 
 * user id. The original password is never stored. Instead a salted hash of 
 * the original password is stored in the database. If the user forgets their
 * original password the system will need to reset the password, sending them
 * a replacement password via their confirmed email address.
 * 
 * @author DRAND
 *
 */
@Entity
@Table(name = "USERS")
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "USER_ID")
	private Long id;
	
	@Basic
	@Column(name = "USERNAME", length = 48, nullable = false, unique = true)
	private String username;
	
	@Basic
	@Column(name = "PASSWORD_SALT")
	private Integer passwordSalt;
	
	@Basic
	@Column(name = "PASSWORD_HASH", length = 128, nullable = false)
	private String passwordHash;
	
	@Basic
	@Column(name = "EMAIL", length = 128)
	private String email;
	
	@Basic
	@Column(name = "CONFIRMED")
	private Boolean emailConfirmed = false;
	
	@Basic
	@Column(name = "CONFIRMATION_HASH", length = 128, nullable = true)
	private String confirmationHash;
	
	@Basic
	@Column(name = "FAILED_ATTEMPTS")
	private Integer failedAttempts = 0;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<Role> roles = new HashSet<Role>();
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the passwordSalt
	 */
	public Integer getPasswordSalt() {
		return passwordSalt;
	}

	/**
	 * @param passwordSalt the passwordSalt to set
	 */
	public void setPasswordSalt(Integer passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	/**
	 * @return the passwordHash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @param passwordHash the passwordHash to set
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the emailConfirmed
	 */
	public Boolean getEmailConfirmed() {
		return emailConfirmed;
	}

	/**
	 * @param emailConfirmed the emailConfirmed to set
	 */
	public void setEmailConfirmed(Boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}

	/**
	 * @return the confirmationHash
	 */
	public String getConfirmationHash() {
		return confirmationHash;
	}

	/**
	 * @param confirmationHash the confirmationHash to set
	 */
	public void setConfirmationHash(String confirmationHash) {
		this.confirmationHash = confirmationHash;
	}

	/**
	 * @return the failedAttempts
	 */
	public Integer getFailedAttempts() {
		return failedAttempts;
	}

	/**
	 * @param failedAttempts the failedAttempts to set
	 */
	public void setFailedAttempts(Integer failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	/**
	 * @return the roles
	 */
	public Set<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return username.hashCode();
	}
}
