package org.dainst.gazetteer.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer.DeserializeException;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class JsonPlaceMessageConverter extends AbstractHttpMessageConverter<Place> {
	
	@Autowired
	private JsonPlaceDeserializer deserializer;

	public JsonPlaceMessageConverter() {
		super(new MediaType("application","json", Charset.forName("UTF-8")));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Place.class.isAssignableFrom(clazz);
	}

	@Override
	protected Place readInternal(Class<? extends Place> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		
		try {
			return deserializer.deserialize(inputMessage.getBody());
		} catch (DeserializeException e) {
			logger.error(e);
			throw new HttpMessageNotReadableException("Failed to deserialize message body", e);
		}
		
	}

	@Override
	protected void writeInternal(Place place, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		
		throw new IllegalStateException("method writeInternal() is not implemented and should never be called.");
		
	}

}
