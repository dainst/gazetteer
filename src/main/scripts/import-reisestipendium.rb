# encoding: utf-8

require 'rubygems'
require 'mongo'
require 'csv'

include Mongo

client = MongoClient.new
db = client['gazetteer']
places = db['place']

i = 0
CSV.foreach(ARGV[0], {:col_sep => ';'}) do |row|
	place = places.find_one({ '_id' => row[0]})
  if place != nil then
    place['noteReisestipendium'] = row[2]
    places.save place
    puts "updated: #{row[0]}"
    i += 1
  end
end

puts "total updates: #{i}"