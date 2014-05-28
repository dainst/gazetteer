package org.dainst.gazetteer.helpers;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.UserPasswordChangeRequestRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.domain.UserPasswordChangeRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class AuthenticationSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler {

	private UserRepository userRepository;
	private UserPasswordChangeRequestRepository userPasswordChangeRequestRepository;
	
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws javax.servlet.ServletException, IOException {
		
		User user = (User) authentication.getPrincipal();
		user.setLastLogin(new Date());
		userRepository.save(user);
		
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestRepository.findByUserId(user.getId());
		if (changeRequest != null)
	    	userPasswordChangeRequestRepository.delete(changeRequest);
		
		super.onAuthenticationSuccess(request, response, authentication);
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserPasswordChangeRequestRepository getUserPasswordChangeRequestRepository() {
		return userPasswordChangeRequestRepository;
	}

	public void setUserPasswordChangeRequestRepository(UserPasswordChangeRequestRepository userPasswordChangeRequestRepository) {
		this.userPasswordChangeRequestRepository = userPasswordChangeRequestRepository;
	}
}
