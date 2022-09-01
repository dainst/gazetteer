package org.dainst.gazetteer.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.helpers.LanguagesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class AppController {
	
	private static Logger logger = LoggerFactory.getLogger(AppController.class);
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;
	
	@Value("${idTypes}")
	private String[] idTypes;
	
	@Value("${placeTypes}")
	private String[] placeTypes;
	
	@Value("${placeTypeGroups}")
	private String[] placeTypeGroups;
	
	@Value("${placeTypeGroupIds}")
	private int[] placeTypeGroupIds;
	
	@Value("${version}")
	private String version;
	
	@Autowired
	private RecordGroupRepository recordGroupDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	LanguagesHelper langHelper;
	@RequestMapping(value="/app/")
	public String app(ModelMap model, HttpServletRequest request, 
			@RequestParam(value="_escaped_fragment_", required=false) String fragment) throws UnsupportedEncodingException {
		
		// render static html for crawlers
		if (fragment != null && !fragment.isEmpty() && fragment.startsWith("/show")) {
			String[] split = URLDecoder.decode(fragment, "UTF-8").split("/");
			return "forward:/doc/" + split[2] + ".html";
		}

		Arrays.sort(idTypes, String.CASE_INSENSITIVE_ORDER);

		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		model.addAttribute("googleMapsApiKey", googleMapsApiKey);
		model.addAttribute("idTypes", idTypes);
		model.addAttribute("placeTypes", placeTypes);
		model.addAttribute("placeTypeGroups", placeTypeGroups);
		model.addAttribute("placeTypeGroupIds", placeTypeGroupIds);
		model.addAttribute("version", version);
		logger.debug("accept: {}", request.getHeader("Accept"));
		return "app/index";
	}
	
	@RequestMapping(value="/app/{view}.html")
	public String app(@PathVariable String view, ModelMap model, HttpServletRequest request) {		
		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		model.addAttribute("googleMapsApiKey", googleMapsApiKey);
		model.addAttribute("idTypes", idTypes);
		model.addAttribute("placeTypes", placeTypes);
		model.addAttribute("placeTypeGroups", placeTypeGroups);
		model.addAttribute("placeTypeGroupIds", placeTypeGroupIds);
		return "app/" + view;
	}
	
	@RequestMapping(value="/app/partials/{view}.html")
	public String appPartials(@PathVariable String view, ModelMap model, HttpServletRequest request) {
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal != null && principal instanceof User) {
			user = (User) principal;
		
			List<RecordGroup> recordGroups = new ArrayList<RecordGroup>();
			List<RecordGroup> editRecordGroups = new ArrayList<RecordGroup>();
			List<GroupRole> groupRoles = groupRoleDao.findByUserId(user.getId());
			for (GroupRole groupRole : groupRoles) {
				RecordGroup group = recordGroupDao.findById(groupRole.getGroupId()).orElse(null);
				if (groupRole.getRoleType().equals("admin") || groupRole.getRoleType().equals("edit") || groupRole.getRoleType().equals("read"))
					recordGroups.add(group);
				if (groupRole.getRoleType().equals("admin") || groupRole.getRoleType().equals("edit"))
					editRecordGroups.add(group);
			}
			
			model.addAttribute("recordGroups", recordGroups.toArray());
			model.addAttribute("editRecordGroups", editRecordGroups.toArray());
		}
		
		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		model.addAttribute("idTypes", idTypes);
		model.addAttribute("placeTypes", placeTypes);
		model.addAttribute("placeTypeGroups", placeTypeGroups);
		model.addAttribute("placeTypeGroupIds", placeTypeGroupIds);
		return "app/partials/" + view;
	}
	
}
