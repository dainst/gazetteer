# encoding: utf-8

require 'rubygems'
require 'mongo'
require 'csv'

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

i = 0
CSV.foreach(ARGV[0], {:col_sep => ';'}) do |row|
  next if row[0] == nil
	place = $places.find_one({ '_id' => row[0].strip})
  if place != nil then
    place = get_current(place)
    place['noteReisestipendium'] = row[2].strip
    if row[3] != nil then
      place['commentsReisestipendium'] = [{"text" => row[3].strip}]
    end
    if ARGV[1] != "--noop" then
      $places.save place
    end
    puts "updated: #{row[0]}"
    i += 1
  elsif
    puts "Place not found: #{row}"
  end
end

puts "total updates: #{i}"