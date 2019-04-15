import requests
import logging
import math


class Harvester:
    _base_url: str = "https://gazetteer.dainst.org"
    _batch_size: int = 250
    _processed_batches_counter: int = 0

    def _retry_query(self, url, retries_left):
        self.logger.info(f"  Retrying {url}...")
        try:
            if retries_left == 0:
                self.logger.info(f"  No retries left for #{url}.")
                return None
            else:
                response = requests.get(url=url)
                response.raise_for_status()
                self.logger.info("  Retry successful.")
                return response.json()
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, retries_left - 1)

    def _handle_query_exception(self, e, retries_left):
        self.logger.error(e)
        if type(e) is ValueError:
            self.logger.error("JSON decoding fails!")
        elif type(e) is requests.exceptions.RequestException:
            self.logger.error(f"Gazetteer service request fails!")
            self.logger.error(f"Request: {e.request}")
            self.logger.error(f"Response: {e.response}")
        elif type(e) is requests.exceptions.HTTPError and e.response.status_code == 500:
            return self._retry_query(e.request.url, retries_left)
        elif type(e) is requests.exceptions.ConnectionError:
            return self._retry_query(e.request.url, retries_left)

    def _get_batch(self, offset):
        url = f"{self._base_url}/search.json?limit={self._batch_size}&offset={offset}"

        if not self.polygons:
            url += "&noPolygons=true"

        self.logger.debug(url)
        try:
            response = requests.get(url=url)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, 5)

    def get_data(self):

        self.logger.info(f"Retrieving data for batch #{self._processed_batches_counter + 1}...")
        batch = self._get_batch(0)
        self._processed_batches_counter += 1

        total = batch['total']
        places = batch['result']

        self.logger.info(f"{total} places in query total.")
        self.logger.info(f"Number of batches: {math.ceil(total / self._batch_size)}")

        if total > self._batch_size:

            offset = self._batch_size

            while offset < total:
                self.logger.info(f"Retrieving data for batch #{self._processed_batches_counter + 1}...")
                places += self._get_batch(offset)['result']
                offset += self._batch_size

                self._processed_batches_counter += 1

        self.logger.info(f"Done.")
        return places

    def __init__(self, include_polygons):

        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)

        self.polygons = include_polygons

