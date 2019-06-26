#!/bin/sh

ps - ef | grep torrssen2.jar | awk '{print $1}' | xargs kill
