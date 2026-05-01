#!/bin/sh
while true; do
  java -cp /app br.com.example.worker.Main
  sleep 30
done