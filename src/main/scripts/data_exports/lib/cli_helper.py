import os
import argparse

import logging

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
