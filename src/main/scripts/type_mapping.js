var mapping = {
	"city": "populated-place",
	"Archaeological Site": "archaeological-site",
	"museum": "building-institution",
	"populated place": "populated-place",
	"district": "administrative-unit",
	"archaeological-site": "archaeological-site",
	"archaeological site": "archaeological-site",
	"administrative-unit": "administrative-unit",
	"populated-place": "populated-place",
	"archaeological area": "archaeological-area",
	"City": "populated-place",
	"sea": "hydrography",
	"country": "administrative-unit",
	"site": "archaeological-site",
	"island": "island",
	"landform": "landform",
	"hydrography": "hydrography",
	"continent": "continent",
	"Populated Place": "populated-place",
	"lake": "hydrography",
	"Quelle": "hydrography",
	"river": "hydrography",
	"town": "populated-place",
	"Village": "populated-place",
	"Achaeological site": "archaeological-site",
	"Ancient Place": "archaeological-site",
	"Fluss": "hydrography",
	"hil": "landform",
	"monastery": "populated-place",
	"ancient site": "archaeological-site",
	"hill": "landform",
	"administrative unit": "administrative-unit",
	"ancient city": "archaeological-area",
	"landscape": "landform",
	"Ort": "populated-place",
	"Stadt": "populated-place",
	"mountain": "landform",
	"Administrative Unit": "administrative-unit",
	"deserted site": "archaeological-site",
	"Island": "island",
	"Museum": "building-institution",
	"Provinz": "administrative-unit",
	"Spring": "hydrography",
	"state": "administrative-unit",
	"Archeological Site": "archaeological-site",
	"ruins": "archaeological-site",
	"Stream": "hydrography",
	"Forest": "landcover",
	"archaeological-area": "archaeological-area",
	"land": "administrative-unit",
	"excavation": "archaeological-site",
	"stream": "hydrography",
	"Archaeological site": "archaeological-site",
	"ancient place": "archaeological-site",
	"deserted site": "archaeological-site",
	"Land": "administrative-unit",
	"Historic site": "archaeological-site",
	"ocean": "hydrography",
	"Ancient Site": "archaeological-site",
	"Administrativ Unit": "administrative-unit",
	"Administrative unit": "administrative-unit",
	"Town": "populated-place",
	"Ruinen": "archaeological-site",
	"forest": "landcover",
	"Insel": "island",
	"Sea": "hydrography",
	"See": "hydrography",
	"mountains": "landform",
	"arcaeological area": "archaeological-area",
	"insel": "island",
	"Sammlung": "institution",
	"archaeological aera": "archaeological-area",
	"River": "hydrography",
	"Ancient place": "archaeological-site",
	"archaeological areas": "archaeological-area",
	"Historic": "archaeological-site",
	"Archeological site": "archaeological-site",
	"Street": "archaeological-site",
	"stream source": "hydrography",
	"palace": "institution",
	"Ancient Plaace": "archaeological-site",
	"Tall": "archaeological-site",
	"Tell": "archaeological-site",
	"stadt": "populated-place",
	"arachaeological site": "archaeological-site",
	"archaeoological site": "archaeological-site",
	"archeological area": "archaeological-area",
	"Grab": "archaeological-site",
	"locality": "archaeological-site",
	"Wadi": "landform",
	"Ruin": "archaeological-site",
	"Cty": "populated-place",
	"Admininstrative Unit": "administrative-unit"
};

function storeOldTypes() {
	db.place.find({ type: { $exists: true }}).snapshot().forEach(	function(p) {
		p.type_old = p.type;
		db.place.save(p);
	});
}

function mapTypes() {
	for (old_type in mapping) {
		db.place.update({type_old: old_type}, { $set: { type: mapping[old_type] } }, { multi: true });
	}
}

function showTypes() {
	var types = db.place.group({
		cond: { deleted: false },
		key: { type: 1 },
		reduce: function(curr, result) {
			result.count += 1;
		},
		initial: { count: 0 }
	});
	types.forEach(function(t) {
		print(t.type + ": " + t.count);
	});
}