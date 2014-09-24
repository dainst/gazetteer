package org.dainst.gazetteer.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Document
public class User implements UserDetails {

	private static final long serialVersionUID = -5949117611306099791L;
	
	@Id
	private String id;
	
	private String username;
	private String firstname;
	private String lastname;
	private String institution;
	private String password;
	private String email;
	private Date registrationDate;
	private Date lastLogin;
	private boolean enabled;

	private List<GrantedAuthority> authorities;
	private Set<String> recordGroupIds = new HashSet<String>();
	

	public User(String username, String firstname, String lastname, String institution,
				String email, String password, Date registrationDate, List<GrantedAuthority> authorities) {
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.institution = institution;
		this.email = email;
		this.password = password;
		this.setRegistrationDate(registrationDate);
		this.authorities = authorities;
		this.enabled = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}
	
	public String getRegistrationDateAsText() {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
		
		if (registrationDate == null)
			return "-";
		
		return dateFormat.format(registrationDate);
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
		
	public Date getLastLogin() {
		return lastLogin;
	}

	public String getLastLoginAsText() {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
		
		if (lastLogin == null)
			return "-";
		
		return dateFormat.format(lastLogin);
	}
	
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}
	
	public boolean hasRole(String role) {
	
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().equals(role))
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public Set<String> getRecordGroupIds() {
		if (recordGroupIds == null)
			recordGroupIds = new HashSet<String>();
			
		return recordGroupIds;
	}

	public void setRecordGroupIds(Set<String> recordGroupIds) {
		this.recordGroupIds = recordGroupIds;
	}

	public static class UsernameComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			return user1.getUsername().toLowerCase().compareTo(user2.getUsername().toLowerCase());
		}
	}

	public static class FirstnameComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			return user1.getFirstname().toLowerCase().compareTo(user2.getFirstname().toLowerCase());
		}
	}
	
	public static class LastnameComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			return user1.getLastname().toLowerCase().compareTo(user2.getLastname().toLowerCase());
		}
	}
	
	public static class InstitutionComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			return user1.getInstitution().toLowerCase().compareTo(user2.getInstitution().toLowerCase());
		}
	}
	
	public static class EmailComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			return user1.getEmail().toLowerCase().compareTo(user2.getEmail().toLowerCase());
		}
	}
	
	public static class AdminComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			if (user1.hasRole("ROLE_ADMIN") == user2.hasRole("ROLE_ADMIN")) 
				return 0;
			else if (user1.hasRole("ROLE_ADMIN"))
				return -1;
			else
				return 1;			
		}
	}
	
	public static class EditorComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			if (user1.hasRole("ROLE_EDITOR") == user2.hasRole("ROLE_EDITOR")) 
				return 0;
			else if (user1.hasRole("ROLE_EDITOR"))
				return -1;
			else
				return 1;			
		}
	}
	
	public static class ReisestipendiumComparator implements Comparator<User> {
		public int compare(User user1, User user2) {
			if (user1.hasRole("ROLE_REISESTIPENDIUM") == user2.hasRole("ROLE_REISESTIPENDIUM")) 
				return 0;
			else if (user1.hasRole("ROLE_REISESTIPENDIUM"))
				return -1;
			else
				return 1;			
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null && other.id != null)
			return false;
		if (id != null && !id.equals(other.id))
			return false;
		if (username == null && other.username != null)
			return false;
		if (username != null && !username.equals(other.username))
			return false;
		if (firstname == null && other.firstname != null)
			return false;
		if (firstname != null && !firstname.equals(other.firstname))
			return false;
		if (lastname == null && other.lastname != null)
			return false;
		if (lastname != null && !lastname.equals(other.lastname))
			return false;
		if (institution == null && other.institution != null)
			return false;
		if (institution != null && !institution.equals(other.institution))
			return false;
		if (password == null && other.password != null)
			return false;
		if (password != null && !password.equals(other.password))
			return false;
		if (email == null && other.email != null)
			return false;
		if (email != null && !email.equals(other.email))
			return false;
		if (registrationDate == null && other.registrationDate != null)
			return false;
		if (registrationDate != null && !registrationDate.equals(other.registrationDate))
			return false;
		if (lastLogin == null && other.lastLogin != null)
			return false;
		if (lastLogin != null && !lastLogin.equals(other.lastLogin))
			return false;
		if (enabled != other.enabled)
			return false;
		if (authorities == null && other.authorities != null)
			return false;
		if (authorities != null && !authorities.equals(other.authorities))
			return false;
		if (recordGroupIds == null && other.recordGroupIds != null)
			return false;
		if (recordGroupIds != null && !recordGroupIds.equals(other.recordGroupIds))
			return false;
		return true;
	}
}