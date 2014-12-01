#!/usr/bin/env ruby
require 'ostruct'
require 'optparse'
require 'rest-client'
require 'json'
require 'csv'
require 'pp'
require 'highline/import'


# Standard parameter values

gaz_uri = "http://gazetteer.dainst.org"

template = '{
  "prefName": {
    "title": "#{_[0]}",
    "language": "#{_[1]}",
    "ancient": #{ case _[2]; when \'yes\' then true; else false; end }
  },
  "types": [ "#{_[3]}" ],
  "prefLocation": {
    "coordinates": [ #{_[4].gsub(/,/,\'.\').to_f}, #{_[5].gsub(/,/,\'.\').to_f} ]
  },
  "identifiers": [
    {
      "value": "#{_[6]}",
      "context": "geonames"
    },
    {
      "value": "#{_[7]}",
      "context": "pleiades"
    }
  ],
  "parent": "http://gazetteer.dainst.org/place/#{_[8].empty? ? id(_[9]) : _[8]}"
}'


# option parsing

options = OpenStruct.new
opts = OptionParser.new do |opts|
  
  opts.banner = "Usage: example.rb [options] [file ...]"

  opts.on("-P", "--provenance TAG", "Add TAG to provenance field (mandatory)") do |p|
    options.provenance = p
  end

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

  options.separator = ";"
  opts.on("-s", "--separator SEPARATOR", "Use SEPARATOR as a column separator (standard: \"#{options.separator}\")") do |s|
  	options.separator = s
  end

  options.headers = false
  opts.on("-H", "--[no-]headers", "Skip first line in CSV input") do |d|
  	options.headers = d
  end

  options.template = template
  opts.on("-t", "--template TEMPLATE", "Use TEMPLATE as a JSON template for every place,\n                                     use \"\#\{_[n]\}\" to reference columns in CSV starting with n=0") do |t|
  	options.template = open(t).read
  end

  opts.on("-T", "--temp-id ROW_NUMBER", "Use values in row ROW_NUMBER as temporary IDs.\n                                     Temporary IDs can be referenced in the template to with \"\#{id[_[ROW_NUMBER]]}\" in order to insert the generated gazetter IDs.\n                                     Note: In order for this mechanism to work referenced places have to occur before being referred to.\n                                     When no value is given the line number is used as the temporary ID.") do |t|
  	options.temp_id = t.to_i
  end

  options.commit = false
  opts.on("-c", "--[no-]commit", "Commit changes to gazetteer instead of only printing them") do |c|
  	options.commit = c
  end

  options.merge = false
  opts.on("-m", "--[no-]merge", "Perform a merge with existing data if 'gazId' is present\n                                     IMPORTANT: otherwise places with existing gazetteer ids will be replaced!") do |m|
    options.merge = m
  end

  options.replace = false
  opts.on("-r", "--[no-]replace", "Replace existing data\n                                     when merging new data will have priority over existing data\n                                     when merging is switchted existing places with the same id will be replaced") do |r|
    options.replace = r
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

if !options.provenance
  puts "ERROR: provenance not set!"
  puts opts
  exit(1)
end

# merge helper
merger = lambda do |key, oldval, newval|
  if oldval.is_a? Array
    newval | oldval
  elsif oldval.is_a? Hash
    oldval.merge(newval)
  else
    newval
  end
end

# id map helper
$ids = {}
def id(temp_id)
	if $ids.key?(temp_id.to_s)
		$ids[temp_id.to_s]
	else
		raise IndexError, "Temporary ID #{temp_id} has not been assigned a permanent ID. Make sure rows are processed in the right order."
	end
end

# main program

gaz = RestClient::Resource.new(options.uri, :user => options.user, :password => options.password)
total = 0
inserted = 0
skipped = 0
merged = 0
replaced = 0

ids = {} # map to store mapping between temporary and gazetteer IDs
parsed_headers = false
row_no = 0

CSV.parse(ARGF.read, {:col_sep => options.separator}) do |row|

  row_no += 1
  
  if options.headers and !parsed_headers
    parsed_headers = true
    next
  end

  id_present = false

  if options.temp_id
  	temp_id = row[options.temp_id]
  else
  	temp_id = row_no
  end

  # create place object by applying template
  _ = row
  _.map! { |val| val.to_s } # convert nils to empty strings
  eval_str = "\"#{options.template.gsub(/\"/){|m|"\\"+m}}\""
  begin
  	place = JSON.parse(eval(eval_str), :symbolize_names => true)
  rescue Exception => e
  	puts e.message
  	next
  end

  # postprocess to delete empty fields and add provenance
  place[:provenance] = [options.provenance]
  place.delete(:gazId) if place[:gazId].to_s.empty?
  place[:prefName].delete(:language) if place[:prefName][:language].to_s.empty?
  place[:prefName].delete(:ancient) if !place[:prefName][:ancient]
  place[:prefName][:title] = "-Untitled-" if place[:prefName][:title].to_s.empty?
  if place[:alternativeNames] 
    place[:alternativeNames].delete_if { |id| id[:title].to_s.empty? }
  end
  place.delete(:types) if place[:types].empty?
  place.delete(:prefLocation) if place[:prefLocation] and place[:prefLocation][:coordinates] == [0, 0]
  if place[:identifiers] 
    place[:identifiers].delete_if { |id| id[:value].to_s.empty? || id[:value] == "0" }
  end
  if place[:comments]
    place[:comments].delete_if { |comment| comment[:text].to_s.empty? }
  end

  id_present = true if !place[:gazId].to_s.empty?
  total += 1

  # check for existing place and apply merge if necessary
  if id_present
    begin
      response = gaz["doc/#{place[:gazId]}"].get(:content_type => :json, :accept => :json)
      existing_place = JSON.parse(response.body, :symbolize_names => true)
      if !options.merge
        if !options.replace
          # skip place to prevent replacement of existing place
          puts "skipping duplicate #{place[:gazId]}" if options.verbose
          skipped += 1
          next
        else
          # place will be replaced
          puts "WARNING: place with gazetteer id #{place[:gazId]} will be replaced!"
          replaced += 1
        end
      else
        if !options.replace
          # existing place has priority
          place = place.merge(existing_place, &merger)
        else
          # new place has priority
          place = existing_place.merge(place, &merger)
        end
        merged += 1
      end
    rescue RestClient::Exception => e
      if e.http_code == 401
        puts "ERROR: user name or password incorrect, aborting ..."
        exit(1)
      end
      puts "WARNING: gazetteer id #{place[:gazId]} not present in gazetteer, generation of custom ids is not supported"
      puts "HTTP response code: #{e.http_code}" if options.verbose
      place.delete(:gazId)
      id_present = false
      inserted += 1
    end
  else
    inserted += 1
  end

  # write data to gazetteer
  if options.commit
    begin
      # perform POST to gazetteer API
      if id_present
        response = gaz["doc/#{place[:gazId]}"].put(place.to_json, :content_type => :json, :accept => :json)
        $ids[temp_id.to_s] = place[:gazId]
        puts "updated: " + response.headers[:location] if options.verbose
      else
        response = gaz["doc/"].post(place.to_json, :content_type => :json, :accept => :json)
        $ids[temp_id.to_s] = JSON.parse(response.body)["gazId"]
        puts "created: " + response.headers[:location] if options.verbose
      end
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
    # dry run
    if id_present
      $ids[temp_id.to_s] = place[:gazId]
    else
      $ids[temp_id.to_s] = "temp_#{temp_id}"
    	place[:gazId] = $ids[temp_id.to_s]
    end
    puts JSON.pretty_generate(place) if options.verbose
  end

end

pp $ids if options.verbose
puts "OK: read #{total} places"
puts "inserted #{inserted}"
puts "skipped #{skipped}"
puts "merged #{merged}"
puts "replaced #{replaced}"
exit(0)
