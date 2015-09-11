use strict;
use warnings;
use utf8;
use open qw(:std :utf8);
use LWP::UserAgent;
use Encode;
use integer;

# harvest-gazetteer-json.pl
# simpler Wrapper, um alle Datensätze über die JSON-Schnittstelle des Gazetteers zu harvesten
# wichtig: man darf *nicht* angemeldet sein, damit nicht-öffentliche Datensätze ausgefiltert werden

my $entferneLinebreaks = 1;

my $schritt = 1000;

my $urlAnfang = "http://gazetteer.dainst.org/search.json?q=&fq=";
my $urlEnde = "&limit=".$schritt."&type=";

my $dir = "original";
mkdir($dir) unless(-d $dir);

my $gesamtDatei = "gesamt.json";
unlink $dir."/".$gesamtDatei;

#

sub readFile {
	my $url = shift;
	
	my $ua = LWP::UserAgent->new();
	my $response = $ua->get($url);
	my $content;
	if ($response->is_success) {
		$content = $response->decoded_content;
	} else {
		die "HTTP Error Message: ". $response->status_line . "\n". $url . "\n";
	}
	$content = decode("utf8", $content);
	
	return $content;
}

#

# bestimme die Gesamtzahl der Datensätze mit einer Testanfrage
my $content = readFile("http://gazetteer.dainst.org/gazetteer-test/search.json?q=&fq=&limit=1&type=");
$content =~ m!"total": (\d+),!;
my $gesamt = $1;
print "\nGesamt: ".$gesamt."\n\n";


my $i = 0; 
my $offset = "";

while ($i*$schritt < $gesamt) {

	# URL bestimmen
	if ($i > 0) { $offset = "&offset=".$i*$schritt; }	
	my $url = $urlAnfang.$offset.$urlEnde;
	print $url."\n";

	# Datensätze von URL einlesen
	$content = readFile($url);

	# Datensätze in Datei schreiben
	my $vierstellig = $i;
	if ($i < 1000) { $vierstellig = "0" . $vierstellig; } 
	if ($i < 100) { $vierstellig = "0" . $vierstellig; } 
	if ($i < 10) { $vierstellig = "0" . $vierstellig; } 
	my $original = "original-" . $vierstellig . ".json";
	open (ORIGINAL, ">$dir/$original");
	print ORIGINAL $content;
	close(ORIGINAL);
	print "--> ".$original."\n";

	# entferne Anfang und facets
	$content =~ s!(.|\n)+"result": \[!!;
	$content =~ s!],\n"facets":(.|\n)+!!;


	if ($entferneLinebreaks) {
		print "entferne Linebreaks\n";
		$content =~ s!{\n +"\@id"!{ "\@id"!g;	
		# dieser Schritt braucht sehr lange, aber join(' ',split("\n +", $content)) ist auch nicht schneller
		$content =~ s!\n +! !g;
		$content =~ s!\n},?! }!g;
	}


	# schreibe die Datensätze in die Gesamtdatei
	print "--> ".$gesamtDatei."\n\n";
	$content .= "\n";
	open (GESAMT, ">>$dir/$gesamtDatei");
	print GESAMT $content;
	close(GESAMT);
	
 	$i++;
}
