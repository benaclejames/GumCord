import json

import requests

LICENSE_VERIFY_ENDPOINT = "https://api.gumroad.com/v2/licenses/verify"


def verify_product_exists(product_permalink):
    response = requests.post(LICENSE_VERIFY_ENDPOINT,
                             data={'product_permalink': product_permalink,
                                   'license_key': "XXXXX"})
    response_json = json.loads(response.content)
    return "message" in response_json and response_json["message"] != "No product exists with that custom or unique permalink."


def verify_license(product_permalink, license_id):
    response = requests.post(LICENSE_VERIFY_ENDPOINT,
                             data={'product_permalink': product_permalink,
                                   'license_key': license_id,
                                   "increment_uses_count": "false"})
    response_json = json.loads(response.content)
    return response_json["success"] and not response_json["purchase"]["refunded"]
