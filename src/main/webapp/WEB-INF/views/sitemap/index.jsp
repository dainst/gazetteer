<%@ page session="false" %><% response.setHeader("Content-Type", "text/xml"); %><?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<% 
Long no = (Long) request.getAttribute("no");
for(int i=0; i < no.intValue(); i++) { 
%>
	<sitemap>
		<loc>${baseUri}/sitemap<%= i+1 %>.xml</loc>
	</sitemap>
<% } %>
</sitemapindex>