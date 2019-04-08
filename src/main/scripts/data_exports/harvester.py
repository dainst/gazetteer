import gevent.monkey as monkey
monkey.patch_all(thread=False, select=False)

import requests
import grequests  # used for asynchronous/parallel queries
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

    def _collect_places_data(self, batch):
        self.logger.info(f"Retrieving place data for batch #{self._processed_batches_counter + 1}...")
        url_list = []
        for item in batch:
            url_list.append(f"{self._base_url}/doc/{item['gazId']}.json")

        places = []

        try:
            rs = [grequests.get(url) for url in url_list]
            responses = grequests.map(rs)
            for response in responses:
                if response is None:
                    continue

                response.raise_for_status()
                place = response.json()
                places.append(place)
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, 5)

        self._processed_batches_counter += 1
        return places

    def _get_batch(self, offset):
        url = f"{self._base_url}/search.json?limit={self._batch_size}&offset={offset}"
        self.logger.debug(url)
        try:
            response = requests.get(url=url)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.HTTPError as e:
            self._handle_query_exception(e, 5)

    def start(self):
        batch = self._get_batch(0)
        total = batch['total']

        self.logger.info(f"{total} places in query total.")
        self.logger.info(f"Number of batches: {math.ceil(total / self._batch_size)}")
        places = self._collect_places_data(batch['result'])

        if total > self._batch_size:
            offset = self._batch_size
            while offset < total:
                batch = self._get_batch(offset)
                places += self._collect_places_data(batch['result'])
                offset += self._batch_size

        return places

    def __init__(self):

        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)
