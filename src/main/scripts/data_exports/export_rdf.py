from lib.harvester import Harvester
from lib.cli_helper import is_writable_directory

import argparse
import logging
import pycountry

from rdflib import Graph, Literal, BNode, URIRef
from rdflib.namespace import DC, DCTERMS, FOAF, OWL, RDF, RDFS, SKOS
from rdflib.namespace import Namespace, NamespaceManager
from urllib.parse import quote

namespace_manager = NamespaceManager(Graph())

CRM = Namespace("http://www.cidoc-crm.org/rdfs/cidoc-crm#")
GAZ_ID = Namespace("http://gazetteer.dainst.org/types/id#")
GEO = Namespace("http://www.opengis.net/ont/geosparql#")
SF = Namespace("http://www.opengis.net/ont/sf#")
WGS84_POS = Namespace("http://www.w3.org/2003/01/geo/wgs84_pos#")
XSI = Namespace("http://www.w3.org/2001/XMLSchema-instance")

namespace_manager.bind('crm', CRM)
namespace_manager.bind('dc', DC)
namespace_manager.bind('dcterms', DCTERMS)
namespace_manager.bind('foaf', FOAF)
namespace_manager.bind('gaz_id', GAZ_ID)
namespace_manager.bind('geo', GEO)
namespace_manager.bind('owl', OWL)
namespace_manager.bind('rdf', RDF)
namespace_manager.bind('rdfs', RDFS)
namespace_manager.bind('sf', SF)
namespace_manager.bind('skos', SKOS)
namespace_manager.bind('wgs84_pos', WGS84_POS)
namespace_manager.bind('xsi', XSI)

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logging.basicConfig(format="%(asctime)s-%(levelname)s-%(name)s - %(message)s")

parser = argparse.ArgumentParser(description="Export all publicly available Gazetteer data as one RDF file.")
parser.add_argument('-t', '--target', type=is_writable_directory, nargs='?', default="./gazetteer_export.rdf",
                    help="specify output file, default: './gazetteer_export.rdf'")
parser.add_argument('-f', '--format',   nargs='?', default="turtle",
                    choices={'xml', 'n3', 'turtle', 'nt', 'pretty-xml', 'trig'},
                    help="specify the output's RDF format, default: 'turtle'")
parser.add_argument('-p', '--polygons', action='store_true',
                    help="export place shape polygons, this will increase the file size significantly")


def create_awk_multipolygon(multipolygon):
    result = "MULTIPOLYGON("

    # See https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry#Geometric_objects
    first_group = True
    for group in multipolygon:
        if not first_group:
            result += ", "

        result += "("
        first_single = True
        for polygon in group:
            if not first_single:
                result += ", "

            result += "("
            first_vertex = True
            for vertex in polygon:
                if not first_vertex:
                    result += ", "
                result += f"{vertex[0]} {vertex[1]}"
                first_vertex = False

            result += ")"
            first_single = False

        result += ")"

        first_group = False

    return result


def create_place_rdf(graph, place):

    place_uri = URIRef(place['@id'])

    graph.add((place_uri, RDF.type, CRM.E53_Place))

    if 'language' in place['prefName'] and place['prefName']['language'] != '':
        try:
            pref_label = Literal(place['prefName']['title'],
                                 lang=pycountry.languages.get(alpha_3=place['prefName']['language']).alpha_2
                                 )
        except AttributeError:
            logger.debug(f"Unknown language key for {place['@id']}:")
            logger.debug(place['prefName'])
            pref_label = Literal(place['prefName']['title'])
    else:
        pref_label = Literal(place['prefName']['title'])

    graph.add((
        place_uri,
        SKOS.prefLabel,
        pref_label
    ))

    if 'names' in place:
        for name in place['names']:
            if 'language' in name and name['language'] != '':
                try:
                    alt_label = Literal(name['title'],
                                        lang=pycountry.languages.get(alpha_3=name['language']).alpha_2
                                        )
                except AttributeError:
                    logger.debug(f"Unknown language key for {place['@id']}:")
                    logger.debug(name)
                    alt_label = Literal(name['title'])
            else:
                alt_label = Literal(name['title'])
            graph.add((
                place_uri,
                SKOS.altLabel,
                alt_label
            ))

    if 'prefLocation' in place and 'coordinates' in place['prefLocation']:
        graph.add((
            place_uri,
            WGS84_POS.long,
            Literal(place['prefLocation']['coordinates'][0])
        ))
        graph.add((
            place_uri,
            WGS84_POS.lat,
            Literal(place['prefLocation']['coordinates'][1])
        ))

        blank_node = BNode()
        graph.add((
            blank_node,
            RDF.type,
            SF.Point
        ))

        graph.add((
            blank_node,
            GEO.asWKT,
            Literal(
                f"Point({place['prefLocation']['coordinates'][1]} {place['prefLocation']['coordinates'][0]})",
                datatype=GEO.wktLiteral
            )
        ))

        graph.add((
            place_uri,
            GEO.hasGeometry,
            blank_node
        ))

    if 'prefLocation' in place and 'shape' in place['prefLocation']:
        blank_node = BNode()
        graph.add((
            blank_node,
            RDF.type,
            SF.Polygon
        ))

        graph.add((
            blank_node,
            GEO.asWKT,
            Literal(
                create_awk_multipolygon(place['prefLocation']['shape']),
                datatype=GEO.wktLiteral
            )
        ))

        graph.add((
            place_uri,
            GEO.hasGeometry,
            blank_node
        ))

    if 'identifiers' in place:
        for identifier in place['identifiers']:
            graph.add((
                place_uri,
                DC.identifier,
                Literal(f"{identifier['context']}:{identifier['value']}")
            ))

    if 'links' in place:
        for link in place['links']:

            predicate_ns, predicate_value = link['predicate'].split(':')

            if predicate_ns == "owl" and predicate_value == "sameAs":
                graph.add((
                    place_uri,
                    OWL.sameAs,
                    URIRef(quote(link['object'], safe=";/?:@&=+$,"))
                ))
            elif predicate_ns == "rdfs" and predicate_value == "seeAlso":
                graph.add((
                    place_uri,
                    RDFS.seeAlso,
                    URIRef(quote(link['object'], safe=";/?:@&=+$,"))
                ))
            else:
                logger.warning(f"Unknown link predicate prefix: {predicate_ns}. In place:")
                logger.warning(place)
                continue

    if 'parent' in place:
        graph.add((
            place_uri,
            DC.isPartOf,
            URIRef(place['parent'])
        ))

    return graph


def create_rdf(places):
    g = Graph()
    g.namespace_manager = namespace_manager

    logger.info("Creating RDF data.")
    for place in places:
        try:
            g = create_place_rdf(g, place)
        except KeyError as e:
            logger.error(e)
            logger.error(place)

    return g


if __name__ == "__main__":
    options = vars(parser.parse_args())
    harvester = Harvester(options['polygons'])

    logger.info("Retrieving raw place data...")
    data = harvester.get_data()

    rdf_graph = create_rdf(data)

    logger.info("Writing RDF file.")
    rdf_graph.serialize(destination=options['target'], format=options['format'])

    logger.info("Done")
