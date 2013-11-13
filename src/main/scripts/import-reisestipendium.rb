# encoding: utf-8

require 'rubygems'
require 'mongo'
require 'csv'
require 'json'

include Mongo

client = MongoClient.new
db = client['gazetteer']
$places = db['place']

def get_current(place)
  if place["deleted"] then
    if place["replacedBy"] then
      place = $places.find_one({ '_id' => place["replacedBy"]})
      puts "Found replacement #{place["_id"]}"
      return get_current(place)
      if place == nil then
        puts "Deleted place, replacement not found: #{place}"
      end
    elsif
      puts "Deleted place without replacement: #{place}"
    end
  else
    return place
  end
end

def save_place(place)
  if ARGV[1] != "--noop" then
    $places.save place
  else
    jj place
  end
  puts "updated: #{place['_id']}"
end

def add_related(place, relId)
  if place["relatedPlaces"] == nil then
    place["relatedPlaces"] = []
  end
  place["relatedPlaces"] << relId
end

i = 0
CSV.foreach(ARGV[0]) do |row|
  next if row[0] == nil
	place = $places.find_one({ '_id' => row[0].strip})
  if place != nil then
    place = get_current(place)
    if row[2] != nil then
      place['noteReisestipendium'] = row[2].strip
    end
    if row[3] != nil then
      place['commentsReisestipendium'] = [{"text" => row[3].strip}]
    end
    if row[4] != nil then
      place["parent"] = row[4].strip
    end
    if row[5] != nil then
      add_related(place, row[5].strip)
      relPlace = $places.find_one({ '_id' => row[5].strip})
      add_related(relPlace, row[0].strip)
      save_place(relPlace)
    end
    save_place(place)
    i += 1
  elsif
    puts "Place not found: #{row}"
  end
end

puts "total updates: #{i}"