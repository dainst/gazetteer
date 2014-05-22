package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = getUserRepository().findByUsername(username);
		
		if (user == null)
			throw new UsernameNotFoundException("Username " + username + " does not exist");
			
		return user;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}


	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}	
}
