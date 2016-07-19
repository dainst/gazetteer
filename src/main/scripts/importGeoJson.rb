#!/usr/bin/env ruby
require 'ostruct'
require 'optparse'
require 'rest-client'
require 'json'


# Standard parameter values

gaz_uri = "https://gazetteer.dainst.org"
id_field = "gazId"


# option parsing

options = OpenStruct.new
opts = OptionParser.new do |opts|
  
  opts.banner = "Usage: importGeoJson.rb [options] [file ...]"

  opts.on("-u", "--username USERNAME", "Gazetteer user name") do |u|
    options.user = u
  end

  opts.on("-p", "--password [PASSWORD]", "Gazetteer user password") do |p|
    if p
      options.password = p
    else
      options.password = ask("Gazetteer password:") { |q| q.echo = false }
    end
  end

  options.uri = gaz_uri
  opts.on("-U", "--uri URI", "Use URI as gazetteer base uri (standard: \"#{gaz_uri}\")") do |u|
  	options.uri = u
  end

  options.idField = id_field
  opts.on("-i", "--id-field FIELDNAME", "The name of the properties field which contains the Gazetteer ID (standard: \"#{id_field}\")") do |i|
    options.idField = i
  end

  options.commit = false
  opts.on("-c", "--[no-]commit", "Commit changes to gazetteer instead of only printing them") do |c|
  	options.commit = c
  end

  options.verbose = false
  opts.on("-v", "--[no-]verbose", "Run verbosely") do |v|
    options.verbose = v
  end

  opts.on_tail("-h", "--help", "Show this message") do
    puts opts
    exit
  end

end

opts.parse!


# main program

gaz = RestClient::Resource.new(options.uri, :user => options.user, :password => options.password)

file = ARGF.read;

data = JSON.parse(file);

for feature in data['features'] do
  gazId = feature['properties'][options.idField];

  begin
    response = gaz["doc/#{gazId}"].get(:content_type => :json, :accept => :json)
    place = JSON.parse(response.body, :symbolize_names => true)
  rescue RestClient::Exception => e
    if e.http_code == 401
      puts "ERROR: user name or password incorrect, aborting ..."
      exit(1)
    end
    puts "WARNING: gazetteer id #{gazId} is not present in the gazetteer"
    puts "HTTP response code: #{e.http_code}" if options.verbose
    next;
  end

  if !place[:prefLocation]
    place[:prefLocation] = {};
  end

  place[:prefLocation][:shape] = feature['geometry']['coordinates'];

  if options.commit
    begin
      response = gaz["doc/#{gazId}"].put(place.to_json, :content_type => :json, :accept => :json)
      puts "updated: " + response.headers[:location] if options.verbose
    rescue RestClient::Exception => e
      if e.http_code == 401
        puts "ERROR: user name or password incorrect, aborting ..."
        exit(1)
      else
        puts "ERROR: #{e.http_code}"
        puts JSON.pretty_generate(place)
        puts e.response.body
      end
    end
  else
    puts JSON.pretty_generate(place);
  end
end

exit(0)
