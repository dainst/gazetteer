from harvester import Harvester

import argparse
import logging
import os
import json

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logging.basicConfig(format="%(asctime)s-%(levelname)s-%(name)s - %(message)s")


def is_writable_directory(path: str):
    directory = os.path.dirname(path)
    if os.path.exists(directory) and (not os.path.isdir(directory) or not os.access(directory, os.W_OK)):
        msg = f"Please provide writable directory."
        raise argparse.ArgumentTypeError(msg)
    else:
        if os.path.exists(path):
            logger.warning(f"File will be replaced: {path}")

        if not os.path.exists(os.path.dirname(path)):
            os.makedirs(os.path.dirname(path))
        return path


parser = argparse.ArgumentParser(description="Export all publicly available Gazetteer data as one JSON file.")
parser.add_argument('-t', '--target', type=is_writable_directory, nargs='?', default="./gazetteer_export.json",
                    help="Specificy output directory.")

if __name__ == "__main__":
    options = vars(parser.parse_args())

    harvester = Harvester()

    places = harvester.start()

    with open(options['target'], 'w') as outfile:
        json.dump(places, outfile)
