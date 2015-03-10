#!/usr/bin/env ruby

require 'rest-client'
require 'json'

gaz = RestClient::Resource.new("http://gazetteer.dainst.org", :user => "admin", :password => "")

result = gaz["search/?q=provenance:2015.03.04_Ortsnamenliste-Aleppo&limit=1000"].get(:content_type => :json, :accept => :json)
result = JSON.parse(result, :symbolize_names => true)
puts "results: #{result[:result].length}"
result[:result].each do |place|
	result2 = gaz["doc/#{place[:gazId]}"].get(:content_type => :json, :accept => :json)
	place = JSON.parse(result2, :symbolize_names => true)
	next if !place[:names]
	place[:names].map! do |name|
		name[:title].unicode_normalize!
		name
	end
	response = gaz["doc/#{place[:gazId]}"].put(place.to_json, :content_type => :json, :accept => :json)
	puts "updated: " + response.headers[:location]
end
