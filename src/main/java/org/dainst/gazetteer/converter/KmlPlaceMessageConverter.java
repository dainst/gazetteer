package org.dainst.gazetteer.converter;

import java.io.IOException;

import org.dainst.gazetteer.domain.Place;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class KmlPlaceMessageConverter extends AbstractHttpMessageConverter<Place> {
	
	private String baseUri;

	public KmlPlaceMessageConverter() {
		// TODO Auto-generated constructor stub
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	@Override
	protected Place readInternal(Class<? extends Place> arg0,
			HttpInputMessage arg1) throws IOException,
			HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean supports(Class<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void writeInternal(Place arg0, HttpOutputMessage arg1)
			throws IOException, HttpMessageNotWritableException {
		// TODO Auto-generated method stub
		
	}

}
