#!/usr/bin/env bash

COMMAND=$(./build.sh)

$COMMAND com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue.HueOff $@
