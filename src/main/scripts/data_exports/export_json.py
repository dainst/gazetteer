from harvester import Harvester
from cli_helper import is_writable_directory

import argparse
import logging
import json

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logging.basicConfig(format="%(asctime)s-%(levelname)s-%(name)s - %(message)s")

parser = argparse.ArgumentParser(description="Export all publicly available Gazetteer data as one JSON file.")
parser.add_argument('-t', '--target', type=is_writable_directory, nargs='?', default="./gazetteer_export.json",
                    help="Specificy output directory.")

if __name__ == "__main__":
    options = vars(parser.parse_args())

    harvester = Harvester()

    places = harvester.start()

    with open(options['target'], 'w') as outfile:
        json.dump(places, outfile)
