import requests
import logging
import math


class Harvester:
    _base_url: str = "https://gazetteer.dainst.org"
    _batch_size: int = 500
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

    def _get_batch(self, scroll_id):
        url = f"{self._base_url}/search.json?pretty=true&limit={self._batch_size}&scrollId={scroll_id}"

        if not self.polygons:
            url += "&noPolygons=true"

        try:
            response = requests.get(url=url)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, 5)

    def get_data(self):

        url = f"{self._base_url}/search.json?limit={self._batch_size}&scroll=true"

        if not self.polygons:
            url += "&noPolygons=true"

        first_query = None
        try:
            response = requests.get(url=url)
            response.raise_for_status()
            first_query = response.json()
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, 5)

        if first_query is None:
            self.logger.error("Failed to retrieve first batch")
            return None
        if 'error' in first_query:
            self.logger.error(first_query['error'])
            return None

        self.logger.info(f"Got data for batch #{self._processed_batches_counter + 1}...")
        total = first_query['total']
        places = first_query['result']
        scroll_id = first_query['scrollId']
        self._processed_batches_counter += 1

        self.logger.info(f"{total} places in total.")
        self.logger.info(f"Number of batches: {math.ceil(total / self._batch_size)}")

        next_batch = self._get_batch(scroll_id)
        while next_batch['result']:
            if 'error' in first_query:
                self.logger.error(first_query['error'])
                break

            self.logger.info(f"Got data for batch #{self._processed_batches_counter + 1}...")
            places += next_batch['result']

            next_batch = self._get_batch(scroll_id)
            self._processed_batches_counter += 1

        self.logger.info(f"Done.")
        return places

    def __init__(self, include_polygons=False):

        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)

        self.polygons = include_polygons

