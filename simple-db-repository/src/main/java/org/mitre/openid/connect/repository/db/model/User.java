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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
 
import org.eclipse.persistence.annotations.PrivateOwned;
import org.mitre.openid.connect.repository.UserManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

//** james adds
import java.io.*;
import java.security.MessageDigest;
import javax.mail.internet.MimeUtility;



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
@NamedQueries(value = {
		@NamedQuery(name = "users.by_first_name", 
			query = "select u from User u order by u.firstname"),
		@NamedQuery(name = "users.by_last_name", 
			query = "select u from User u order by u.lastname"),
		// FIXME: users.by_username is already taken by something that actually finds just one user
	    @NamedQuery(name = "users.sort_by_username", 
            query = "select u from User u order by u.username"),
		@NamedQuery(name = "users.by_email", 
			query = "select u from User u order by u.email"),
		@NamedQuery(name = "users.by_username",
			query = "select u from User u where u.username = :username"),
		@NamedQuery(name = "users.by_admin_role",
			query = "select u from User u inner join u.roles r where r.name = 'ADMIN'"),
		@NamedQuery(name = "users.like_name",
			query = "select u from User u where lower(u.username) like :pattern"),
		@NamedQuery(name = "users.all",
			query = "select u from User u"),
		@NamedQuery(name = "users.count",
			query = "select count(u) from User u"),
		@NamedQuery(name = "users.username",
			query = "select u from User u where " +
					"(lower(concat(u.firstname, ' ', u.lastname)) = :name and (u.middlename is null or length(trim(u.middlename)) = 0)) or " +
					"(lower(concat(u.firstname, ' ', u.middlename, ' ', u.lastname)) = :name)")
})
public class User implements UserDetails, Serializable {
	private Long id;
	private String username;
	
	private Integer passwordSalt;	
	private String passwordHash;
	private String jamesPasswordHash;
	private String email;
	private Boolean emailConfirmed = false;
	private String firstname;
	private String middlename;
	private String lastname;
	private String nickname;
	private String profile;
	private String picture;
	private String website;
	private String gender;
	private String zoneinfo;
	private String locale;
	private String phone;
	private String formattedAddress;
	private String street;
	private String locality;
	private String region;
	private String country;
	private String postalCode;
	private String confirmationHash;
	private Integer failedAttempts = 0;
	private Set<Role> roles = new HashSet<Role>();
	private Set<UserAttribute> attributes;
	private Date updated;
	
	private static SecureRandom random = new SecureRandom();
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "USER_ID")
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
	@Basic
	@Column(name = "USERNAME", length = 48, nullable = false, unique = true)
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
	@Basic
	@Column(name = "PASSWORD_SALT")
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
	@Basic
	@Column(name = "PASSWORD_HASH", length = 128, nullable = false)
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @return the jamesPasswordHash
	 */
	@Basic
	@Column(name = "JAMES_PASSWORD_HASH", length = 128, nullable = false)
	public String getJamesPasswordHash() {
		return jamesPasswordHash;
	}

	/**
	 * @param passwordHash the passwordHash to set
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}


	/**
	 * @param password is transformed into the jamesPasswordHash to set
	 */
	public void setJamesPasswordHash(String jamesPasswordHash) {
        this.jamesPasswordHash = jamesPasswordHash;
    }

	/**
	 * @return the email
	 */
	@Basic
	@Column(name = "EMAIL", length = 128)
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
	 * @return the firstname
	 */
    @Basic
    @Column(name = "FIRST_NAME", length = 48)
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * @return the middlename
	 */
	@Basic
    @Column(name = "MIDDLE_NAME", length = 48)
	public String getMiddlename() {
		return middlename;
	}

	/**
	 * @param middlename the middlename to set
	 */
	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	/**
	 * @return the lastname
	 */
	@Basic
    @Column(name = "LAST_NAME", length = 48)
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the nickname
	 */
	@Basic
    @Column(name = "NICKNAME", length = 48)
	public String getNickname() {
		return nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the profile
	 */
	@Basic
    @Column(name = "PROFILE", length = 512)
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the picture
	 */
	@Basic
	@Column(name = "PICTURE", length = 512)
	public String getPicture() {
		return picture;
	}

	/**
	 * @param picture the picture to set
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * @return the website
	 */
	@Basic
    @Column(name = "WEBSITE", length = 512)
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website the website to set
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	/**
	 * @return the gender
	 */
	@Basic
    @Column(name = "GENDER", length = 1)
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the zoneinfo
	 */
	@Basic
    @Column(name = "ZONEINFO", length = 16)
	public String getZoneinfo() {
		return zoneinfo;
	}

	/**
	 * @param zoneinfo the zoneinfo to set
	 */
	public void setZoneinfo(String zoneinfo) {
		this.zoneinfo = zoneinfo;
	}

	/**
	 * @return the locale
	 */
	@Basic
    @Column(name = "LOCALE", length = 64)
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the phone
	 */
	@Basic
    @Column(name = "PHONE_NUMBER", length = 32)
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the formattedAddress
	 */
	@Basic
	@Column(name = "FORMATTED_ADDRESS", length = 256)
	public String getFormattedAddress() {
		return formattedAddress;
	}

	/**
	 * @param formattedAddress the formattedAddress to set
	 */
	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}

	/**
	 * @return the street
	 */
	@Basic
    @Column(name = "STREET", length = 32)
	public String getStreet() {
		return street;
	}

	/**
	 * @param street the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * @return the locality
	 */
	@Basic
    @Column(name = "LOCALITY", length = 32)
	public String getLocality() {
		return locality;
	}

	/**
	 * @param locality the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}

	/**
	 * @return the region
	 */
	@Basic
    @Column(name = "REGION", length = 32)
	public String getRegion() {
		return region;
	}

	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the country
	 */
	@Basic
    @Column(name = "COUNTRY", length = 32)
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return the postalCode
	 */
	@Basic
	@Column(name = "POSTAL_CODE", length = 32)
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @param postalCode the postalCode to set
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @return the emailConfirmed
	 */
	@Basic
	@Column(name = "CONFIRMED")
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
	@Basic
	@Column(name = "CONFIRMATION_HASH", length = 128, nullable = true)
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
	@Basic
	@Column(name = "FAILED_ATTEMPTS")	
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
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "USERS_ROLES",
		joinColumns = {
			@JoinColumn(name="USER_ID")
		},
		inverseJoinColumns = {
			@JoinColumn(name="ROLE_ID")
		}
	)
	public Set<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	/**
	 * @return the attributes
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "USER_ID")
	@PrivateOwned
	public Set<UserAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<UserAttribute> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * @return the updated
	 */
	@Basic
	@Column(name = "UPDATED")
	public Date getUpdated() {
		return updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}


	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
	 */
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection authorities = new ArrayList<GrantedAuthority>();
		for(Role role : roles) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
		}
		return authorities;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
	 */
	public String getPassword() {
		return passwordHash;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
	 */
	public boolean isAccountNonExpired() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
	 */
	public boolean isAccountNonLocked() {
		return failedAttempts < 5;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime
				* result
				+ ((confirmationHash == null) ? 0 : confirmationHash.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((emailConfirmed == null) ? 0 : emailConfirmed.hashCode());
		result = prime * result
				+ ((failedAttempts == null) ? 0 : failedAttempts.hashCode());
		result = prime * result
				+ ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((jamesPasswordHash == null) ? 0 : jamesPasswordHash
						.hashCode());
		result = prime * result
				+ ((lastname == null) ? 0 : lastname.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result
				+ ((locality == null) ? 0 : locality.hashCode());
		result = prime * result
				+ ((middlename == null) ? 0 : middlename.hashCode());
		result = prime * result
				+ ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result
				+ ((passwordHash == null) ? 0 : passwordHash.hashCode());
		result = prime * result
				+ ((passwordSalt == null) ? 0 : passwordSalt.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((picture == null) ? 0 : picture.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
		result = prime * result
				+ ((zoneinfo == null) ? 0 : zoneinfo.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (confirmationHash == null) {
			if (other.confirmationHash != null)
				return false;
		} else if (!confirmationHash.equals(other.confirmationHash))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (emailConfirmed == null) {
			if (other.emailConfirmed != null)
				return false;
		} else if (!emailConfirmed.equals(other.emailConfirmed))
			return false;
		if (failedAttempts == null) {
			if (other.failedAttempts != null)
				return false;
		} else if (!failedAttempts.equals(other.failedAttempts))
			return false;
		if (firstname == null) {
			if (other.firstname != null)
				return false;
		} else if (!firstname.equals(other.firstname))
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (jamesPasswordHash == null) {
			if (other.jamesPasswordHash != null)
				return false;
		} else if (!jamesPasswordHash.equals(other.jamesPasswordHash))
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		} else if (!lastname.equals(other.lastname))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		if (locality == null) {
			if (other.locality != null)
				return false;
		} else if (!locality.equals(other.locality))
			return false;
		if (middlename == null) {
			if (other.middlename != null)
				return false;
		} else if (!middlename.equals(other.middlename))
			return false;
		if (nickname == null) {
			if (other.nickname != null)
				return false;
		} else if (!nickname.equals(other.nickname))
			return false;
		if (passwordHash == null) {
			if (other.passwordHash != null)
				return false;
		} else if (!passwordHash.equals(other.passwordHash))
			return false;
		if (passwordSalt == null) {
			if (other.passwordSalt != null)
				return false;
		} else if (!passwordSalt.equals(other.passwordSalt))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (picture == null) {
			if (other.picture != null)
				return false;
		} else if (!picture.equals(other.picture))
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (updated == null) {
			if (other.updated != null)
				return false;
		} else if (!updated.equals(other.updated))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (website == null) {
			if (other.website != null)
				return false;
		} else if (!website.equals(other.website))
			return false;
		if (zoneinfo == null) {
			if (other.zoneinfo != null)
				return false;
		} else if (!zoneinfo.equals(other.zoneinfo))
			return false;
		return true;
	}

	//** GG: James mail userdb add
    //** encode a passowrd into the needed encoding for the james email server
    //**
    public String encodeJamesPasswordHash(String pass) {
        MessageDigest md;
        ByteArrayOutputStream bos;
        String encoded_password = "";

        //System.out.println("here in encodeJamesPasswordHash !! ..." + pass + "...");
        if (pass == null || pass.trim().length() == 0) {
            //System.out.println("encodeJamesPasswordHash - null/blank password, returning...");
            return null;
        }

        try {
            md = MessageDigest.getInstance("SHA");
            byte[] digest = md.digest(pass.getBytes("iso-8859-1"));

            bos = new ByteArrayOutputStream();
            OutputStream encodedStream = MimeUtility.encode(bos, "base64");

            encodedStream.write(digest);
            encoded_password = bos.toString("iso-8859-1");
            //System.out.println("encoded pwd1:" + encoded_password);
            
        } catch (Exception e) {
            System.out.println("encodeJamesPasswordHash - fatal error: " + e);
        }    

        //System.out.println("encoded pwd2:" + encoded_password);
        return encoded_password; 
    }


}
