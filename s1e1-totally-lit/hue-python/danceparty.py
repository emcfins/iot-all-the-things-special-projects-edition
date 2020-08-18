#!/usr/bin/env python3

import random
import sys

import requests

# Disable SSL warnings since the Hue bridge will have a self-signed certificate
requests.packages.urllib3.disable_warnings()

if (len(sys.argv) < 4):
    print("You must specify the following parameters (in order):")
    print("  - The Hue bridge's IP address")
    print("  - The Hue bridge's username")
    print("  - The Hue bridge's light IDs")
    print("")
    print("Example: ./danceparty.py 192.168.1.10 USERNAME 31 32 33")
    print("")
    print("You must specify at least one light but may specify any number of lights by adding more parameters")
    sys.exit(1)

LOCAL_HUE = sys.argv[1]
USER_NAME = sys.argv[2]

# All of the remaining parameters are light IDs
IDS = sys.argv[3:]

COLOR_MAX = 65535
BRIGHT_MAX = 254


# Obtains the URL for a specific light ID
def get_light_url(id):
    return 'https://' + LOCAL_HUE + '/api/' + USER_NAME + '/lights/' + id


# Obtains the URL to set a specific light's state directly
def get_light_state_url(id):
    return get_light_url(id) + '/state'


def get_rando(value):
    return random.randrange(value)


def get_light_status(id):
    state = requests.get(get_light_url(id), verify=False)
    return (state.json())


# Canned JSON to turn lights on and off
on = '{"on": true}'
off = '{"on": false}'


def turn_light_on(id):
    send_request_to_bridge(id, on)


def dance_party_mode():
    # Get a random brightness
    bright_val = str(get_rando(BRIGHT_MAX))

    # Get a random color
    color_val = str(get_rando(COLOR_MAX))

    # Create the JSON used to set the state with our random values
    random_on = '{"on":true, "sat":254, "bri":' + bright_val + ',"hue":' + color_val + '}'

    # Turn each light on with the specified color and then turn it off
    for id in IDS:
        send_request_to_bridge(id, random_on)
        send_request_to_bridge(id, off)

    return


def send_request_to_bridge(id, on):
    requests.put(get_light_state_url(id), data=on, verify=False)


def main():
    # Loop through each light and force it to be on if necessary. No lights can be off during dance party mode!
    for id in IDS:
        light_stat = get_light_status(id)
        if light_stat['state']['on'] == False:
            turn_light_on(id)

    # Loop forever. Dance parties must not end!
    while True:
        dance_party_mode()


if __name__ == "__main__":
    main()
