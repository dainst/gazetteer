# encoding: utf-8

require 'rubygems'
require 'mongo'
require 'rest_client'

include Mongo

client = MongoClient.new
db = client['gazetteer']
places = db['place']
i = 0

subjects = places.find(
	"$and" => [
		{ "parent" => "2043686" },
		{ "type" => { "$ne" => "region" } }
	]
)

subjects.each do |subject|
	candidates = places.find(
		"$and" => [
			{
				"$or" => [
					{ "prefName.title" => subject["prefName"]["title"] },
					{ "names.title" => subject["prefName"]["title"] }
				]
			},
			{
				"_id" => { "$ne" => subject["_id"] }
			}
		]

	)
	if candidates.count == 1 then
		candidate = candidates.to_a.first
		puts "#{candidate["_id"]} #{candidate["prefName"]["title"]} - #{subject["_id"]} #{subject["prefName"]["title"]}"
		RestClient.post "http://admin:admin1@gazetteer.dainst.org/merge/#{subject["_id"]}/#{candidate["_id"]}", {}
		i += 1
	end
end

puts "merged #{i} places"