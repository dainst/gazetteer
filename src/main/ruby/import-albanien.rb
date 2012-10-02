require "rest-client"
require "ap"
require "json"

gaz_uri = "http://localhost:8080/gazetteer"
root = '/Users/scuy/Development/Arachne/material/Arkiva_Archive/Dokumentacion_Archaeological record'

gaz = RestClient::Resource.new gaz_uri

def walk(path, parent_uri, gaz)
  
  Dir.foreach(path) do |f|

    result = f.split("_")
   
    next if result == nil
    next if result.size < 3
   
    result[1] = result[1][0..-2] if result[1][-1] == "s"
    result[1] = (result[1][0..-3] + "y") if result[1][-2,2] == "ie"
   
    place = {
      "type" => result[1],
      "thesaurus" => "albania",
      "names" => [
        {
          "title" => result[2],
          "language" => "sq"
        }
      ]
    }
    
    place["parent"] = parent_uri if parent_uri != nil

    begin
      response = gaz["place"].post place.to_json, :content_type => :json, :accept => :json
      uri = response.headers[:location]
      puts result[2] + " -> " + uri
    rescue => e
      puts "AN ERROR OCCURRED!"
      ap e
    end
   
    if Dir.entries(path+"/"+f).size > 0 then
      walk path+"/"+f, uri, gaz
    end
   
  end
  
end

walk root, nil, gaz



