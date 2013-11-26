# encoding: utf-8

require 'rubygems'
require 'mongo'
require 'csv'
require 'json'

include Mongo

client = MongoClient.new
db = client['gazetteer']
$places = db['place']

counter = 2181600

def save_place(place)
  if ARGV[1] != "--noop" then
    $places.save place
  else
    jj place
  end
  puts "updated: #{place['_id']}"
end

i = 0
CSV.foreach(ARGV[0], {:col_sep => ";"}) do |row|
  next if row[0] == nil
	place = {
    "_id" => counter.to_s,
    "_class" => "org.dainst.gazetteer.domain.Place",
    "type" => "city",
    "prefName" => {
        "title" => row[1],
        "ancient" => false,
        "ordering" => 0
    },
    "names" => [],
    "prefLocation" => {
        "coordinates" => [
            row[5],
            row[4]
        ],
        "confidence" => 1
    },
    "locations" => [],
    "parent" => (counter+1).to_s,
    "relatedPlaces" => [],
    "comments" => [],
    "tags" => [],
    "ids" => [
        {
            "value" => row[0].to_s,
            "context" => "geonames"
        }
    ],
    "needsReview" => true,
    "deleted" => false,
    "children" => 0
  }
  if row[3] then
    row[3].split(",").each do |name|
      place["names"] << {
        "title" => name,
        "ancient" => false,
        "ordering" => 0
      }
    end
  end
  save_place place
  counter += 1
  province = {
    "_id" => counter.to_s,
    "_class" => "org.dainst.gazetteer.domain.Place",
    "prefName" => {
        "title" => row[9],
        "ancient" => false,
        "ordering" => 0
    },
    "type" => "region",
    "names" => [],
    "locations" => [],
    "parent" => "2043686",
    "relatedPlaces" => [],
    "comments" => [],
    "tags" => [],
    "ids" => [
        {
            "value" => row[12].to_s,
            "context" => "geonames"
        }
    ],
    "needsReview" => true,
    "deleted" => false,
    "children" => 0
  }
  if row[11] then
    row[11].split(",").each do |name|
      province["names"] << {
        "title" => name,
        "ancient" => false,
        "ordering" => 0
      }
    end
  end
  counter += 1
  save_place province
end

puts "total updates: #{i}"