# encoding: utf-8

require 'rubygems'
require 'mongo'

include Mongo

client = MongoClient.new
db = client['gazetteer']
places = db['place']

places.find("ids.context" => "arachne-place").each do |place|
  ids = place["ids"].select { |id| id["context"] == "arachne-place" }
  ids.each do |id|
    puts "UPDATE ort SET Gazetteerid = #{place['_id']} WHERE PS_OrtID = #{id['value']};"
  end
end