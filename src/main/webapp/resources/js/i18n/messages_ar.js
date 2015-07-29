var messages = angular.module('gazetteer.messages', []);

messages.factory('messages', function(){
	return {
		"ui.error": "خطأ",
		"ui.contactAdmin": "في حال arachne@uni-koeln.de وجود المشكلة بشكل دائم يرجى الاتصال بـ",
		"ui.search.results": "نتيجة البحث",
		"ui.search.hits": "نتيجة",
		"ui.search.limit.10.tooltip": "أظهر ١٠ نتائج في الصفحة",
		"ui.search.limit.50.tooltip": "أظهر ٥٠ نتيجة في الصفحة",
		"ui.search.limit.100.tooltip": "أظهر ١٠٠ نتيجة في الصفحة",
		"ui.search.limit.1000.tooltip": "أظهر ١٠٠٠ نتيجة في الصفحة",
		"ui.search.sort.score.tooltip": "ترتيب حسب الصلة",
		"ui.search.sort.id.tooltip": "ترتيب حسب رقم التعريف",
		"ui.search.sort.name.tooltip": "ترتيب وفقاً للأسماء",
		"ui.search.sort.type.tooltip": "ترتيب وفقاً للنوع",
		"ui.search.sort.thesaurus.tooltip": "ترتيب وفقاً للمكنز",
		"ui.search.filter": "ترشيح",
		"ui.search.filter.coordinates": "مع إحداثيّات",
		"ui.search.filter.no-coordinates": "بدون إحداثيّات",
		"ui.search.filter.polygon": "مع مضلّع",
		"ui.search.filter.no-polygon": "بدون مضلّع",
		"ui.search.filter.no-tags": "بدون بطاقات",
		"ui.search.filter.no-provenance": "بدون ذكر المصدر",
		"ui.place.names.more": "أكثر",
		"ui.place.names.less": "أقلّ",
		"ui.place.children.search": "أظهر الأماكن في البحث",
		"ui.place.save.success": "تمّ حفظ المكان بنجاح",
		"ui.place.save.failure": "لقد تعذّر حفظ المكان",
		"ui.place.save.failure.parentError": "لا يمكن أن يكون المكان تابعاً لذاته أو تابعاً لمكان يتبع له",
		"ui.place.save.failure.accessDeniedError": "ليس لديك الصلاحية اللازمة لتعديل هذا المكان",
		"ui.place.duplicate.success": "تمّ استنساخ المكان بنجاح",
		"ui.place.duplicate.failure": "لقد تعذّر استنساخ المكان",
		"ui.place.remove.success": "تمّ حذف المكان بنجاح",
		"ui.place.remove.failure": "لقد تعذّر حذف المكان",
		"ui.place.protected-site-info": "الرجاء تسجيل الدخول من أجل الحصول على الإحداثيّات الدقيقة.",
		"ui.place.user-group-info": "عندما يتمّ تحديد مجموعة بيانات فإنّ هذا المكان يصبح مرئيّاً لأعضاء هذه المجموعة فقط. لا يمكن تغيير هذا الإعداد لاحقاً.",
		"ui.thesaurus": "المكنز",
		"ui.create": "إنشاء مكان",
		"ui.link.tooltip": "اربط مع المكان الحالي",
		"ui.place.deleted": "تمّ حذف هذا المكان",
		"ui.place.hiddenPlace": "مكان محجوب",
		"ui.place.provenance-info": "يحتوي هذا المكان بيانات من المصادر المُدخلة.",
		"ui.merge.tooltip": "ادمج هذا المكان مع المكان الحالي",
		"ui.merge.success.head": "تمّ الدمج بنجاح",
		"ui.merge.success.body": "يرجى التحقّق من معلومات المكان المُنشأ حديثاً الواردة أدناه",
		"ui.extendedSearch": "بحث موسّع",
		"ui.change-history.change-type.create": "تمّ الإنشاء",
		"ui.change-history.change-type.edit": "تمّ التعديل",
		"ui.change-history.change-type.delete": "تمّ الحذف",
		"ui.change-history.change-type.merge": "تمّ الدمج",
		"ui.change-history.change-type.replace": "تمّ الاستبدال",
		"ui.change-history.change-type.duplicate": "تمّ الاستنساخ",
		"ui.change-history.change-type.unknown": "مجهول",
		"place.name.ancient": "قديم",
		"place.name.transliterated": "منقول حرفياً",
		"place.types.no-type": ">لا يوجد نوع<",
		"place.types.archaeological-site": "موقع أثري",
		"place.types.archaeological-area": "منطقة أثرية",
		"place.types.continent": "قارّة",
		"place.types.administrative-unit": "وحدة إدارية",
		"place.types.populated-place": "مكان مأهول",
		"place.types.building-institution": "مبنى / منشأة",
		"place.types.landform": "التضاريس",
		"place.types.island": "جزيرة",
		"place.types.hydrography": "المسطّحات المائيّة",
		"place.types.landcover": "الغطاء النباتي",
		"place.types.description.archaeological-site": "مكان له بُنى ذات صبغة أثرية",
		"place.types.description.archaeological-area": "منطقة حضارية معرّفة أثريّاً ووحدات إدارية تاريخية",
		"place.types.description.continent": "مناطق برّية منفصلة محدّدة بواسطة حدود طبيعية وتاريخية",
		"place.types.description.administrative-unit": "وحدات إدارية معرّفة بشكل سياسي إداري",
		"place.types.description.populated-place": "مكان مأهول بالسكّان",
		"place.types.description.building-institution": "موقع المتاحف والمرافق الأخرى",
		"place.types.description.landform": "معلم تضريسي جيومورفولوجي",
		"place.types.description.island": "(مساحة برّية محاطة بالمياه بشكل كامل (أصغر من قارّة",
		"place.types.description.hydrography": "(جميع التجمّعات المائية الكبيرة (الراكدة والجارية",
		"place.types.description.landcover": "غطاء سطح الأرض الفيزيائي والبيولوجي",
		"place.types.groups.physical-geographic": "وحدات جغرافية طبيعية",
		"place.types.groups.human-geographic": "وحدات جغرافية بشرية",
		"place.types.groups.archaeological": "وحدات أثرية وحضارية تاريخية",
		"place.types.groups.building": "مبنى",
		"location.confidence.0": "لا توجد معطيات",
		"location.confidence.1": "غير دقيق",
		"location.confidence.2": "دقيق",
		"location.confidence.3": "دقيق للغاية",
		"location.confidence.4": "خاطئ",
		"location.public": "إحداثيات مفتوحة للعموم",
		"domain.place.parent": "يقع ضمن",
		"domain.place.types": "النوع",
		"domain.place.tags": "البطاقات"
	};
});