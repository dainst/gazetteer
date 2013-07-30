# encoding: utf-8

require 'rubygems'
require 'mongo'

include Mongo

client = MongoClient.new
db = client['gazetteer']
places = db['place']

places.find("ids.context" => "zenon-systemnr").each do |place|
  ids = place["ids"].select { |id| id["context"] == "zenon-systemnr" }
  puts ids.first['value'] + " 034   L $$0" + place["_id"] + "$$2gazetteer"
end