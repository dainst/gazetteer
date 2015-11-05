#!/usr/bin/env ruby
require 'ostruct'
require 'optparse'
require 'rest-client'
require 'json'
require 'csv'
require 'pp'
require 'highline/import'
require 'net/http'


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

  opts.on("-P", "--provenance TAGS", "Add comma separated TAGS to provenance field (mandatory)") do |p|
    options.provenance = p.split ","
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

  opts.on("-T", "--temp-id ROW_NUMBER", "Use values in row ROW_NUMBER as temporary IDs.\n                                     Temporary IDs can be referenced in the template with \"\#{id(_[ROW_NUMBER])}\" in order to insert the generated gazetter IDs.\n                                     Note: In order for this mechanism to work referenced places have to occur before being referred to.\n                                     When no value is given the line number is used as the temporary ID.") do |t|
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
  opts.on("-r", "--[no-]replace", "Replace existing data\n                                     when merging new data will have priority over existing data\n                                     when merging is switched off existing places with the same id will be replaced") do |r|
    options.replace = r
  end

  options.keepAsAlternative = false
  opts.on("-k", "--keep-as-alternative", "Keep preferred names/locations as alternative names/locations when merging with activated replace option") do |k|
    options.keepAsAlternative = k
  end

  options.updatedCSV = false
  opts.on("-C", "--updated-csv FILE", "Create an updated version of the CSV input file which includes newly generated Gazetteer IDs") do |f|
    options.updateCSV = f
  end

  options.geonames = false
  opts.on("-g", "--geonames-ids", "Import Geonames IDs if they match the place name title") do |g|
    options.geonames = g
  end

  options.splitNames = false
  opts.on("-S", "--split-names SEPARATOR", "Divide place names into multiple names based on separator") do |s|
    options.splitNames = s
  end

  options.degrees = false
  opts.on("-d", "--degrees", "Convert coordinates from degrees (°), minutes ('), seconds ('') to decimal values") do |d|
    options.degrees = d
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

# coordinates conversion helper method (from degrees, minutes, seconds to decimal values)
def convert_degrees_to_decimal(coordinate)
  pos = 0
  tempString = ""
  degrees = 99999
  minutes = 99999
  seconds = 99999
  orientation = ""
  while pos < coordinate.length do
    if pos != coordinate.length - 1
      nextCharacter = coordinate[pos + 1]
    else
      nextCharacter = ""
    end
    if coordinate[pos] == "°"
      if degrees == 99999
        degrees = tempString.to_i
        tempString = ""
        pos += 1
      else
        return nil
      end
    elsif ["'", "’", "‘"].include?(coordinate[pos]) && !["'", "’", "‘"].include?(nextCharacter)
      if minutes == 99999
        minutes = tempString.to_i
        tempString = ""
        pos += 1
      else
        return nil
      end
    elsif ["'", "’", "‘"].include?(coordinate[pos]) && ["'", "’", "‘"].include?(nextCharacter)
      if seconds == 99999
        seconds = tempString.to_i
        tempString = ""
        pos += 2
      else
        return nil
      end
    elsif ["n", "s", "w", "o" ].include? coordinate[pos].downcase
      if orientation == "" && tempString == "" && degrees != 99999
        orientation = coordinate[pos].downcase
        pos += 1
      else
        return nil
      end
    elsif coordinate[pos] == " "
      if tempString == "" || !(nextCharacter =~ /[[:digit:]]/)
        pos += 1
        next
      else
        return nil
      end
    else
      if coordinate[pos] =~ /[[:digit:]]/ && orientation == ""
        tempString += coordinate[pos]
        pos += 1
      else
        return nil
      end
    end  
  end

  result = 0.0
  result += seconds.to_f / 60 if seconds != 99999
  result += minutes.to_f / 60 if minutes != 99999
  result += degrees.to_f if degrees != 99999

  if ["s", "w"].include? orientation
    result *= -1.0
  end
  
  return result
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

# country codes map
$country_codes = {};

# geonames ids map for administrative units
$administrative_units_geonames_ids = {}

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
    # write GazID header to updated CSV file
    if options.updateCSV
      updatedRow = row.dup
      updatedRow << "Gazetteer ID"
      if options.geonames
        updatedRow << "Geonames ID"
      end
      CSV.open(options.updateCSV, "ab", {:col_sep => options.separator}) do |csv|
        csv << updatedRow
      end
    end
    next
  end

  if row[0] && row[0].start_with?('#')
    puts "skipping comment row #{row_no}"
    # copy comment row to updated CSV file
    if options.updateCSV
      updatedRow = row.dup
      CSV.open(options.updateCSV, "ab", {:col_sep => options.separator}) do |csv|
        csv << updatedRow
      end
    end
    next
  end

  id_present = false

  if options.temp_id
  	temp_id = row[options.temp_id]
  else
  	temp_id = row_no
  end

  # normalize field values
  row.map! { |s| s.unicode_normalize if s }

  # create place object by applying template
  _ = row
  _.map! { |val| val.to_s } # convert nils to empty strings
  _.map! { |val| val.strip } # remove leading and trailing whitespace
  eval_str = "\"#{options.template.gsub(/\"/){|m|"\\"+m}.gsub(/'/,"\"")}\""
  begin
  	place = JSON.parse(eval(eval_str), :symbolize_names => true)
  rescue Exception => e
  	puts e.message
  	next
  end

  # convert coordinates
  if options.degrees
    if place[:prefLocation] && place[:prefLocation][:coordinates] && place[:prefLocation][:coordinates].length == 2
      lng = convert_degrees_to_decimal(place[:prefLocation][:coordinates][0])
      lat = convert_degrees_to_decimal(place[:prefLocation][:coordinates][1])
      if lng != nil && lat != nil
        place[:prefLocation][:coordinates] = [lng, lat]
      else
        puts "Failed to convert coordinates for pref location of place #{temp_id}"
        next
      end
    end
    if place[:locations]
      for location in place[:locations] do
        if location && location[:coordinates] && location[:coordinates].length == 2
          lng = convert_degrees_to_decimal(location[:coordinates][0])
          lat = convert_degrees_to_decimal(location[:coordinates][1])
          if lng != nil && lat != nil
            location[:coordinates] = [lng, lat]
          else
            puts "Failed to convert coordinates for additional location of place #{temp_id}"
            next
          end
        end
      end
    end
  end

  # shape
  if place[:prefLocation] && place[:prefLocation][:shapeString]
    if place[:prefLocation][:shapeString] == ""
      place[:prefLocation].delete(:shapeString)
    else
      tempString = place[:prefLocation][:shapeString].downcase

      if tempString.include?("multipolygon(((") || tempString.include?("multipolygon (((")
        tempString = tempString.gsub("multipolygon", "")
        tempString.slice!(0)
      elsif tempString.include?("polygon((") || tempString.include?("polygon ((")
        tempString = tempString.gsub("polygon", "")
      else
        puts "Invalid polygon data for place #{temp_id}"
        next
      end

      tempString.strip!

      multipolygon = Array.new

      level = "multipolygon"
      i = 0
      j = 0
      error = false

      while tempString.length > 0 do

        if tempString[0] == "("
          tempString.slice!(0)
          if level == "multipolygon"
            multipolygon[i] = Array.new
            level = "polygon"
          elsif level == "polygon"
            level = "path"
          end
        elsif tempString[0] == ")"
          tempString.slice!(0)
          if level == "multipolygon"
            break
          elsif level == "polygon"
            level = "multipolygon"
            i += 1
            j = 0
          elsif level == "path"
            level = "polygon"
            j += 1
          end
        elsif tempString[0] == "," || tempString[0] == " "
          tempString.slice!(0)
        else
          index = tempString.index(")")
          if index == nil
            error = true
            break
          end
          substring = tempString.slice!(0..index - 1)
          points = substring.split(',')
          pointsArray = Array.new
          for point in points do
            pointArray = point.split(' ')
            floatArray = Array.new
            for coordinate in pointArray do
              floatCoordinate = coordinate.to_f
              floatArray << floatCoordinate
            end
            pointsArray << floatArray
          end
          multipolygon[i][j] = pointsArray
        end

      end

      if error
        puts "Invalid polygon data for place #{temp_id}"
        next
      end

      place[:prefLocation].delete(:shapeString)
      place[:prefLocation][:shape] = multipolygon
    end
  end

  # get country code and geonames id of administrative unit (if geonames option is active)
  if options.geonames
    country_code = ""
    administrative_unit_geonames_id = ""
    # check if the place already has a country code and/or is an administrative unit
    for identifier in place[:identifiers] do
      if identifier[:context] == "ISO 3166-1 alpha-2"
        country_code = identifier[:value]
      end
    end
    if place[:types].include? "administrative-unit"
      for identifier in place[:identifiers] do
        if identifier[:context] == "geonames"
          administrative_unit_geonames_id = identifier[:value]
        end
      end
    end
    if country_code != ""
      $country_codes[temp_id.to_s] = country_code
    else
      # check if a previously imported parent place has a country code
      parent_temp_id = $ids.find{ |key, value| value == place[:parent][place[:parent].rindex('/')+1..-1] }
      if parent_temp_id && parent_temp_id.is_a?(Array) && $country_codes.key?(parent_temp_id[0].to_s)
        country_code = $country_codes[parent_temp_id[0].to_s]
        $country_codes[temp_id.to_s] = country_code
        if administrative_unit_geonames_id == "" && $administrative_units_geonames_ids.key?(parent_temp_id[0].to_s)
          administrative_unit_geonames_id = $administrative_units_geonames_ids[parent_temp_id[0].to_s]
        end
      else
        # get country code from gazetteer
        if place[:parent]
          parent_id = place[:parent].to_s
        else
          raise "Can't find country code for place with temp id #{temp_id.to_s}!"
        end
        loop do
          parent_id = parent_id[parent_id.rindex('/')+1..-1]
          response = gaz["doc/#{parent_id}"].get(:content_type => :json, :accept => :json)
          parent_place = JSON.parse(response.body, :symbolize_names => true)
          geonames_id = ""
          if parent_place[:identifiers]
            for identifier in parent_place[:identifiers] do
              if identifier[:context] == "ISO 3166-1 alpha-2"
                country_code = identifier[:value]
                $country_codes[temp_id.to_s] = country_code
              end
              if identifier[:context] == "geonames"
                geonames_id = identifier[:value]
              end
            end
          end
          if administrative_unit_geonames_id == "" && parent_place[:types].include?("administrative-unit") && geonames_id != ""
            administrative_unit_geonames_id = geonames_id
          end
          break if country_code != ""
          if parent_place[:parent]
            parent_id = parent_place[:parent].to_s
          else
            raise "Can't find country code for place with temp id #{temp_id.to_s}!"
          end
        end
        $country_codes[temp_id.to_s] = country_code
      end
    end
    $administrative_units_geonames_ids[temp_id.to_s] = administrative_unit_geonames_id
  end

  # get geonames id
  if options.geonames && place[:prefName][:title] && !place[:types].include?("administrative-unit")
    uri = URI.parse("http://arachne.uni-koeln.de")
    http = Net::HTTP.new(uri.host, 8080)

    # get administrative unit
    if $administrative_units_geonames_ids.key?(temp_id.to_s)
      http_response = http.get('/solrGeonames35/select/?q=id:geonames-' + $administrative_units_geonames_ids[temp_id.to_s] + '&version=2.2&start=0&rows=500&indent=on&wt=ruby')
      response = eval(http_response.body)
      admin1 = response['response']['docs'][0]['admin1_code']
      admin2 = response['response']['docs'][0]['admin2_code']
      admin3 = response['response']['docs'][0]['admin3_code']
      admin4 = response['response']['docs'][0]['admin4_code']
    end

    searchName = place[:prefName][:title].gsub(" ", "%20")
    if place[:types].include? "populated-place"
      featureClass = '%20AND%20feature_class:P'
    else
      featureClass = ''
    end
    http_response = http.get('/solrGeonames35/select/?q=%28name:"' + searchName + '"%20OR%20alternatenames:"' + searchName + '"%29%20AND%20country_code:' + country_code + featureClass + '&version=2.2&start=0&rows=500&indent=on&wt=ruby')
    response = eval(http_response.body)
    if response['response']['docs'].size == 0
      puts "no geonames id found for place " + place[:prefName][:title]
    else
      geonames_document = nil
      for doc in response['response']['docs'] do
        if doc['admin1_code'] == admin1 && doc['admin2_code'] == admin2 && doc['admin3_code'] == admin3 && doc['admin4_code'] == admin4
          if geonames_document != nil
            multiple = true
          else
            geonames_document = doc
          end
        end
      end
    end

    puts "more than one geonames id found for place " + place[:prefName][:title] if multiple

    if geonames_document != nil && !multiple
      geonamesId = geonames_document['id']
      if geonamesId
        identifier = Hash.new
        identifier[:value] = geonamesId.to_s.gsub("geonames-", "")
        identifier[:context] = "geonames"
        place[:identifiers] << identifier
      end
    end
  end

  # check for existing place
  existing_place = nil
  id_present = true if !place[:gazId].to_s.empty?
  if id_present
    begin
      response = gaz["doc/#{place[:gazId]}"].get(:content_type => :json, :accept => :json)
      existing_place = JSON.parse(response.body, :symbolize_names => true)
    rescue RestClient::Exception => e
      if e.http_code == 401
        puts "ERROR: user name or password incorrect, aborting ..."
        exit(1)
      end
      puts "WARNING: gazetteer id #{place[:gazId]} not present in gazetteer, generation of custom ids is not supported"
      puts "HTTP response code: #{e.http_code}" if options.verbose
      place.delete(:gazId)
      id_present = false
    end
  end

  # postprocess to delete empty fields and add provenance
  place[:provenance] = options.provenance
  place.delete(:gazId) if place[:gazId].to_s.empty?
  if place[:prefName]
    place[:prefName].delete(:language) if place[:prefName][:language].to_s.empty?
    place[:prefName].delete(:ancient) if !place[:prefName][:ancient]
    place[:prefName].delete(:transliterated) if !place[:prefName][:transliterated]
  end
  if options.splitNames
    newNames = []
    for name in place[:names] do
      titles = name[:title].split(options.splitNames)
      if titles.length > 1
        name[:title] = titles[0].strip
        for i in 1..titles.length - 1 do
          newName = {}
          newName[:title] = titles[i].strip
          newName[:language] = name[:language]
          newName[:ancient] = name[:ancient]
          newName[:transliterated] = name[:transliterated]
          newNames << newName
        end
      end
    end
    place[:names].concat(newNames)
  end
  if place[:prefName] && place[:prefName][:title].to_s.empty?
    if existing_place && existing_place[:prefName] && existing_place[:prefName][:title] && !existing_place[:prefName][:title].empty?
      place.delete(:prefName)
    elsif place[:names] && place[:names].size > 0
      place[:prefName] = place[:names][0]
      place[:names].shift
    else
      place[:prefName][:title] = "-Untitled-" 
    end
  end
  if place[:names] 
    place[:names].delete_if { |name| name[:title].to_s.empty? }
  end
  if place[:types] 
    place[:types].delete_if { |type| type.to_s.empty? }
  end
  place.delete(:types) if place[:types] && place[:types].empty?
  place[:prefLocation].delete(:coordinates) if place[:prefLocation] && place[:prefLocation][:coordinates] == [0, 0]
  place.delete(:prefLocation) if place[:prefLocation] && !place[:prefLocation][:coordinates] && !place[:prefLocation][:shape]
  if place[:identifiers] 
    place[:identifiers].delete_if { |id| id[:value].to_s.empty? || id[:value] == "0" }
  end  
  if place[:tags] 
    place[:tags].delete_if { |tag| tag.to_s.empty? }
  end
  if place[:comments]
    place[:comments].delete_if { |comment| comment[:text].to_s.empty? }
  end

  total += 1

  # apply merge if necessary
  if id_present
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

        # keep prefNames & prefLocations as alternative names/locations if keepAsAlternative option is switched on
        if options.keepAsAlternative
          if place[:prefName] && place[:prefName][:title] && !place[:prefName][:title].to_s.empty?
            if existing_place[:prefName] && existing_place[:prefName][:title] && !existing_place[:prefName][:title].to_s.empty?
              if place[:prefName][:title] != existing_place[:prefName][:title] || place[:prefName][:language] != existing_place[:prefName][:language]
                existing_place[:names] = [] if !existing_place[:names]                
                existing_place[:names] << existing_place[:prefName]
              end
            end
          end
          if place[:prefLocation] && (place[:prefLocation][:coordinates] || place[:prefLocation][:shape])
            if existing_place[:prefLocation] && (existing_place[:prefLocation][:coordinates] || existing_place[:prefLocation][:shape])
              existing_place[:locations] = [] if !existing_place[:locations]
              existing_place[:locations] << existing_place[:prefLocation]
            end
          end
        end

        place = existing_place.merge(place, &merger)
      end
      merged += 1
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

  # write updated CSV file
  if options.updateCSV
    updatedRow = row.dup
    updatedRow << $ids[temp_id.to_s]
    if place[:identifiers].size > 0 && place[:identifiers][0][:context] == "geonames"
      updatedRow << place[:identifiers][0][:value]
    elsif place[:identifiers].size > 1 && place[:identifiers][1][:context] == "geonames"
      updatedRow << place[:identifiers][1][:value]
    end
    CSV.open(options.updateCSV, "ab", {:col_sep => options.separator}) do |csv|
      csv << updatedRow
    end
  end

end

pp $ids if options.verbose
puts "OK: read #{total} places"
puts "inserted #{inserted}"
puts "skipped #{skipped}"
puts "merged #{merged}"
puts "replaced #{replaced}"
exit(0)
