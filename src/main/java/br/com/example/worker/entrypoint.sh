#!/bin/sh
while true; do
  java -cp /app worker.Main
  sleep 30
done